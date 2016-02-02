(ns falx.ui
  (:require [falx.react :as react]
            [falx.ui.widget :as widget])
  (:refer-clojure :exclude [empty]))

(def empty
  {:reactions {}
   :events []
   :screen {}})

(defn ui
  [reactions]
  (assoc empty :reactions (react/react-map reactions)))

(defn publish
  [ui event]
  (let [ui' (react/react ui (:reactions ui) event)]
    (update ui' :events conj event)))

(defmulti screen (fn [ui frame] (:selected-screen ui)))

(defmethod screen :default
  [ui frame]
  {})

(defn get-state
  [ui widget]
  (-> ui :widgets (get (:id widget))))

(defn update-state
  ([ui widget f]
   (update-in ui [:widgets (:id widget)] f))
  ([ui widget f & args]
   (update-state ui widget #(apply f % args))))

(defn handle-frame
  [ui frame]
  (let [nui (widget/handle-frame ui (:screen ui) frame)]
    (if (identical? ui nui)
      ui
      (assoc ui :screen (screen ui (:world frame))))))