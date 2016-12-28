(ns falx.gdx
  (:require [clojure.set :as set]
            [clojure.pprint :refer [pprint]])
  (:import (com.badlogic.gdx Gdx ApplicationListener Application Input$Keys Input$Buttons)
           (java.net URL)
           (com.badlogic.gdx.backends.lwjgl LwjglApplication)
           (com.badlogic.gdx.graphics Texture Pixmap Pixmap$Format Color OrthographicCamera)
           (com.badlogic.gdx.files FileHandle)
           (com.badlogic.gdx.graphics.g2d TextureRegion SpriteBatch BitmapFont BitmapFont$TextBounds)
           (org.lwjgl.opengl GL11)
           (java.io Writer PrintWriter)))

(set! *warn-on-reflection* true)

(def config-ref
  (atom {:size           [640 480]
         :fullscreen? false
         :title          (str *ns*)
         :show-debug-key Input$Keys/F1}))

(defn configure!
  [& {:as kvs}]
  (swap! config-ref merge kvs))

(def ^:private last-config-ref
  (atom {}))

(def ^:dynamic *on-render-thread* false)

(def ^:private renderer (atom (fn [])))

(defn- do-render
  []
  (try
    (let [lcfg @last-config-ref
          {:keys [fullscreen? title size] :as cfg} @config-ref
          [w h] size]
      (when (or (not= (:size lcfg) size)
                (not= (:fullscreen? lcfg) fullscreen?))
        (.setDisplayMode Gdx/graphics w h (boolean fullscreen?)))
      (when (not= (:title lcfg) title)
        (.setTitle Gdx/graphics (str title)))
      (reset! last-config-ref cfg))
    (binding [*on-render-thread* true]
      (@renderer))
    (catch Throwable e
      ;; swallow
      )))

(defn- do-resize
  [w h]
  (swap! config-ref assoc :size [(long w) (long h)]))

(defonce ^Application app
  (LwjglApplication.
    (reify ApplicationListener
      (create [this])
      (resize [this width height]
        (do-resize width height))
      (render [this]
        (do-render))
      (pause [this]
        )
      (resume [this]
        )
      (dispose [this]
        ))))

(defn dispatch
  [f]
  (if *on-render-thread*
    (delay (f))
    (let [p (promise)]
      (.postRunnable
        app
        (fn [] (try
                 (deliver p (f))
                 (catch Throwable e
                   (deliver p e)))))
      (delay
        (let [ret @p]
          (if (instance? Throwable ret)
            (throw ret)
            ret))))))

