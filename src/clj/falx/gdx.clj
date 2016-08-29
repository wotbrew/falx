(ns falx.gdx
  "A clojure wrapper for libgdx.
  Many gdx operations are not threadsafe, and need to be called on the gdx application thread.
  If in doubt, use `run` to dispatch the operations correctly on the gdx thread.

  Most functions will require the gdx app to be running in order to work."
  (:require [clojure.java.io :as io]
            [clojure.string :as str])
  (:import (com.badlogic.gdx Gdx ApplicationListener Input$Buttons Input$Keys)
           (com.badlogic.gdx.files FileHandle)
           (com.badlogic.gdx.graphics Texture Pixmap Pixmap$Format Color OrthographicCamera Camera)
           (com.badlogic.gdx.graphics.g2d TextureRegion SpriteBatch BitmapFont BitmapFont$TextBounds)
           (com.badlogic.gdx.math Vector3 Matrix4)
           (com.badlogic.gdx.backends.lwjgl LwjglApplicationConfiguration LwjglApplication)
           (org.lwjgl.opengl GL11)
           (clojure.lang Indexed)))

(defn ensure-started
  "Throws if the gdx application instance hasn't started."
  []
  (assert (some? Gdx/app) "Gdx started"))

(def ^:dynamic *on-thread* false)

(defn run*
  "Calls the function on the gdx thread, returns a delay that will yield the result."
  [f]
  (ensure-started)
  (if *on-thread*
    (deliver (promise) (f))
    (let [p (promise)]
      (.postRunnable
        Gdx/app
        (fn []
          (deliver p (try (f) (catch Throwable e e)))))
      (delay
        (let [ret @p]
          (if (instance? Throwable ret)
            (throw ret)
            ret))))))

