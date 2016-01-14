(ns falx.game.path
  (:require [falx.point :as point]
            [falx.event :as event]
            [falx.game.solid :as solid]
            [falx.game.selection :as selection]
            [falx.state :as state]
            [falx.game.focus :as focus]
            [falx.location :as location]))

(defn get-pred
  [world level]
  (complement (solid/get-solid-point-pred world level)))

(defn get-path*
  [world level point-a point-b]
  (seq (rest (point/get-a*-path (get-pred world level) point-a point-b))))

(defn get-path
  [world cell-a cell-b]
  (when (= (:level cell-a) (:level cell-b))
    (->> (get-path* world (:level cell-a) (:point cell-a) (:point cell-b))
         (map #(location/cell (:level cell-a) %))
         seq)))

;; =====
;; PATH PREVIEW

(defn get-path-preview
  [game cell]
  (let [{:keys [world level]} game
        fselected (first (selection/get-selected-in-level world level))]
    (when (and fselected (:cell fselected))
      (get-path world (:cell fselected) cell))))

(defn refresh-path-preview!
  [cell]
  (if-some [preview (get-path-preview (state/get-game) cell)]
   (state/update-game! assoc
                       :path-preview preview
                       :path-preview-cells (set preview))
   (state/update-game! dissoc
                       :path-preview
                       ::path-preview-cells)))

(event/defhandler-async
  :event.focus/cell-changed
  ::cell-changed
  (fn [{:keys [cell]}]
    (refresh-path-preview! cell)))

(event/defhandler-async
  :event.selection/changed
  ::selection-changed
  (fn [_]
    (refresh-path-preview! (focus/get-cell (state/get-game)))))

(event/defhandler-async
  :event.thing/put
  ::thing-put
  (fn [{:keys [cell]}]
    (let [game (state/get-game)]
      (when (contains? (::path-preview-points game) cell)
        (refresh-path-preview! (focus/get-cell game))))))

(event/defhandler-async
  :event.thing/unput
  ::thing-put
  (fn [{:keys [cell]}]
    (let [game (state/get-game)]
      (when (contains? (::path-preview-points game) cell)
        (refresh-path-preview! (focus/get-cell game))))))

(event/defhandler-async
  :event.selection/put
  ::selection-put
  (fn [_]
    (refresh-path-preview! (focus/get-cell (state/get-game)))))

(event/defhandler-async
  :event.selection/unput
  ::selection-unput
  (fn [_]
    (refresh-path-preview! (focus/get-cell (state/get-game)))))