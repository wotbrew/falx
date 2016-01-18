(ns falx.input
  (:require [clj-gdx :as gdx]))

(def default
  {:keyboard gdx/default-keyboard
   :mouse gdx/default-mouse
   :actions #{}})

(defn get-keyboard-state
  "Returns the current keyboard state (requires game to be running)"
  []
  @gdx/keyboard-state)

(defn get-mouse-state
  "Returns the current mouse state"
  []
  @gdx/mouse-state)

(def temp-bindings**
  "A set of temporary bindings, will be externalised and passed as part of the game..."
  {:action.hit/select #{[:mouse :left]}

   :action.pressed/lasso #{[:mouse :left]}

   :action.hit/move #{[:mouse :left]}

   :action.hit/attack #{[:mouse :left]}

   :action.hit/describe #{[:mouse :right]}

   :action.pressed/cam-up #{[:keyboard :w]}
   :action.pressed/cam-left #{[:keyboard :a]}
   :action.pressed/cam-down #{[:keyboard :s]}
   :action.pressed/cam-right #{[:keyboard :d]}

   :action.pressed/modifier #{[:keyboard :shift-left]
                              [:keyboard :shift-right]}})


(defn- action-active?
  ([action buttons mouse keyboard]
   (let [type (namespace action)
         mh? (:hit mouse #{})
         mp? (:pressed mouse #{})
         kh? (:hit keyboard #{})
         kp? (:pressed keyboard #{})]
     (some
       (fn [[kind button]]
         (case type
           "action.hit"
           (case kind
             :mouse (mh? button)
             :keyboard (kh? button))
           "action.pressed"
           (case kind
             :mouse (mp? button)
             :keyboard (kp? button))))
       buttons))))

(defn get-actions
  "Returns the current set of active actions for the given mouse and keyboard."
  [bindings mouse keyboard]
  (let [xform (comp
                (filter #(action-active? (key %)
                                         (val %)
                                         mouse keyboard))
                (map key))]
    (->> bindings
         (into #{} xform))))

(defn get-input-state
  "Returns the current input state including any active actions."
  [bindings]
  (let [keyboard (get-keyboard-state)
        mouse (get-mouse-state)]
    {:keyboard keyboard
     :mouse mouse
     :actions (get-actions bindings mouse keyboard)}))

(defn doing?
  "Is the given `action` being performed?."
  [input action]
  (contains? (:actions input) action))

(defn modified?
  "Is the modifier key/button down?"
  [input]
  (doing? input :action.pressed/modifier))

(defn get-mouse-point
  "Returns the mouse point in screen co-ordinates."
  [input]
  (-> input :mouse :point))

(defn get-action-event
  "Returns an action event for the given action"
  [action input]
  {:type   [:event.action action]
   :action action
   :input  input})

(defn get-input-events
  "Returns a coll of input events for the given input"
  [input]
  (map #(get-action-event % input) (:actions input)))
