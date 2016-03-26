(ns falx.request)

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

(defn spawn-ai
  [actor]
  {:type :request/spawn-ai
   :actor actor})

(defn tick-ai
  [actor]
  {:type :request/tick-ai
   :actor actor})

(defn print-message
  [msg]
  {:type :request/print-message
   :message msg})

(defn print-actor
  [actor]
  {:type :request/print-actor
   :actor actor})

(defn select-creature
  [actor]
  {:type :request/select-creature
   :actor actor})

(defn unselect-creature
  [actor]
  {:type :request/unselect-creature
   :actor actor})

(defn toggle-creature-selection
  [actor]
  {:type :request/toggle-creature-selection
   :actor actor})