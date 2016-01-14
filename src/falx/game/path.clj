(ns falx.game.path
  (:require [falx.point :as point]
            [falx.event :as event]
            [falx.game.solid :as solid]
            [falx.game.selection :as selection]
            [falx.state :as state]
            [falx.game.focus :as focus]))

(defn get-pred
  [world level]
  (complement (solid/get-solid-point-pred world level)))

(defn get-path
  [world level point-a point-b]
  (seq (rest (point/get-a*-path (get-pred world level) point-a point-b))))

;; =====
;; PATH PREVIEW

(defn get-path-preview
  [game to]
  (let [{:keys [world level]} game
        fselected (first (filter (comp #{level} :level) (selection/get-selected world)))
        point-a (:point fselected)]
    (when (and fselected point-a)
      (get-path world level point-a to))))

(defn refresh-path-preview!
  [to]
  (if-some [preview (get-path-preview (state/get-game) to)]
    (state/update-game! assoc :path-preview preview
                        :path-preview-points (set preview))
    (state/update-game! dissoc :path-preview
                        ::path-preview-points)))

(event/defhandler-async
  :event.focus/point-changed
  ::point-changed
  (fn [{:keys [point]}]
    (refresh-path-preview! point)))

(event/defhandler-async
  :event.selection/changed
  ::selection-changed
  (fn [_]
    (refresh-path-preview! (focus/get-point (state/get-game)))))

(event/defhandler-async
  :event.thing/put
  ::thing-put
  (fn [{:keys [cell]}]
    (let [game (state/get-game)]
      (when (contains? (::path-preview-points game) (:point cell))
        (refresh-path-preview! (focus/get-point game))))))

(event/defhandler-async
  :event.thing/unput
  ::thing-put
  (fn [{:keys [cell]}]
    (let [game (state/get-game)]
      (when (contains? (::path-preview-points game) (:point cell))
        (refresh-path-preview! (focus/get-point game))))))

(event/defhandler-async
  :event.selection/put
  ::selection-put
  (fn [_]
    (refresh-path-preview! (focus/get-point (state/get-game)))))

(event/defhandler-async
  :event.selection/unput
  ::selection-unput
  (fn [_]
    (refresh-path-preview! (focus/get-point (state/get-game)))))