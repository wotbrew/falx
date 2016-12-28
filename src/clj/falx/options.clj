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

(defn active-aspect-ratio
  [gs]
  (-> gs :settings :aspect-ratio (or 4/3)))

(defn selected-aspect-ratio
  [gs]
  (or (-> gs :ui :options :aspect-ratio)
      (active-aspect-ratio gs)))

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
    (if (= ratio (active-aspect-ratio gs))
      (util/dissoc-in gs [:ui :options :aspect-ratio])
      (assoc-in gs [:ui :options :aspect-ratio] ratio))
    (select-resolution (first (get resolutions ratio)))))

(defn aspect-ratio-up
  [gs]
  (if-some [r (next-aspect-ratio gs)]
    (select-aspect-ratio gs r)
    gs))

(defn aspect-ratio-down
  [gs]
  (if-some [r (prev-aspect-ratio gs)]
    (select-aspect-ratio gs r)
    gs))

(def aspect-ratio-cycler
  (ui/stack
    (ui/restrict-width 24 (ui/if-elem (ui/gs-pred prev-aspect-ratio)
                            (ui/button "<" :on-click aspect-ratio-down)
                            (ui/disabled-button "<")))
    (ui/stack
      (ui/fancy-box 2)
      (ui/center
        (ui/gs-text (comp aspect-ratio-str selected-aspect-ratio))))
    (ui/hug #{:right}
      (ui/restrict-width 24 (ui/if-elem (ui/gs-pred next-aspect-ratio)
                              (ui/button ">" :on-click aspect-ratio-up)
                              (ui/disabled-button ">"))))))

(defn active-resolution
  [gs]
  (-> gs :settings :resolution (or [640 480])))

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
  (first (rsubseq (get resolutions (selected-aspect-ratio gs)) < (selected-resolution gs))))

(defn next-resolution
  [gs]
  (first (subseq (get resolutions (selected-aspect-ratio gs)) > (selected-resolution gs))))

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
                            (ui/gs-pred prev-resolution)
                            (ui/button "<" :on-click resolution-down)
                            (ui/disabled-button "<")))
    (ui/stack
      (ui/fancy-box 2)
      (ui/center
        (ui/gs-text (comp resolution-str selected-resolution))))
    (ui/hug #{:right}
      (ui/restrict-width 24 (ui/if-elem
                              (ui/gs-pred next-resolution)
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

(defn active-cell-size
  [gs]
  (-> gs :settings :cell-size (or [32 32])))

(defn selected-cell-size
  [gs]
  (or (-> gs :ui :options :cell-size)
      (active-cell-size gs)))

(defn prev-cell-size
  [gs]
  (first (rsubseq cell-sizes < (selected-cell-size gs))))

(defn next-cell-size
  [gs]
  (first (subseq cell-sizes > (selected-cell-size gs))))

(defn select-cell-size
  [gs size]
  (if (= size (active-cell-size gs))
    (util/dissoc-in gs [:ui :options :cell-size])
    (assoc-in gs [:ui :options :cell-size] size)))

(defn cell-size-up
  [gs]
  (if-some [s (next-cell-size gs)]
    (select-cell-size gs s)
    gs))

(defn cell-size-down
  [gs]
  (if-some [s (prev-cell-size gs)]
    (select-cell-size gs s)
    gs))

(def cell-size-cycler
  (ui/stack
    (ui/restrict-width 24 (ui/if-elem (ui/gs-pred prev-cell-size)
                            (ui/button "<" :on-click cell-size-down)
                            (ui/disabled-button "<")))
    (ui/stack
      (ui/fancy-box 2)
      (ui/center
        (ui/gs-text (comp resolution-str selected-cell-size))))
    (ui/hug #{:right}
      (ui/restrict-width 24
        (ui/if-elem (ui/gs-pred next-cell-size)
          (ui/button ">" :on-click cell-size-up)
          (ui/disabled-button ">"))))))

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