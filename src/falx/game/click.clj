(ns falx.game.click
  (:require [gdx.camera :as camera]
            [falx.position :as pos]
            [clojure.core.async :as async]
            [clojure.tools.logging :refer [debug]]))

(defn game-view-clicked->world-clicked
  [{:keys [camera point level cell-width cell-height] :as event}]
  (prn event)
  (when event
    (let [[x y] (camera/get-world-point camera point)]
      {:type :ui.event/world-clicked
       :cell (pos/cell [(int (/ x cell-width))
                        (int (/ y cell-height))]
                       level)})))

(defn install!
  [game]
  (debug "Installing click module")
  (let [event-pub (:event-pub game)
        event-chan (:event-chan game)
        c (async/chan)]
    (async/sub event-pub :ui.event/game-view-clicked c)
    (async/pipeline-blocking 1 event-chan (map game-view-clicked->world-clicked) c)
    game))