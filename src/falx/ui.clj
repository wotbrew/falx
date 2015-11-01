(ns falx.ui
  (:require [falx.ui.widgets :as widgets]
            [falx.event :as event]
            [falx.size :as size]))

(def default {::screen ::warning
              ::size   size/default})

(def get-screen ::screen)

(def get-size ::size)

(defonce ui (atom default))

(defn change-screen
  [ui screen]
  (assoc ui ::screen screen))

(defn update!
  [f & args]
  (swap! ui #(apply f % args)))

(defn update-state!
  [f & args]
  (update! #(update ::state f args)))

(def draw! widgets/draw!)

(def get-input-events widgets/get-input-events)

(defn change-screen!
  [screen]
  (update! change-screen screen))

(defn set-size!
  [size]
  (update! assoc ::size size))

(defmulti get-screen-widget ::screen)

(defmethod get-screen-widget :default
  [ui]
  (widgets/static-text (str "Undefined screen: " (::screen ui))))

(defn get-ui
  []
  @ui)

(defn get-current-screen-widget
  []
  (get-screen-widget (get-ui)))

(defn change-screen-event
  [screen]
  {:event/type :event/change-screen
   :screen screen})

(event/register-handler!
  :event/change-screen
  :change-screen
  (fn [event]
    (change-screen! (:screen event))))
