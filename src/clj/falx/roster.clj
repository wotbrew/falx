(ns falx.roster
  (:require [falx.ui :as ui]
            [falx.gdx :as gdx]
            [falx.util :as util]
            [falx.game-state :as gs]
            [falx.character :as char]
            [falx.inventory :as inv]
            [falx.game :as g]
            [clojure.java.io :as io]
            [clojure.core.memoize :as memo])
  (:import (com.badlogic.gdx.graphics Color)
           (com.badlogic.gdx Input$Keys)))

(defmethod ui/scene-name :roster [_] "Roster")

(def in-play-icon
  (gdx/texture-region ui/misc 64 0 32 32))

(defn selected-id
  [gs]
  (-> gs :ui :roster :selected))

(defn selected
  [gs]
  (->> gs selected-id (gs/entity gs)))

(def cscale 64)

(defn select
  [gs id]
  (assoc-in gs [:ui :roster :selected] id))

(def can-delete? (complement :in-play?))

(defn can-delete-id?
  [gs id]
  (when-some [e (gs/entity gs id)]
    (can-delete? e)))

(defn delete
  [gs id]
  (let [selected (selected-id gs)]
    (if-not (can-delete-id? gs id)
      gs
      (-> gs
          (update :roster (partial into [] (remove #{id})))
          (cond->
            (= selected id)
            (-> (gs/del id)
                (util/dissoc-in [:ui :roster :selected])))))))

(defn delete-selected
  [gs]
  (if-some [selected (selected-id gs)]
    (delete gs selected)
    gs))

(defn delete-handler
  [id]
  (ui/if-elem (ui/key-combo-pred
                Input$Keys/D
                (ui/down Input$Keys/SHIFT_LEFT))
    (ui/gs-behaviour delete id)))

(defn character-el*
  [m]
  (->
    (ui/stack
      (ui/if-elem (ui/gs-pred (comp #{(:id m)} selected-id))
        (ui/tint Color/GREEN ui/selection-circle))
      (let [img (ui/stack (char/body-drawable m)
                          (if (:in-play? m)
                            (ui/translate 48 3
                              (ui/resize 32 32 in-play-icon))
                            ui/nil-elem))]
        (ui/if-hovering
          (ui/stack
            (delete-handler (:id m))
            img)
          (ui/if-elem (ui/gs-pred (comp #{(:id m)} selected-id))
            (ui/tint ui/vlight-gray img)
            (ui/tint Color/GRAY img))))
      (ui/if-debug (str (:id m))))
    (ui/wrap-opts
      {:on-click   [select (:id m)]
       :hover-over (:name m)})))

(def character-el
  (memo/lru character-el* :lru/threshold 256))

(def up-arrow
  (gdx/texture-region ui/gui 0 32 32 32))

(def down-arrow
  (gdx/texture-region ui/gui 32 32 32 32))

(def character-array
  (let [scroll-pos (volatile! 0)
        click-handled? (volatile! false)]
    (ui/stack
      (ui/fancy-box 1)
      (ui/pad 28 3
        (ui/dynamic
          (fn [{{:keys [roster] :as gs} :state} x y w h]
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
                           (map (partial gs/entity gs)))
                         (map #(ui/resize
                                cscale cscale
                                (ui/stack
                                  (character-el %)
                                  (ui/click-handler
                                    (fn [_]
                                      (vreset! click-handled? true))))))
                         roster)]
              (when (not= spos2 spos)
                (vreset! scroll-pos spos2))
              (apply ui/flow ents)))))
      (ui/hug #{:right}
        (ui/pad -6 4 (ui/resize 28 28
                       (ui/link up-arrow
                                :on-click!
                                (fn [_]
                                  (vreset! click-handled? true)
                                  (vswap! scroll-pos (fn [n] (max 0 (dec n)))))))))
      (ui/hug #{:bottom :right}
        (ui/pad -6 0 (ui/resize 28 28
                       (ui/link down-arrow
                                :on-click!
                                (fn [{{:keys [roster]} :state}]
                                  (vreset! click-handled? true)
                                  (vswap! scroll-pos inc))))))
      (ui/click-handler
        (fn [frame]
          (when-not @click-handled?
            (g/update-state! (:game frame) util/dissoc-in [:ui :roster :selected]))
          (vreset! click-handled? false))))))

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

(defn select-sort-type
  [gs]
  gs)

(def sort-cycler
  (ui/cycler
    (comp sort-str selected-sort-type)
    select-sort-type
    prev-sort-type
    next-sort-type))

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
  (ui/if-elem (ui/gs-pred
                (fn [gs]
                  (when-some [e (selected gs)]
                    (:in-play? e))))
    (ui/button "Continue")
    (ui/disabled-button "Continue")))

(def delete-button
  (ui/if-elem (ui/gs-pred
                (fn [gs]
                  (when-some [e (selected gs)]
                    (can-delete? e))))
    (ui/button "Delete" :on-click delete-selected)
    (ui/disabled-button "Delete")))

(def stats-button
  (let [bcontents (ui/stack
                    (ui/translate 3 0 (ui/resize 32 32 char/icon))
                    (ui/center (ui/translate 8 0 "Stats")))]
    (ui/if-elem (ui/gs-pred selected-id)
      (ui/button bcontents :on-click [ui/goto :stats])
      (ui/disabled-button bcontents))))

(def inventory-button
  (let [bcontents (ui/stack
                    (ui/translate 3 0 (ui/resize 32 32 inv/icon))
                    (ui/center (ui/translate 8 0 "Inventory")))]
    (ui/if-elem (ui/gs-pred selected-id)
      (ui/button bcontents :on-click [ui/goto :inventory])
      (ui/disabled-button bcontents))))

(def names
  (with-open [rdr (io/reader (io/resource "names.txt"))]
    (vec (line-seq rdr))))

(defn create
  [gs]
  (let [[id gs] (gs/next-id gs)
        body (char/genbody)]
    (-> gs
        (gs/add id (merge body
                          {:id      id
                           :name (rand-nth names)
                           :editable? true
                           :player? true}))
        (select id))))

(def character-opts
  (ui/cols
    (ui/button "Create"
      :on-click create
      :hover-over "Create a new character")
    continue-button
    stats-button
    inventory-button
    delete-button))

(def character-details
  (ui/center
    (ui/if-debug
      (ui/gs-dynamic
        (comp util/pprint-str selected)))))

(ui/defscene :roster
  ui/back-handler
  ui/breadcrumbs
  (ui/pad 32 50
    (ui/stack
      (ui/restrict-height 32 controls)
      (ui/pad 0 48
        (ui/if-elem (ui/gs-pred selected-id)
          (ui/rows
            character-array
            (ui/pad 0 3
              (ui/fancy-box 1)
              character-details))
          character-array))
      (ui/hug #{:bottom}
        (ui/restrict-height 32
          character-opts))))
  ui/hover-over
  (ui/at-mouse
    (ui/resize  32 32 ui/mouse-pointer)))