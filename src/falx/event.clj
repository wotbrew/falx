(ns falx.event)

(defn multi
  [coll]
  {:type :event/multi
   :events coll})

(defn multi?
  [event]
  (= (:type event) :event/multi))

(defn display-changed
  [old-display display]
  {:type :event/display-changed
   :old-display old-display
   :display display})

(defn screen-size-changed
  [old-size size]
  {:type :event/screen-size-changed
   :old-size old-size
   :size size
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
  (multi
    [{:type :event/key-hit
      :key  key}
     {:type [:event/key-hit key]
      :key key}]))

(defn button-hit
  [button]
  (multi
    [{:type   :event/button-hit
      :button button}
     {:type [:event/button-hit button]
      :button button}]))

(defn world-clicked
  [point]
  {:type :event/world-clicked
   :point point})

(defn cell-clicked
  [cell]
  {:type :event/cell-clicked
   :cell cell})

(defn actor-clicked
  [actor]
  (multi
    [{:type :event/actor-clicked
      :actor actor}
     {:type [:event/actor-clicked (:type actor)]
      :actor actor}]))

(defn key-pressed
  [key]
  (multi
    [{:type :event/key-pressed
      :key  key}
     {:type [:event/key-pressed key]
      :key  key}]))

(defn button-pressed
  [button]
  (multi
    [{:type   :event/button-pressed
      :button button}
     {:type   [:event/button-pressed button]
      :button button}]))

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

(defn frame
  [frame]
  {:type :event/frame
   :frame frame})

(defn input
  [input]
  {:type :event/input
   :input input})