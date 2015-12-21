(ns falx.screen)

(defmulti draw!
  (fn [screen world input frame]
    (:type screen)))

(defmethod draw! :default
  [screen world input frame]
  screen)

(defmulti act
  (fn [screen action]
    (:type action)))

(defmethod act :default
  [screen action]
  screen)

(defmulti get-input-actions
  (fn [screen world input frame]
    (:type screen)))

(defmethod get-input-actions :default
  [screen world input frame]
  [])

(defn publish-event
  [screen event]
  (update screen :events (fnil conj []) event))

(defmethod act :publish-screen-event
  [screen action]
  (publish-event screen (:event action)))