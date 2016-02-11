(ns falx.ui
  (:require [falx.react :as react]
            [falx.rect :as rect])
  (:refer-clojure :exclude [empty]))

(def empty
  {:reactions {}
   :events []
   :selected-screen :screen/main})

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
  [ui id]
  (-> ui :state (get (:id id))))

(defn update-state
  ([ui id f]
   (update-in ui [:state id] f))
  ([ui id f & args]
   (update-state ui id #(apply f % args))))

(defmulti update-ui (fn [ui widget frame prect] (:type widget)))

(defmethod update-ui :default
  [ui _ _ prect]
  ui)

(defn select-screen
  [ui screen-id]
  (assoc ui :selected-screen screen-id
            :previous-screen (or (:selected-screen ui)
                                 screen-id)))

(defn revert-screen
  [ui]
  (select-screen ui (:previous-screen ui)))