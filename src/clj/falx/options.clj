(ns falx.options
  (:require [falx.ui :as ui]
            [clojure.set :as set]
            [falx.gdx :as gdx]
            [falx.util :as util]
            [falx.state :as state]))

(defn options-changed?
  [gs]
  (some? (-> gs :ui :options)))

(defn discard-changes
  [gs]
  (util/dissoc-in gs [:ui :options]))

(defn apply-changes
  [gs]
  (update gs :settings merge (-> gs :ui :options)) )

(defn apply-options!
  []
  (let [opts (-> @state/game :ui :options)]
    (swap! state/game (comp discard-changes apply-changes))
    (apply
      gdx/configure!
      (apply concat (set/rename-keys opts {:resolution :size})))))

(def default-resolution
  [640 480])

(def available-resolutions
  (sorted-set [640 480]
              [800 600]
              [1024 768]))

(defn active-resolution
  [gs]
  (-> gs :settings :resolution (or default-resolution)))

(defn selected-resolution
  [gs]
  (or (-> gs :ui :options :resolution)
      (active-resolution gs)))

(defn select-resolution
  [gs size]
  (if (= size (active-resolution gs))
    (util/dissoc-in gs [:ui :options :resolution])
    (assoc-in gs [:ui :options :resolution] size)))

(defn prev-resolution
  [gs]
  (first (rsubseq available-resolutions < (selected-resolution gs))))

(defn next-resolution
  [gs]
  (first (subseq available-resolutions > (selected-resolution gs))))

(defn resolution-down
  [gs]
  (if-some [s (prev-resolution gs)]
    (select-resolution gs s)
    gs))

(defn resolution-up
  [gs]
  (if-some [s (next-resolution gs)]
    (select-resolution gs s)
    gs))

(defn resolution-str
  [[w h]]
  (str w " x " h))

(def resolution-cycler
  (ui/stack
    (ui/restrict-width 24 (ui/if-elem
                            (fn [frame _ _ _ _]
                              (some? (prev-resolution (:game frame))))
                            (ui/button "<" :on-click resolution-down)
                            (ui/disabled-button "<")))
    (ui/stack
      (ui/fancy-box 2)
      (ui/center
        (ui/gs-text (comp resolution-str selected-resolution))))
    (ui/hug #{:right}
      (ui/restrict-width 24 (ui/if-elem
                              (fn [frame _ _ _ _]
                                (some? (next-resolution (:game frame))))
                              (ui/button ">" :on-click resolution-up)
                              (ui/disabled-button ">"))))))

(def apply-button
  (ui/if-elem (ui/gs-pred options-changed?)
    (ui/button "Apply" :on-click! (fn [_] (apply-options!)))
    (ui/disabled-button "Apply")))

(def cancel-button
  (ui/button "Cancel" :on-click (comp ui/back discard-changes)))

(defn active-fullscreen
  [gs]
  (-> gs :settings :fullscreen? (or false)))

(defn selected-fullscreen
  [gs]
  (if-some [v (-> gs :ui :options :fullscreen?)]
    v
    (active-fullscreen gs)))

(defn set-fullscreen
  [gs x]
  (if (= x (active-fullscreen gs))
    (util/dissoc-in gs [:ui :options :fullscreen?])
    (assoc-in gs [:ui :options :fullscreen?] x)))

(def fullscreen-toggle
  (ui/center
    (ui/restrict-width 48
      (ui/if-elem
        (ui/gs-pred selected-fullscreen)
        (ui/selected-button "On" :on-click [set-fullscreen false])
        (ui/button "Off" :on-click [set-fullscreen true])))))

(ui/defscene :options
  ui/back-handler
  (ui/translate
    32 32
    (ui/center
      (ui/restrict-width
        320
        (ui/rows
          (ui/restrict-height
            64
            (ui/rows
              (ui/cols
                (ui/center "Resolution")
                (ui/center "Fullscreen"))
              (ui/cols
                resolution-cycler
                fullscreen-toggle))))))
    (ui/hug #{:bottom :right}
      (ui/resize 96 64
        (ui/rows
          apply-button
          cancel-button)))))