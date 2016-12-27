(ns falx.core
  (:require [falx.gdx :as gdx]))

(gdx/on-tick render-ui
  [tick]
  (gdx/draw! "hello world"))

(defn init!
  [])