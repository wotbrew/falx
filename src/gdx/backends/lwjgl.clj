(ns gdx.backends.lwjgl
  (:require [gdx.app :as app]
            [gdx.display :as display])
  (:import (com.badlogic.gdx.backends.lwjgl LwjglApplicationConfiguration LwjglApplication)
           (com.badlogic.gdx ApplicationListener)))

(defn set-display-config-fields!
  [^LwjglApplicationConfiguration o display]
  (let [{:keys [vsync? fullscreen? size title resizable?]} (merge display/default display)
        [width height] size]
    (set! (.-title o) (str title))
    (set! (.-width o) (int width))
    (set! (.-height o) (int height))
    (set! (.-vSyncEnabled o) (boolean vsync?))
    (set! (.-fullscreen o) (boolean fullscreen?))
    (set! (.-resizable o) (boolean resizable?))
    o))

(defn set-config-fields!
  [^LwjglApplicationConfiguration o app]
  (let [{:keys [display max-foreground-fps max-background-fps]} (merge app/default app)]
    (set-display-config-fields! o display)
    (set! (.-foregroundFPS o) (int max-foreground-fps))
    (set! (.-backgroundFPS o) (int max-background-fps))
    o))

(defn map->lwjgl-configuration
  [m]
  (doto (LwjglApplicationConfiguration.)
    (set-config-fields! m)))

(defmethod app/start!* :application/lwjgl
  [app]
  (LwjglApplication.
    ^ApplicationListener app/listener-proxy
    ^LwjglApplicationConfiguration (map->lwjgl-configuration app)))
