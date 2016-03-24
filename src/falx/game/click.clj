(ns falx.game.click
  (:require [gdx.camera :as camera]
            [falx.position :as pos]
            [clojure.tools.logging :refer [debug]]
            [falx.game :as game]
            [falx.world :as world]))

(defn game-view-clicked->world-clicked-coll
  [{:keys [camera point button level cell-width cell-height]}]
  (let [[x y] (camera/get-world-point camera point)
        cell (pos/cell [(int (/ x cell-width))
                        (int (/ y cell-height))]
                       level)]
    [{:type   :ui.event/world-clicked
      :button button
      :cell   cell}
     {:type   [:ui.event/world-clicked button]
      :button button
      :cell   cell}]))

(defn actor-click-events
  [actor button]
  [{:type  :ui.event/actor-clicked
    :id    (:id actor)
    :button button
    :actor actor}
   {:type   [:ui.event/actor-clicked button]
    :id     (:id actor)
    :button button
    :actor  actor}
   {:type [:ui.event/actor-clicked (:type actor)]
    :id (:id actor)
    :button button
    :actor actor}
   {:type [:ui.event/actor-clicked (:type actor) button]
    :id (:id actor)
    :button button
    :actor actor}])

(defn world-clicked->actor-clicked-coll
  [world {:keys [cell button]}]
  (let [as (world/query-actors world :cell cell)]
    (mapcat #(actor-click-events % button) as)))

(defn install!
  [game]
  (debug "Installing click module")
  (-> game
      (game/install-event-xform-blocking
        :ui.event/game-view-clicked
        (mapcat #'game-view-clicked->world-clicked-coll))
      (game/install-event-xform
        :ui.event/world-clicked
        (mapcat #(world-clicked->actor-clicked-coll (game/get-world game) %)))))