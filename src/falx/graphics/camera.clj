(ns falx.graphics.camera
  (:require [falx.graphics.screen :as screen]
            [falx.application :as app])
  (:import (com.badlogic.gdx.graphics OrthographicCamera)
           (com.badlogic.gdx.graphics.g2d SpriteBatch)))

(defn camera
  ([[w h]]
    (camera w h))
  ([width height]
   (doto (OrthographicCamera.)
     (.setToOrtho true width height))))

(def game-camera (delay (camera (screen/get-size))))

(def ui-camera (delay (camera (screen/get-size))))

(defn set-size!
  ([[w h]]
   (set-size! w h))
  ([width height]
   (app/on-render-thread
     (.setToOrtho @game-camera true width height)
     (.setToOrtho @ui-camera true width height))))

(defn get-combined-matrix
  [camera]
  (.combined camera))

(defn use-camera!
  [camera]
  (.update camera)
  (let [^SpriteBatch batch @app/sprite-batch]
    (.setProjectionMatrix batch (get-combined-matrix camera))))

(defn use-game-camera!
  []
  (use-camera! @game-camera))

(defn use-ui-camera!
  []
  (use-camera! @ui-camera))