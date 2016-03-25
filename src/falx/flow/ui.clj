(ns falx.flow.ui
  (:require [falx.game :as game]
            [clojure.core.async :as async]
            [falx.ui :as ui]
            [falx.creature :as creature]))

(defn get-world-clicks-chan
  [game]
  (let [c (game/sub game [:ui.event/clicked :ui/game-view])
        out (async/chan)
        xform (mapcat #(ui/get-world-clicked-events (:element %)
                                                    (:point %)
                                                    (:button %)))]
    (async/pipeline-blocking 1 out xform c)
    out))

(defn get-actor-clicks-chan
  [game]
  (-> (game/sub game :ui.event/world-clicked)
      (async/pipe (async/chan 32
                              (mapcat (fn [{:keys [cell button]}]
                                        (for [a (game/get-at game cell)
                                              e (ui/get-actor-clicked-events a button)]
                                          e)))))))

(defn get-select-request-chan
  [game]
  (game/sub! game
             [:ui.event/actor-clicked :actor.type/creature :left]
             (async/chan 32 (map (fn [{:keys [actor]}]
                                   {:type  :request/select
                                    :actor actor})))))

(defn get-move-selected-request-chan
  [game]
  (game/sub! game
             [:ui.event/world-clicked :left]
             (async/chan 32 (keep (fn [{:keys [cell]}]
                                    (when-not (game/solid-at? game cell)
                                      {:type :request/move-selected
                                       :cell cell}))))))

(defn get-move-request-chan
  [game]
  (game/sub! game
             :request/move-selected
             (async/chan 32 (mapcat (fn [{:keys [cell]}]
                                      (for [a (game/query-actors game :selected? true)
                                            :when (not= cell (:cell a))]
                                        {:type :request/move
                                         :actor a
                                         :cell cell}))))))

(defn install!
  [game]
  (doto game
    (game/plug! (get-world-clicks-chan game)
                (get-actor-clicks-chan game)
                (get-select-request-chan game)
                (get-move-selected-request-chan game)
                (get-move-request-chan game))

    (game/subfn!
      :request/select
      #(game/update-actor! game (:id (:actor %)) creature/toggle-select))

    (game/subfn!
      :request/move
      #(game/update-actor! game (:id (:actor %)) creature/move (:cell %)))))