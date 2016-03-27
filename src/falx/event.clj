(ns falx.event)

(defn multi
  [events]
  {:type :event/multi
   :events events})

;; Actors

(defn actor-put
  [actor old-cell cell]
  {:type :event/actor-put
   :actor actor
   :old-cell old-cell
   :cell cell})

(defn actor-unput
  [actor cell]
  {:type :event/actor-unput
   :actor actor
   :cell cell})

(defn actor-goal-removed
  [actor goal]
  (multi
    [{:type  :event/actor-goal-removed
      :actor actor
      :goal  goal}
     {:type  [:event/actor-goal-removed (:type goal)]
      :actor actor
      :goal  goal}]))

(defn actor-goal-added
  [actor goal]
  (multi
    [{:type  :event/actor-goal-added
      :actor actor
      :goal  goal}
     {:type  [:event/actor-goal-added (:type goal)]
      :actor actor
      :goal  goal}]))

(defn actor-clicked
  [actor button]
  (multi
    [{:type  :event/actor-clicked
      :button button
      :actor actor}
     {:type   [:event/actor-clicked button]
      :button button
      :actor  actor}
     {:type [:event/actor-clicked (:type actor)]
      :button button
      :actor actor}
     {:type [:event/actor-clicked (:type actor) button]
      :button button
      :actor actor}]))

(defn actor-created
  [actor]
  (multi
    [{:type  :event/actor-created
      :actor actor}
     {:type  [:event/actor-created (:type actor)]
      :actor actor}]))

(defn actor-changed
  [old-actor actor]
  (multi
    [{:type      :event/actor-changed
      :old-actor old-actor
      :actor     actor}
     {:type      [:event/actor-changed (:type actor)]
      :old-actor old-actor
      :actor     actor}]))

(defn actor-selected
  [actor]
  {:type :event/actor-selected
   :actor actor})

(defn actor-unselected
  [actor]
  {:type :event/actor-unselected
   :actor actor})

;; Game

(defn game-closing
  [game]
  {:type :event/game-closing
   :id (:id game)})

(defn game-starting
  [game]
  {:type :event/game-starting
   :id (:id game)})

(defn game-started
  [game]
  {:type :event/game-started
   :id (:id game)})

(defn frame
  [frame]
  {:type :event/frame
   :frame frame})

;; World

(defn world-clicked
  [cell button]
  (multi
    [{:type   :event/world-clicked
      :button button
      :cell   cell}
     {:type   [:event/world-clicked button]
      :button button
      :cell   cell}]))

;; UI

(defn ui-clicked
  [element button point]
  {:type [:event/ui-clicked (:type element)]
   :button button
   :point point
   :element element})

(defn ui-hover-enter
  [element point]
  {:type [:event/ui-hover-enter (:type element)]
   :element element
   :point point})

(defn ui-hover-exit
  [element point]
  {:type [:event/ui-hover-exit (:type element)]
   :element element
   :point point})