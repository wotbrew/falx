(ns clj-gdx
  (:require [gdx.app :as app]
            [gdx.display :as display]
            [gdx.batch :as batch]
            [gdx.color :as color]
            [gdx.camera :as camera]
            [gdx.gl :as gl]
            [gdx.font :as font]
            [gdx.texture-region :as region]
            [gdx.texture :as texture]
            [gdx.mouse :as mouse]
            [gdx.keyboard :as keyboard])
  (:import (com.badlogic.gdx.graphics.g2d SpriteBatch TextureRegion)
           (java.util Map)))

(def default-display display/default)

(def default-app app/default)

(defn get-fps
  []
  (app/get-fps))

(defn get-delta-time
  []
  (app/get-delta-time))

(defn get-display
  []
  (app/on-render-thread
    (display/get-current)))

(defn sync-display!
  [display]
  (app/on-render-thread
    (display/sync! display)))

(defn start-app!
  [app]
  (app/start! app))

(def ^:dynamic *sprite-batch* nil)

(defonce default-sprite-batch (delay (app/on-render-thread (SpriteBatch.))))

(defmacro using-batch
  [batch & body]
  `(binding [*sprite-batch* ~batch]
    (batch/using *sprite-batch* ~@body)))

(defmacro using-default-batch
  [& body]
  `(using-batch @default-sprite-batch ~@body))

(defmacro using-camera
  [camera & body]
  `(camera/using-camera
     *sprite-batch*
     ~camera
     ~@body))

(def default-camera
  (camera/ortho-camera
    :flip-y? true))

(def keyboard-state (atom (keyboard/get-keyboard)))

(def default-keyboard keyboard/default)

(def mouse-state (atom (mouse/get-mouse)))

(def default-mouse mouse/default)

(defmacro defrender
  [& body]
  `(app/defrender
     (try
       (swap! keyboard-state keyboard/get-next-keyboard)
       (swap! mouse-state mouse/get-next-mouse)
       (using-default-batch
         (using-camera
           ~default-camera
           (gl/clear!)
           ~@body))
       (catch Throwable e#))))

(defn font
  ([]
    (font/font :flip-y? true))
  ([file]
    (font/font :file file
               :flip-y? true)))

(def default-font
  (font))

(defn get-string-bounds
  ([s]
   (get-string-bounds s default-font))
  ([s font]
   (font/get-bounds font s)))

(defn get-string-wrapped-bounds
  ([s width]
   (get-string-wrapped-bounds s width default-font))
  ([s width font]
   (font/get-bounds-wrapped font s width)))

(defn sprite
  [file rect]
  (let [texture (texture/texture file)]
    (region/texture-region
      texture
      rect
      :flip-y? true)))

(defmacro using-color
  [color & body]
  `(let [c# ~color]
     (if c#
       (batch/using-color-float-bits
         *sprite-batch*
         (:float-bits c# (color/get-float-bits c#))
         ~@body)
       (do ~@body))))

(defmacro using-texture-region-context
  [context & body]
  `(let [context# ~context]
     (using-color (:color context#) ~@body)))

(defmacro using-font-color
  [font color & body]
  `(let [c# ~color]
     (if c#
       (font/using-color-float-bits
         ~font
         (:float-bits c# (color/get-float-bits c#))
         ~@body)
       (do ~@body))))

(defmacro using-font-context
  [context & body]
  `(let [context# ~context]
     (using-font-color (:font context# default-font)
                       (:color context#)
                       ~@body)))

(defprotocol IDraw
  (-draw! [this x y context]))

(defn draw!
  ([thing point]
   (draw! thing point {}))
  ([thing point context]
   (let [x (nth point 0)
         y (nth point 1)]
     (-draw! thing x y context))))

(defmulti draw-map! (fn [m x y context] (:type m)))

(defmethod draw-map! :default
  [m x y context]
  (-draw! (pr-str m) x y context))

(extend-protocol IDraw
  Object
  (-draw! [this x y context]
    (using-font-context
      context
      (batch/draw-string!
        *sprite-batch*
        (font/find (:font context default-font))
        this
        x y)))
  nil
  (-draw! [this x y context]
    (-draw! "?" x y context))
  Map
  (-draw! [this x y context]
    (draw-map! this x y context))
  TextureRegion
  (-draw! [this x y context]
    (using-texture-region-context
      context
      (batch/draw-texture-region! *sprite-batch* this x y))))

(defmethod draw-map! :resource/texture-region
  [region x y context]
  (-draw! (region/find region) x y context))

(defprotocol IDrawIn
  (-draw-in! [this x y w h context]))

(defn draw-in!
  ([thing rect]
   (draw-in! thing rect {}))
  ([thing rect context]
   (let [x (nth rect 0)
         y (nth rect 1)
         w (nth rect 2)
         h (nth rect 3)]
     (-draw-in! thing x y w h context))))

(defmulti draw-map-in! (fn [m x y w h context]
                         (:type m)))

(defmethod draw-map-in! :default
  [m x y w h context]
  (-draw-in! (pr-str m) x y w h context))

(extend-protocol IDrawIn
  Object
  (-draw-in! [this x y w h context]
    (using-font-context
      context
      (batch/draw-string-wrapped!
        *sprite-batch*
        (font/find (:font context default-font))
        this
        x y w)))
  nil
  (-draw-in! [this x y w h context]
    (-draw-in! "?" x y w h context))
  Map
  (-draw-in! [this x y w h context]
    (draw-map-in! this x y w h context))
  TextureRegion
  (-draw-in! [this x y w h context]
    (using-texture-region-context
      context
      (batch/draw-texture-region! *sprite-batch* this x y w h))))

(defmethod draw-map-in! :resource/texture-region
  [region x y w h context]
  (-draw-in! (region/find region) x y w h context))