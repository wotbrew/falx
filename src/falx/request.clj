(ns falx.request)

(defn in-ms
  [msg time-ms]
  {:type :request/in-ms
   :message msg
   :ms time-ms})

(defn give-goal
  [actor goal]
  {:type :request/give-goal
   :actor actor
   :goal goal})

(defn remove-goal
  [actor goal]
  {:type :request/remove-goal
   :actor actor
   :goal goal})

(defn step
  [actor cell]
  {:type :request/step
   :actor actor
   :cell cell})

(defn spawn-ai
  [actor]
  {:type :request/spawn-ai
   :actor actor})

(defn tick-ai
  [actor event]
  {:type  :request/tick-ai
   :actor actor
   :event event})

(defn print-message
  [msg]
  {:type :request/print-message
   :message msg})

(defn print-actor
  [actor]
  {:type :request/print-actor
   :actor actor})

(defn select-actor
  [actor]
  {:type :request/select-actor
   :actor actor})

(defn select-only-actor
  [actor]
  {:type :request/select-only-actor
   :actor actor})

(defn unselect-actor
  [actor]
  {:type :request/unselect-actor
   :actor actor})

(defn toggle-actor-selection
  [actor]
  {:type :request/toggle-actor-selection
   :actor actor})