(defmacro run
  [& body]
  `(if *on-render-thread*
     (do ~@body)
     @(dispatch (fn [] ~@body))))

(defn ^FileHandle file-handle
  [x]
  (if (instance? URL x)
    (if (= "jar" (.getProtocol ^URL x))
      (.classpath Gdx/files (.getPath ^URL x))
      (.absolute Gdx/files (.getPath ^URL x)))
    (.internal Gdx/files (str x))))

(defonce texture
  (memoize (fn [file] (run (Texture. (file-handle file))))))

(def blank*
  (memoize
    (fn [color]
      (run
        (let [pm (doto (Pixmap. 1 1 Pixmap$Format/RGBA8888)
                   (.setColor ^Color color)
                   (.fill))]
          (Texture. ^Pixmap pm))))))

(defn blank
  [color]
  (blank* color))

(defonce ^Texture pixel
  (blank Color/WHITE))

(defn texture-region
  [^Texture tex x y w h]
  (doto (TextureRegion. tex (long x) (long y) (long w) (long h))
    (.flip false true)))

(defonce ^OrthographicCamera camera
  (run (doto (OrthographicCamera.)
         (.setToOrtho true 640 480))))

(defonce ^SpriteBatch sprite-batch
  (run (SpriteBatch.)))

(defonce ^BitmapFont default-font
  (run (BitmapFont. true)))

(def ^:dynamic ^BitmapFont *font* default-font)

(defn set-color!
  [^Color color]
  (.setColor sprite-batch color)
  (.setColor *font* color))

(defmacro with-color
  [color & body]
  `(let [c# ~color
         o# (.cpy (.getColor sprite-batch))]
     (set-color! c#)
     (try
       ~@body
       (finally
         (set-color! o#)))))

(defmacro with-tint
  [color & body]
  `(let [^Color c# ~color
         o# (.cpy (.getColor sprite-batch))
         ^Color tmp# (.cpy o#)
         tmp# (.mul tmp# c#)]
     (set-color! tmp#)
     (try
       ~@body
       (finally
         (set-color! o#)))))

(defn draw-string!
  ([s x y]
    (.drawMultiLine *font* sprite-batch (str s) (float x) (float y))
    nil)
  ([s x y w]
   (.drawWrapped *font* sprite-batch (str s) (float x) (float y) (float w))
   nil))

(defn draw-texture!
  ([^Texture tex x y]
   (.draw sprite-batch tex (float x) (float y)))
  ([^Texture tex x y w h]
   (.draw sprite-batch tex (float x) (float y) (float w) (float h))))

(defn draw-texture-region!
  ([^TextureRegion tr x y]
    (.draw sprite-batch tr (float x) (float y)))
  ([^TextureRegion tr x y w h]
   (.draw sprite-batch tr (float x) (float y) (float w) (float h))))

(defprotocol IDrawAt
  (draw-at! [this x y]))

(extend-protocol IDrawAt
  String
  (draw-at! [this x y]
    (.drawMultiLine *font* sprite-batch this (float x) (float y)))
  Texture
  (draw-at! [this x y]
    (.draw sprite-batch this (float x) (float y)))
  TextureRegion
  (draw-at! [this x y]
    (.draw sprite-batch this (float x) (float y))))

(defprotocol IDrawIn
  (draw-in! [this x y w h]))

(extend-protocol IDrawIn
  String
  (draw-in! [this x y w _]
    (.drawWrapped *font* sprite-batch this (float x) (float y) (float w)))
  Texture
  (draw-in! [this x y w h]
    (.draw sprite-batch this (float x) (float y) (float w) (float h)))
  TextureRegion
  (draw-in! [this x y w h]
    (.draw sprite-batch this (float x) (float y) (float w) (float h))))

(defn draw!
  ([obj]
   (draw! obj 0 0))
  ([obj loc]
    (case (count loc)
      2 (draw-at! obj (nth loc 0) (nth loc 1))
      4 (draw-in! obj (nth loc 0) (nth loc 1) (nth loc 2) (nth loc 3))))
  ([obj x y]
    (draw-at! obj x y))
  ([obj x y w h]
    (draw-in! obj x y w h)))

(defn draw-pixel!
  ([^double x ^double y]
   (.draw sprite-batch pixel x y))
  ([^double x ^double y ^double w ^double h]
   (.draw sprite-batch pixel x y w h)))

(defn draw-box!
  ([x y w h]
   (draw-box! x y w h 1))
  ([x y w h t]
   (let [x (float x)
         y (float y)
         w (float w)
         h (float h)
         t (float t)]
     (draw-pixel! x y w t)
     (draw-pixel! x y t h)
     (draw-pixel! x (+ y h (- t)) w t)
     (draw-pixel! (+ x w (- t)) y t h))))

(defn- box*
  ([t]
   (reify IDrawIn
     (draw-in! [this x y w h]
       (draw-box! x y w h t)))))

(def box1 (box* 1))

(defn box
  ([] box1)
  ([thickness]
    (box* thickness)))

(defn recolor
  [obj color]
  (reify IDrawIn
    (draw-in! [this x y w h]
      (with-color color (draw-in! obj x y w h)))
    IDrawAt
    (draw-at! [this x y]
      (with-color color (draw-at! obj x y)))))

(defn tint
  [obj color]
  (reify IDrawIn
    (draw-in! [this x y w h]
      (with-tint color (draw-in! obj x y w h)))
    IDrawAt
    (draw-at! [this x y]
      (with-tint color (draw-at! obj x y)))))

(def nothing
  (reify IDrawIn
    (draw-in! [this x y w h])
    IDrawAt
    (draw-at! [this x y])))

(defn overlay
  ([] nothing)
  ([a] a)
  ([a b]
   (reify IDrawIn
     (draw-in! [this x y w h]
       (draw-in! a x y w h)
       (draw-in! b x y w h))
     IDrawAt
     (draw-at! [this x y]
       (draw-at! a x y)
       (draw-at! b x y))))
  ([a b c]
   (reify IDrawIn
     (draw-in! [this x y w h]
       (draw-in! a x y w h)
       (draw-in! b x y w h)
       (draw-in! c x y w h))
     IDrawAt
     (draw-at! [this x y]
       (draw-at! a x y)
       (draw-at! b x y)
       (draw-at! c x y))))
  ([a b c d]
   (reify IDrawIn
     (draw-in! [this x y w h]
       (draw-in! a x y w h)
       (draw-in! b x y w h)
       (draw-in! c x y w h)
       (draw-in! d x y w h))
     IDrawAt
     (draw-at! [this x y]
       (draw-at! a x y)
       (draw-at! b x y)
       (draw-at! c x y)
       (draw-at! d x y))))
  ([a b c d & more]
   (reify IDrawIn
     (draw-in! [this x y w h]
       (draw-in! a x y w h)
       (draw-in! b x y w h)
       (draw-in! c x y w h)
       (draw-in! d x y w h)
       (run! #(draw-in! % x y w h) more))
     IDrawAt
     (draw-at! [this x y]
       (draw-at! a x y)
       (draw-at! b x y)
       (draw-at! c x y)
       (draw-at! d x y)
       (run! #(draw-at! % x y) more)))))

(defn translate
  ([obj loc]
    (let [[x y] loc]
      (translate obj x y)))
  ([obj x2 y2]
    (reify IDrawIn
      (draw-in! [this x y w h]
        (draw-in! obj (+ x x2) (+ y y2) w h))
      IDrawAt
      (draw-at! [this x y]
        (draw-at! obj (+ x x2) (+ y y2))))))

(defn resize
  ([obj size]
    (let [[w h] size]
      (resize obj w h)))
  ([obj w h]
   (reify IDrawIn
     (draw-in! [this x y _ _]
       (draw-in! obj x y w h))
     IDrawAt
     (draw-at! [this x y]
       (draw-in! obj x y w h)))))

(defn in
  ([obj loc]
    (let [[x y w h] loc]
      (in obj x y w h)))
  ([obj x y w h]
   (reify IDrawIn
     (draw-in! [this x2 y2 _ _]
       (draw-in! obj (+ x x2) (+ y y2) w h))
     IDrawAt
     (draw-at! [this x2 y2]
       (draw-in! obj (+ x x2) (+ y y2 ) w h)))))

(defprotocol IMeasure
  (-measure [this]))

(defprotocol IMeasureIn
  (-measure-in [this w h]))

(extend-protocol IMeasure
  String
  (-measure [this]
    (run
      (let [^BitmapFont$TextBounds tb (.getMultiLineBounds *font* this)]
        [(.-width tb) (.-height tb)])))
  Texture
  (-measure [this]
    [(.getWidth this) (.getHeight this)])
  TextureRegion
  (-measure [this]
    [(.getRegionWidth this) (.getRegionHeight this)]))

(extend-protocol IMeasureIn
  String
  (-measure-in [this w h]
    (run
      (let [^BitmapFont$TextBounds tb (.getWrappedBounds *font* this w)]
        [(.-width tb) (.-height tb)])))
  Texture
  (-measure-in-in [this w h]
    [w h])
  TextureRegion
  (-measure-in-in [this w h]
    [w h]))

(defn measure
  ([obj]
    (-measure obj))
  ([obj size]
    (-measure-in obj (nth size 0) (nth size 1)))
  ([obj w h]
    (-measure-in obj w h)))

(defn draw-tiled!
  [obj x y w h w2 h2]
  (let [x (float x)
        y (float y)
        w (float w)
        h (float h)
        w2 (float w2)
        h2 (float h2)
        columns (Math/ceil (/ w (max 1 w2)))
        rows (Math/ceil (/ h (max 1 h2)))]
    (loop [c 0]
      (when (< c columns)
        (loop [r 0]
          (when (< r rows)
            (draw! obj (+ x (* w2 c)) (+ y (* h2 r)) w2 h2)
            (recur (inc r))))
        (recur (inc c))))))

(defn tiled
  ([obj]
    (let [[w h] (measure obj)]
      (tiled obj w h)))
  ([obj size]
    (let [[w h] size]
      (tiled obj w h)))
  ([obj w2 h2]
   (reify IDrawIn
     (draw-in! [_ x y w h]
       (draw-tiled! obj x y w h w2 h2)))))

(def key-codes
  (set
    (for [n (range 256)
          :let [k (Input$Keys/toString n)]
          :when (some? k)]
      (Input$Keys/valueOf k))))

(defn describe-key-code
  [n]
  (Input$Keys/toString n))

(def button-codes
  #{Input$Buttons/BACK
    Input$Buttons/FORWARD
    Input$Buttons/LEFT
    Input$Buttons/MIDDLE
    Input$Buttons/RIGHT})

(defn describe-button-code
  [^long n]
  (case n
    0 "Left"
    1 "Right"
    2 "Middle"
    3 "Back"
    4 "Forward"))

(def ^:private tick-ref
  (volatile! {:keys-down #{}
              :buttons-down #{}}))

(defn current-tick
  []
  @tick-ref)

(defn- set-tick!
  []
  (let [{:keys [keys-down buttons-down]} @tick-ref
        keys-down2 (into #{} (filter #(.isKeyPressed Gdx/input %)) key-codes)
        buttons-down2 (into #{} (filter #(.isButtonPressed Gdx/input %)) button-codes)
        mouse-loc [(.getX Gdx/input) (.getY Gdx/input)]
        frame-id (.getFrameId Gdx/graphics)
        fps (.getFramesPerSecond Gdx/graphics)
        delta (.getDeltaTime Gdx/graphics)
        keys-hit (set/difference keys-down keys-down2)
        buttons-hit (set/difference buttons-down buttons-down2)]
    (vreset! tick-ref
             {:config @config-ref
              :keys-down    keys-down2
              :keys-hit     keys-hit
              :buttons-down buttons-down2
              :buttons-hit  buttons-hit
              :fps          fps
              :delta        delta
              :frame-id     frame-id
              :mouse-loc    mouse-loc})))

(def ^:private tick-handlers (atom {}))
(def ^:private tick-handler-sort (atom {}))

(defn assoc-tickfn!
  ([k f]
    (assoc-tickfn! k f 0))
  ([k f n]
   (swap! tick-handlers assoc k f)
   (swap! tick-handler-sort assoc k n)
   (reset!
     renderer
     (fn []
       (.begin sprite-batch)
       (let [cfg @config-ref
             [w h] (:size cfg)
             ow (.-viewportWidth camera)
             oh (.-viewportHeight camera)]
         (when (or (not= w ow)
                   (not= h oh))
           (.setToOrtho camera true (float w) (float h))
           (.update camera)))
       (.update camera)
       (.setProjectionMatrix sprite-batch (.-combined camera))
       (GL11/glClear GL11/GL_COLOR_BUFFER_BIT)
       (try
         (let [tick-data (set-tick!)]
           (run! #(% tick-data) (map @tick-handlers (sort-by @tick-handler-sort (keys @tick-handlers)))))
         (catch Throwable e#
           (GL11/glClear GL11/GL_COLOR_BUFFER_BIT)
           (draw! (with-out-str (.printStackTrace e# (PrintWriter. ^Writer *out*))) 0 0))
         (finally
           (.end sprite-batch)))))
   nil))

(def ^:dynamic *translation* [0 0])

(defmacro with-translation
  [loc & body]
  `(let [loc# ~loc]
     (if (= *translation* loc#)
       (do ~@body)
       (binding [*translation* loc#]
         (let [pos# (.cpy (.-position camera))
               [x# y#] loc#
               x# (float x#)
               y# (float y#)]
           (.translate camera (- x#) (- y#))
           (.update camera)
           (.setProjectionMatrix sprite-batch (.-combined camera))
           (try
             ~@body
             (finally
               (.set (.-position camera) pos#)
               (.update camera)
               (.setProjectionMatrix sprite-batch (.-combined camera)))))))))

(defmacro on-tick
  [name binding & body]
  (let [sort (or (:sort (meta name)) 0)]
    `(let [f# (fn ~binding ~@body)]
       (assoc-tickfn! (quote ~name) f# ~sort))))

(defonce ^:private debug-ref (atom {}))

(on-tick ^{:sort Long/MAX_VALUE} draw-debug-info
  [{:keys [keys-hit config] :as tick}]
  (when (contains? keys-hit (:show-debug-key config Input$Keys/F1))
    (swap! debug-ref update :show? (comp boolean not)))
  (when (:show? @debug-ref)
    (with-color
      Color/GREEN
      (draw! (with-out-str
               (pprint tick))
             0 0))))