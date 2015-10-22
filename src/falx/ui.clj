(ns falx.ui
  (:require [falx.ui.screens.main :as main]
            [falx.ui.widgets :as widgets]
            [falx.event :as event]
            [falx.size :as size]
            [falx.ui.screens
             [continue]
             [main]
             [new]
             [roster]
             [settings]]))

(def default {::screen (widgets/static-text "You should never see this" 0 0)
              ::size   size/default})

(defonce ui (atom default))

(defn change-screen
  [ui screen]
  (assoc ui ::screen screen))

(def get-screen ::screen)

(defn update!
  [f & args]
  (swap! ui #(apply f % args)))

(defn update-state!
  [f & args]
  (update! #(update ::state f args)))

(def draw! widgets/draw!)

(def get-input-events widgets/get-input-events)

(event/register-handler!
  :event/change-screen
  :change-screen
  (fn [event]
    (update! change-screen (:screen event))))

(event/register-handler!
  :event/update-ui-state
  :update-ui-state
  (fn [event]
    (update-state! (:f event))))

(defn init!
  [size]
  (update! #(-> %
                (assoc ::size size)
                (change-screen (main/main size))))
  nil)