(ns falx.options
  (:require [falx.ui :as ui]
            [clojure.set :as set]
            [falx.gdx :as gdx]
            [falx.util :as util]
            [falx.game :as g]
            [falx.game-state :as gs]))

(defn options-changed?
  [gs]
  (some? (-> gs :ui :options)))

(defn discard-changes
  [gs]
  (util/dissoc-in gs [:ui :options]))

(defn apply-changes
  [gs]
  (-> (update gs :settings merge (-> gs :ui :options))
      gs/center-camera-on-active-party))

(defn apply-options!
  [frame]
  (let [opts (-> frame :state :ui :options)]
    (g/update-state! (:game frame) (comp discard-changes apply-changes))
    (apply
      gdx/configure!
      (apply concat (set/rename-keys opts {:resolution :size})))))

(def resolutions
  {4/3   (sorted-set
           [640 480]
           [800 600]
           [1024 768]
           [1152 864]
           [1280 960]
           [1400 1050]
           [1600 1200])
   16/9  (sorted-set
           [852 480]
           [1280 720]
           [1365 768]
           [1600 900]
           [1920 1080])
   16/10 (sorted-set
           [1280 800]
           [1440 900]
           [1680 1050]
           [1920 1200]
           [2560 1600])})

(defn aspect-ratio-str
  [ratio]
  (case ratio
    4/3 "4:3"
    16/9 "16:9"
    16/10 "16:10"))

(def aspect-ratios
  (into (sorted-set) (keys resolutions)))


(defn selected-aspect-ratio
  [gs]
  (or (-> gs :ui :options :aspect-ratio)
      (gs/aspect-ratio gs)))

(defn prev-aspect-ratio
  [gs]
  (first (rsubseq aspect-ratios < (selected-aspect-ratio gs))))

(defn next-aspect-ratio
  [gs]
  (first (subseq aspect-ratios > (selected-aspect-ratio gs))))

(declare select-resolution)

(defn select-aspect-ratio
  [gs ratio]
  (->
    (if (or (nil? ratio)
            (= ratio (gs/aspect-ratio gs)))
      (util/dissoc-in gs [:ui :options :aspect-ratio])
      (assoc-in gs [:ui :options :aspect-ratio] ratio))
    (select-resolution (first (get resolutions ratio)))))

(def aspect-ratio-cycler
  (ui/cycler
    (comp aspect-ratio-str selected-aspect-ratio)
    select-aspect-ratio
    prev-aspect-ratio
    next-aspect-ratio))

(defn selected-resolution
  [gs]
  (or (-> gs :ui :options :resolution)
      (gs/resolution gs)))

(defn select-resolution
  [gs size]
  (if (or (nil? size)
          (= size (gs/resolution gs)))
    (util/dissoc-in gs [:ui :options :resolution])
    (assoc-in gs [:ui :options :resolution] size)))

(defn prev-resolution
  [gs]
  (first (rsubseq (get resolutions (selected-aspect-ratio gs)) < (selected-resolution gs))))

(defn next-resolution
  [gs]
  (first (subseq (get resolutions (selected-aspect-ratio gs)) > (selected-resolution gs))))

(defn resolution-str
  [[w h]]
  (str (long w) " x " (long h)))

(def resolution-cycler
  (ui/cycler
    (comp resolution-str selected-resolution)
    select-resolution
    prev-resolution
    next-resolution))

(def apply-button
  (ui/if-elem (ui/gs-pred options-changed?)
    (ui/button "Apply" :on-click! apply-options!)
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
  (if (or (nil? x) (= x (active-fullscreen gs)))
    (util/dissoc-in gs [:ui :options :fullscreen?])
    (assoc-in gs [:ui :options :fullscreen?] x)))

(def fullscreen-toggle
  (ui/restrict-width 48
    (ui/if-elem
      (ui/gs-pred selected-fullscreen)
      (ui/selected-button "On" :on-click [set-fullscreen false])
      (ui/button "Off" :on-click [set-fullscreen true]))))

(def game-options-pane
  (ui/stack
    (ui/fancy-box 1)
    (ui/restrict-height
      24
      (ui/rows
        (ui/center "Game")))))

(def cell-sizes
  (sorted-set
    [16 16]
    [32 32]
    [48 48]
    [64 64]
    [80 80]
    [96 96]))

(defn selected-cell-size
  [gs]
  (or (-> gs :ui :options :cell-size)
      (gs/cell-size gs)))

(defn prev-cell-size
  [gs]
  (first (rsubseq cell-sizes < (selected-cell-size gs))))

(defn next-cell-size
  [gs]
  (first (subseq cell-sizes > (selected-cell-size gs))))

(defn select-cell-size
  [gs size]
  (if (or (nil? size) (= size (gs/cell-size gs)))
    (util/dissoc-in gs [:ui :options :cell-size])
    (assoc-in gs [:ui :options :cell-size] size)))

(def cell-size-cycler
  (ui/cycler
    (comp resolution-str selected-cell-size)
    select-cell-size
    prev-cell-size
    next-cell-size))

(def graphics-options-pane
  (ui/stack
    (ui/fancy-box 1)
    (ui/restrict-height
      80
      (ui/rows
        (ui/center "Graphics")
        (ui/cols
          (ui/pad 6 8 (ui/center "Aspect Ratio"))
          (ui/pad 6 8 (ui/center "Resolution"))
          (ui/stack
            (ui/translate 0 8 "Fullscreen")
            (ui/translate 30 1 (ui/center "Cell size"))))
        (ui/cols
          (ui/pad 6 0 aspect-ratio-cycler)
          (ui/pad 6 0 resolution-cycler)
          (ui/stack
            (ui/pad 8 0 fullscreen-toggle)
            (ui/add-size
              -76 0
              (ui/translate 70 0 cell-size-cycler))))))))

(def audio-options-pane
  (ui/stack
    (ui/fancy-box 1)
    (ui/restrict-height
      24
      (ui/rows
        (ui/center "Audio")))))

(ui/defscene :options
  ui/back-handler
  ui/breadcrumbs
  (ui/pad
    32 48
    (ui/rows
      (ui/pad
        0 4 game-options-pane)
      (ui/pad
        0 4 graphics-options-pane)
      (ui/pad
        0 4 audio-options-pane)
      (ui/hug #{:bottom :right}
        (ui/resize 96 64
          (ui/rows
            apply-button
            cancel-button)))))
  ui/hover-over)