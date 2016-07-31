(ns falx.gdx.display
  (:require [falx.gdx.impl.dispatch :as dispatch])
  (:import (com.badlogic.gdx Gdx)
           (org.lwjgl.opengl GL11)))

(defn set-title!
  [s]
  (dispatch/dispatch
    (.. Gdx/app getGraphics (setTitle (str s)))))

(defn set-display-mode!
  ([size opts]
   (let [[w h] size
         {:keys [fullscreen?]} opts]
     (dispatch/dispatch
       (.. Gdx/app getGraphics (setDisplayMode (int w) (int h) (boolean fullscreen?)))))))

(defn size
  []
  [(.. Gdx/app getGraphics getWidth)
   (.. Gdx/app getGraphics getHeight)])

(defn fullscreen?
  []
  (.. Gdx/app getGraphics isFullscreen))

(defn clear!
  []
  (GL11/glClearColor 0 0 0 0)
  (GL11/glClear GL11/GL_COLOR_BUFFER_BIT))