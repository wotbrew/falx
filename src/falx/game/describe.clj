(ns falx.game.describe
  (:require [falx.event :as event]
            [falx.state :as state]
            [falx.game.focus :as focus]
            [clojure.pprint :as pp]))

(event/defhandler
  [:event.action :action.hit/describe]
  ::describe
  (fn [_]
    (let [g (state/get-game)
          ts (focus/get-all-things g)]
      (run! pp/pprint ts))))