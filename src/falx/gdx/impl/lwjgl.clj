(ns falx.gdx.impl.lwjgl
  (:require [falx.gdx.impl.app :as app])
  (:import (com.badlogic.gdx.backends.lwjgl LwjglApplicationConfiguration LwjglApplication)
           (com.badlogic.gdx ApplicationListener)))

(defn- configure
  ([]
   (configure nil))
  ([opts]
   (let [{:keys [vsync?
                 fullscreen?
                 size
                 title
                 resizable?
                 max-foreground-fps
                 max-background-fps]
          :or {vsync? true
               fullscreen? false
               size [800 600]
               title "Untitled"
               resizable? false
               max-foreground-fps 60
               max-background-fps 60}} opts
         o (LwjglApplicationConfiguration.)
         [w h] size]
     (set! (.-title o) (str title))
     (set! (.-width o) (int w))
     (set! (.-height o) (int h))
     (set! (.-vSyncEnabled o) (boolean vsync?))
     (set! (.-fullscreen o) (boolean fullscreen?))
     (set! (.-resizable o) (boolean resizable?))
     o)))

(defn app
  [opts]
  (LwjglApplication.
    ^ApplicationListener app/listener-proxy
    ^LwjglApplicationConfiguration (configure opts)))