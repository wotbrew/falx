(ns falx.graphics.screen
  (:require [falx.application :as app])
  (:import (org.lwjgl.opengl GL20 GL11)
           (com.badlogic.gdx Gdx)))

(defn clear!
  []
  (GL11/glClearColor 0 0 0 0)
  (GL11/glClear GL11/GL_COLOR_BUFFER_BIT))

(defn get-size
  []
  [(.getWidth Gdx/graphics)
   (.getHeight Gdx/graphics)])

(defn fullscreen?
  []
  (.isFullscreen Gdx/graphics))

(defn set-size!
  ([size]
    (let [[w h] size]
      (set-size! w h)))
  ([width height]
   (app/on-render-thread
     (.setDisplayMode Gdx/graphics width height (fullscreen?)))))


(defn set-title!
  [title]
  (app/on-render-thread
    (.setTitle Gdx/graphics (str title))))
