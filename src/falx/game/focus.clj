(ns falx.game.focus
  (:require [falx.game.camera :as camera]
            [falx.world :as world]
            [falx.thing.creature :as creature]
            [falx.util :as util]
            [falx.location :as location]
            [falx.game :as game]
            [falx.event :as event]))

(defn get-point
  "Get the focused point"
  [game]
  (camera/get-world-mouse-level-point game))

(defn get-cell
  "Get the focused cell"
  [game]
  (location/cell (:level game) (get-point game)))

(defn get-all-things
  "Get all focused things (those things under the mouse)"
  [game]
  (let [{:keys [world]} game
        cell (get-cell game)]
    (world/get-things-by-value world :cell cell)))

(defn get-creature
  "Returns the focused creature, nil if none"
  [game]
  (util/find-first creature/creature? (get-all-things game)))

(game/defreaction
  :event.game/frame
  ::frame
  (fn [game _]
    (let [point (get-point game)
          cell (get-cell game)
          existing-point (::focused-point game)
          existing-cell (::focused-cell game)]
      (cond->
        (assoc game ::focused-point point
                    ::focused-cell cell)
        (not= point existing-point)
        (game/publish-event {:type :event.focus/point-changed
                             :previous existing-point
                             :point point})
        (not= cell existing-cell)
        (game/publish-event {:type :event.focus/cell-changed
                             :previous existing-cell
                             :cell cell})))))