(defmacro run
  "Runs body on the gdx thread, returns a delay that will yield the result."
  [& body]
  `(run* (fn [] ~@body)))

(defn lwjgl-app
  "Creates and starts an lwjgl gdx application.
  Requires a function `f` which is to be called with 0-args every frame.

  opts:
  `size` a vector of [w h] which defines the initial screen size (default [800, 600])
  `:title` the initial screen title. (default \"Untitled\")"
  [f & {:as opts}]
  (let [listener
        (reify ApplicationListener
          (create [this]
            this)
          (resize [this width height]
            this)
          (render [this]
            (try
              (binding [*on-thread* true]
                (f))
              (catch Throwable e
                nil)))
          (pause [this]
            this)
          (resume [this]
            this)
          (dispose [this]
            this))
        cfg (LwjglApplicationConfiguration.)
        [w h] (:size opts [800 600])]
    (set! (.-width cfg) w)
    (set! (.-height cfg) h)
    (set! (.-title cfg) (:title opts "Untitled"))
    (LwjglApplication. listener cfg)))

(defn- ^FileHandle file-handle
  [x]
  (FileHandle. (io/file x)))

(defprotocol IGdxColor
  "A thing that can translated into a gdx Color instance."
  (-gdx-color [this] "Coerce the input into a gdx Color instance."))

(extend-protocol IGdxColor
  Color
  (-gdx-color [this] this)
  Indexed
  (-gdx-color [this]
    (let [[r g b a] this]
      (Color. r g b a))))

(defn color
  "Returns a color from `c`, or from rgba values of 0 to 1.0.
  Coercion is performed via the IGdxColor protocol."
  ([c]
   (-gdx-color color))
  ([r g b a]
   (Color. r g b a)))

(defn pixmap
  "Returns a pixmap, which is a surface you can draw to in a simple way.
  opts:
  `:fill` - fills the initial pixmap with the specified color"
  [w h & {:as opts}]
  (let [p (Pixmap. (int w) (int h) Pixmap$Format/RGBA8888)]
    (when-some [^Color c (some-> opts :fill color)]
      (.setColor p c)
      (.fill p))
    p))

(defn texture
  "Loads the texture from the given file."
  [file]
  (Texture. (file-handle file)))

(defn pixmap->texture
  "Returns a texture for the given pixmap."
  [p]
  (Texture. ^Pixmap p))

(defn pixel
  "Returns a 1x1 texture of the color given by `c`."
  [c]
  (pixmap->texture (pixmap 1 1 :fill c)))

(defn texture-region
  "Returns a texture region of the texture. Returns the TextureRegion instance."
  [t x y w h]
  (let [region (TextureRegion. ^Texture t (float x) (float y) (float w) (float h))]
    ;;We want to work with y pointing down.
    (.flip region false true)
    region))

(defn font
  "Returns a new bitmap font. If a font file isn't provided, loads
  the default font from the libgdx jar."
  ([]
   (BitmapFont. true))
  ([file]
   (BitmapFont. (file-handle file) true)))

(defn measure
  "Measures the string s with the font, returns a vector of [w h]."
  ([font s]
   (let [^BitmapFont$TextBounds bounds (.getMultiLineBounds font (str s))]
     [(.-width bounds) (.-height bounds)]))
  ([font s w]
   (let [^BitmapFont$TextBounds bounds (.getWrappedBounds font (str s) (float w))]
     [(.-width bounds) (.-height bounds)])))

(declare look!)

(defn camera
  "Returns an OrthographicCamera instance."
  [w h]
  (doto (OrthographicCamera. w h)
    ;; We want to work with y pointing down.
    (.setToOrtho true)
    (look! 0 0)))

(defn look!*
  "Like `look!` but doesn't automatically .update the camera. Use this if
  you want to perform several transformations before calling .update yourself."
  [cam x y]
  (let [^Vector3 pos (.-position ^OrthographicCamera cam)
        w (.-viewportWidth cam)
        h (.-viewportHeight cam)]
    ;; start 0,0 at top left of world
    (.set pos (+ (/ w 2) (float x)) (+ (/ h 2) (float y)) 0)))

(defn look!
  "Points the camera at the given x, y co-ordinates."
  [cam x y]
  (doto ^OrthographicCamera cam
    (look!* x y)
    (.update)))

(defn view!*
  "Like `view!` but doesn't automatically .update the camera. Use this if
  you want to perform several transformations before calling .update yourself."
  ([cam w h]
    ;; We want to work with y pointing down.
   (.setToOrtho  ^OrthographicCamera cam true w h))
  ([cam x y w h]
   (doto ^OrthographicCamera cam
     (look! x y)
     (view!* w h))))

(defn view!
  "Sets the location and viewport of the camera to the given rectangle.
  If `x` and `y` are omitted, simply resizes the camera viewport."
  ([cam w h]
   (doto ^OrthographicCamera cam
     (view!* w h)
     (.update)))
  ([cam x y w h]
   (doto ^OrthographicCamera cam
     (view!* x y w h)
     (.update))))

(defn project
  "Returns the given world co-ordinates and projects them via the camera into screen co-ordinates.
  Returns the screen co-ordinates as a vector of [x y]."
  [cam x y]
  (let [v3 (Vector3. x y 1)]
    (.project ^Camera cam v3)
    [(int (.-x v3))
     (int (.-y v3))]))

(defn unproject
  "Returns the given screen co-ordinates and reverse projects them via the camera into world co-ordinates.
  Returns the world co-ordinates as a vector of [x y]."
  [cam x y]
  (let [v3 (Vector3. x y 1)]
    (.unproject ^Camera cam v3)
    [(int (.-x v3))
     (int (.-y v3))]))

(defn batch
  "Returns a new SpriteBatch"
  []
  (SpriteBatch.))

(def ^:dynamic *batch*
  "Bound to a SpriteBatch instance typically when rendering via `with-batch`."
  nil)

(defn ensure-batch
  "Returns the bound *batch* or throws an exception."
  []
  (if-some [b *batch*]
    b
    (throw (Exception. "*batch* is not bound"))))

(defmacro with-batch
  "Will use the given sprite batch contextually in the body, when not otherwise specified."
  [batch & body]
  `(if-some [^SpriteBatch b# ~batch]
     (binding [*batch* b#]
       (.begin b#)
       (try
         ~@body
         (finally
           (.end b#))))
     (do ~@body)))

(defmacro with-projection
  "Will use the given projection matrix contextually in the body, when not otherwise specified."
  [matrix & body]
  `(let [^SpriteBatch b# (ensure-batch)
         ^Matrix4 m# ~matrix
         ^Matrix4 o# (.cpy (.getProjectionMatrix b#))]
     (.setProjectionMatrix b# m#)
     (try
       ~@body
       (finally
         (.setProjectionMatrix b# o#)))))

(defmacro with-camera
  "Will use the given camera contextually in the body, when not otherwise specified."
  [cam & body]
  `(if-some [^OrthographicCamera c# ~cam]
     (with-projection
       (.-combined c#)
       ~@body)
     (do ~@body)))

(def ^:dynamic *font*
  "Bound to a BitmapFont instance typically when rendering via `with-font`."
  nil)

(defn ensure-font
  "Returns the bound *font* or throws an exception."
  []
  (if-some [f *font*]
    f
    (throw (Exception. "*font* is not bound"))))

(defmacro with-font
  "Will use the given font contextually in the body, when not otherwise specified."
  [font & body]
  `(if-some [font# ~font]
    (binding [*font* font#]
      ~@body)
    ~@body))


(defmacro with-color
  "Will use the given color contextually in the body, when not otherwise specified."
  [c & body]
  `(let [^Color c# (color ~c)
         ^SpriteBatch b# *batch*
         ^BitmapFont f# *font*
         ob# (some-> b# .getColor)
         of# (some-> f# .getColor)]
     (some-> b# (.setColor c#))
     (some-> f# (.setColor c#))
     (try
       ~@body
       (finally
         (some-> b# (.setColor ob#))
         (some-> f# (.setColor of#))))))

(defprotocol IDrawable
  "A thing that can be drawn directly to the screen, bounded by a rectangle."
  (-draw! [this x y w h] "Draws the thing to the screen"))

(defn draw!
  "Draws the thing to the screen
  e.g (draw! \"hello world!\" 32 32 64 32)"
  [o x y w h]
  (-draw! o x y w h))

(extend-protocol IDrawable
  CharSequence
  (-draw! [this x y w h]
    (.drawWrapped ^BitmapFont (ensure-font) ^SpriteBatch (ensure-batch) this  (float x) (float y) (float w)))
  Texture
  (-draw! [this x y w h]
    (.draw ^SpriteBatch (ensure-batch) this (float x) (float y) (float w) (float h)))
  TextureRegion
  (-draw! [this x y w h]
    (.draw ^SpriteBatch (ensure-batch) this (float x) (float y) (float w) (float h))))

(defn clear!
  "Clears the screen"
  []
  (GL11/glClearColor 0 0 0 0)
  (GL11/glClear GL11/GL_COLOR_BUFFER_BIT))

(defmacro render
  "Prepares the screen for rendering, executing body binding the following resources.
  All are optional, but will effect what operations are available to you in the body.

  opts:
   `:batch` A SpriteBatch instance
   `:font` A font to use for rendering bare strings
   `:camera` A camera to use"
  [opts & body]
  `(let [opts# ~opts
         batch# (:batch opts#)
         font# (:font opts#)
         cam# (:camera opts#)]
     (clear!)
     (->> (do ~@body)
          (with-camera cam#)
          (with-font font#)
          (with-batch batch#))))

(defn resize!
  "Resizes the screen
  opts:
   `fullscreen?` - Whether or not the app should run in full screen (false)"
  [w h & {:as opts}]
  (ensure-started)
  (.setDisplayMode Gdx/graphics w h (boolean (:fullscreen? opts))))

(defn frame-stats
  "Returns statistics about the current frame
  as a map.
  e.g
  `:fps` the current frames per second count
  `:delta` the delta time in seconds since the last frame
  `:frame-id` the identity of the frame, this increases sequentially over time"
  []
  (ensure-started)
  (let [gfx Gdx/graphics]
    {:fps (.getFramesPerSecond gfx)
     :delta (.getDeltaTime gfx)
     :frame-id (.getFrameId gfx)}))

(defn mouse
  "Returns the current mouse co-ordinates as a vector of `x` and `y`."
  []
  (ensure-started)
  (let [input Gdx/input]
    [(.getX input)
     (.getY input)]))

(def ^:private buttons
  {:left Input$Buttons/LEFT
   :middle Input$Buttons/MIDDLE
   :right Input$Buttons/RIGHT})

(defn buttons-pressed
  "Returns the set of currently pressed buttons"
  []
  (ensure-started)
  (let [input Gdx/input
        set (transient #{})
        rf (fn [set k ^Input$Buttons v]
             (if (.isButtonPressed input v)
               (conj! set k)
               set))]
    (persistent! (reduce-kv rf set buttons))))

(def ^:private keyboard-keys
  (->> (concat
         (let [alphabet "abcdefghijklmnopqrstuvwxyz"]
           (for [c alphabet]
             [(keyword (str c)) (Input$Keys/valueOf (str/upper-case (str c)))])))
       {:esc Input$Keys/ESCAPE
        :shift-left Input$Keys/SHIFT_LEFT
        :shift-right Input$Keys/SHIFT_RIGHT
        ;;todo fill in the rest...
        }
       (into {})))

(defn keys-pressed
  "Returns the set of currently pressed keys"
  []
  (ensure-started)
  (let [input Gdx/input
        set (transient #{})
        rf (fn [set k ^Input$Keys v]
             (if (.isKeyPressed input v)
               (conj! set k)
               set))]
    (persistent! (reduce-kv rf set keyboard-keys))))