(ns falx.game.click
  (:require [gdx.camera :as camera]
            [falx.position :as pos]
            [clojure.core.async :as async]
            [clojure.tools.logging :refer [debug]]
            [falx.game :as game]
            [falx.world :as world]))

(defn game-view-clicked->world-clicked
  [{:keys [camera point level cell-width cell-height]}]
  (let [[x y] (camera/get-world-point camera point)]
    {:type :ui.event/world-clicked
     :cell (pos/cell [(int (/ x cell-width))
                      (int (/ y cell-height))]
                     level)}))

(defn world-clicked->actor-clicked-coll
  [world {:keys [cell]}]
  (for [a (world/query-actors world :cell cell)]
    {:type :ui.event/actor-clicked
     :id (:id a)
     :actor a}))

(defn install!
  [game]
  (debug "Installing click module")
  (-> game
      (game/install-event-fn-blocking
        :ui.event/game-view-clicked
        game-view-clicked->world-clicked)
      (game/install-event-xform
        :ui.event/world-clicked
        (mapcat #(world-clicked->actor-clicked-coll (game/get-world game) %)))))