(ns falx.event)

(defn display-changed
  [old-display display]
  {:type :event/display-changed
   :old-display old-display
   :display display
   :debug? true})

(defn input-changed
  [old-input input]
  {:type :event/input-changed
   :old-input old-input
   :input input})

(defn mouse-changed
  [old-mouse mouse]
  {:type :event/mouse-changed
   :old-mouse old-mouse
   :mouse mouse})

(defn keyboard-changed
  [old-keyboard keyboard]
  {:type :event/keyboard-changed
   :old-keyboard old-keyboard
   :keyboard keyboard})

(defn key-hit
  [key]
  {:type :event/key-hit
   :key key
   :debug? true})

(defn button-hit
  [button]
  {:type :event/button-hit
   :button button
   :debug? true})

(defn multi
  [coll]
  {:type :event/multi
   :events coll})

(defn setting-changed
  [k old-value v]
  (multi
    [{:type      :event/setting-changed
      :setting   k
      :old-value old-value
      :value     v}
     {:type      [:event/setting-changed k]
      :setting   k
      :old-value old-value
      :value     v}]))

(defn pos-changed
  [old-actor actor]
  {:type :event/pos-changed
   :old-actor old-actor
   :actor actor
   :debug? true})

(defn pos-removed
  [old-actor actor]
  {:type :event/pos-removed
   :old-actor old-actor
   :actor actor
   :debug? true})