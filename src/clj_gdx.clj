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
  (:import (com.badlogic.gdx.graphics.g2d SpriteBatch)))

(def default-display display/default)

(def default-app app/default)

(defn started?
  []
  (app/started?))

(defn get-fps
  []
  (app/get-fps))

(defn get-delta-time
  []
  (app/get-delta-time))

(defn get-frame-id
  []
  (app/get-frame-id))

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

(defn get-string
  ([title]
   (get-string title "" ""))
  ([title hint]
   (get-string title hint ""))
  ([title hint s]
   (keyboard/get-string title hint s)))

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

(defmacro using-sprite-options
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

(defmacro using-font-options
  [context & body]
  `(let [context# ~context]
     (using-font-color (:font context# default-font)
                       (:color context#)
                       ~@body)))

(defn draw-sprite!
  ([sprite x y w h]
   (draw-sprite! sprite x y w h {}))
  ([sprite x y w h context]
   (using-sprite-options
     context
     (batch/draw-texture-region! *sprite-batch* (region/find sprite) x y w h))))

(defn draw-string!
  ([s x y w]
   (draw-string! s x y w {}))
  ([s x y w context]
   (using-font-options
     context
     (batch/draw-string-wrapped! *sprite-batch* (font/find (:font context default-font)) s x y w))))