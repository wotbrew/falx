(ns falx.gdx.impl.app
  (:require [falx.gdx.impl.dispatch :as dispatch])
  (:import (com.badlogic.gdx ApplicationListener Gdx Graphics)))

(def on-render-fn (atom nil))
(def on-create-fn (atom nil))
(def on-dispose-fn (atom nil))
(def on-resume-fn (atom nil))
(def on-pause-fn (atom nil))
(def on-resize-fn (atom nil))

(def listener-proxy
  (proxy
    [ApplicationListener]
    []
    (render []
      (when-let [f @on-render-fn]
        (binding [dispatch/*on-render-thread* true]
          (f))))
    (create []
      (when-let [f @on-create-fn]
        (f)))
    (dispose []
      (when-let [f @on-dispose-fn]
        (f)))
    (resume []
      (when-let [f @on-resume-fn]
        (f)))
    (pause []
      (when-let [f @on-pause-fn]
        (f)))
    (resize [width height]
      (when-let [f @on-resize-fn]
        (f [width height])))))