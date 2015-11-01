(ns gdx.display
  (:require [gdx.dispatch :as dispatch])
  (:import (com.badlogic.gdx Gdx)))

(defn set-title!
  [s]
  (dispatch/on-render-thread
    (.. Gdx/app getGraphics (setTitle (str s)))))

(defn set-display-mode!
  ([size fullscreen?]
    (let [[w h] size]
      (set-display-mode! w h fullscreen?)))
  ([width height fullscreen?]
   (dispatch/on-render-thread
     (.. Gdx/app getGraphics (setDisplayMode (int width) (int height) (boolean fullscreen?))))))

(def default
  {:size [800 600]
   :title "Untitled"
   :vsync? false
   :fullscreen? false
   :resizable? false})

(def ^:private current-display (atom default))

(defn sync!
  [display]
  (let [existing @current-display
        {:keys [title size fullscreen?]} display]
    (when-not (= (:title existing) title)
      (set-title! title))
    (when-not (and (= (:size existing) size)
                   (= (:fullscreen? existing) fullscreen?))
      (set-display-mode! size fullscreen?))
    (swap! current-display assoc
           :title title
           :size size
           :fullscreen? fullscreen?)))

(defn get-current
  []
  (merge
    @current-display
    (dispatch/on-render-thread
      {:size        [(.. Gdx/app getGraphics getWidth)
                     (.. Gdx/app getGraphics getHeight)]
       :fullscreen? (.. Gdx/app getGraphics isFullscreen)})))
