(ns falx.roster
  (:require [falx.ui :as ui]
            [falx.gdx :as gdx]
            [clojure.java.io :as io]
            [falx.util :as util]
            [falx.state :as state])
  (:import (java.util UUID)
           (com.badlogic.gdx.graphics Color)))

(defmethod ui/scene-name :roster [_] "Roster")

(def human (gdx/texture (io/resource "tiles/human.png")))
(def human-female (gdx/texture-region human 0 0 32 32))
(def human-male (gdx/texture-region human 32 0 32 32))

(defn selected
  [gs]
  (or (-> gs :ui :roster :selected)
      (peek (:roster gs))))

(def cscale 64)

(defn character-el
  [m]
  (->
    (ui/resize
      cscale cscale
      (ui/stack
        (ui/if-elem (ui/gs-pred (comp #{(:id m)} selected))
          (ui/tint Color/GREEN ui/selection-circle))
        human-male))
    (ui/wrap-opts
      {:on-click [assoc-in [:ui :roster :selected] (:id m)]})))

(def up-arrow
  (gdx/texture-region ui/gui 0 32 32 32))

(def down-arrow
  (gdx/texture-region ui/gui 32 32 32 32))

(def character-array
  (let [scroll-pos (volatile! 0)]
    (ui/stack
      (ui/fancy-box 1)
      (ui/hug #{:right}
        (ui/pad -6 4 (ui/resize 28 28
                       (ui/link up-arrow
                                :on-click!
                                (fn [_]
                                  (vswap! scroll-pos (fn [n] (max 0 (dec n)))))))))
      (ui/hug #{:bottom :right}
        (ui/pad -6 0 (ui/resize 28 28
                       (ui/link down-arrow
                                :on-click!
                                (fn [{{:keys [roster]} :game}]
                                  (vswap! scroll-pos inc))))))
      (ui/pad 3 3
        (ui/dynamic
          (fn [{{:keys [entities roster]} :game} x y w h]
            (let [spos @scroll-pos
                  cols (long (/ w cscale))
                  spos2 (if (< (count roster) (* cols spos))
                          (max 0 (dec spos))
                          spos)
                  max-ids (long (* cols (/ h cscale)))
                  skip (max 0 (min (* cols spos2) (count roster) ))
                  ents (eduction
                         (comp
                           (drop skip)
                           (take max-ids)
                           (map (or entities {})))
                         (map character-el)
                         roster)]
              (when (not= spos2 spos)
                (vreset! scroll-pos spos2))
              (apply ui/flow ents))))))))

(defn sort-str
  [x]
  (str x))

(defn prev-sort-type
  [gs])

(defn next-sort-type
  [gs])

(defn selected-sort-type
  [gs]
  "Name (ASC)")

(defn sort-type-up
  [gs]
  gs)

(defn sort-type-down
  [gs]
  gs)

(def sort-cycler
  (ui/stack
    (ui/restrict-width 24 (ui/if-elem (ui/gs-pred prev-sort-type)
                            (ui/button "<" :on-click sort-type-down)
                            (ui/disabled-button "<")))
    (ui/stack
      (ui/fancy-box 2)
      (ui/center
        (ui/gs-text (comp sort-str selected-sort-type))))
    (ui/hug #{:right}
      (ui/restrict-width 24 (ui/if-elem (ui/gs-pred next-sort-type)
                              (ui/button ">" :on-click sort-type-up)
                              (ui/disabled-button ">"))))))

(def controls
  (ui/stack
    (ui/translate 0 8
      "Filter: _____________________")
    (ui/restrict-width 320
      (ui/translate 218 0
        (ui/translate 0 8 "Sorting by: ")
        (ui/pad 80 0 sort-cycler)))
    (ui/hug #{:right}
      (ui/restrict-width 112
        (ui/cols
          (ui/button "Back" :on-click ui/back))))))

(def continue-button
  (ui/disabled-button "Continue"))

(defn delete-selected
  [gs]
  (if-some [selected (selected gs)]
    (-> gs
        (update :roster (partial into [] (remove #{selected})))
        (util/dissoc-in [:entities selected])
        (util/dissoc-in [:ui :roster :selected]))
    gs))

(def delete-button
  (ui/if-elem (ui/gs-pred selected)
    (ui/button "Delete" :on-click delete-selected)
    (ui/disabled-button "Delete")))

(def edit-button
  (ui/if-elem (ui/gs-pred selected)
    (ui/button "Edit")
    (ui/disabled-button "Edit")))

(defn copy-selected
  [gs]
  (let [id (state/entid)
        selected (selected gs)
        entities (:entities gs)]
    (-> gs
        (assoc-in [:ui :roster :selected] id)
        (update :roster (fnil conj []) id)
        (assoc-in [:entities id] (merge (get entities selected) {:id id})))))

(def copy-button
  (ui/if-elem (ui/gs-pred selected)
    (ui/button "Copy" :on-click copy-selected)
    (ui/disabled-button "Copy")))

(defn create
  [gs]
  (let [id (state/entid)]
    (-> gs
        (assoc-in [:ui :roster :selected] id)
        (update :roster (fnil conj []) id)
        (assoc-in [:entities id] {:id id
                                  :player? true}))))

(def character-opts
  (ui/cols
    (ui/button "Create"
      :on-click create)
    copy-button
    continue-button
    edit-button
    delete-button))

(ui/defscene :roster
  ui/back-handler
  ui/breadcrumbs
  (ui/pad 32 50
    (ui/stack
      (ui/restrict-height 32 controls)
      (ui/pad 0 48
        character-array)
      (ui/hug #{:bottom}
        (ui/restrict-height 32
          character-opts)))))