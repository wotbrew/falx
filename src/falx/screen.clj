(ns falx.screen
  (:require [falx.ui.game :as ui-game]))

(def cfn (constantly {}))

(defn screen-fn
  [k]
  (case k
    :game ui-game/game-element
    cfn))

(def screen*
  (let [memappl (memoize #(%1 [0 0 %2 %3]))]
    (fn [k width height]
      (let [f (screen-fn k)]
        (memappl f width height)))))

(defn screen
  [k width height]
  (screen* k width height))