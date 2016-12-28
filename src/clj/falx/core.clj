(ns falx.core
  (:require [falx.gdx :as gdx]
            [falx.state :as state]
            [falx.ui :as ui]
            [clojure.set :as set]))

(ui/defscene :roster
  ui/back-handler
  (ui/center
    (ui/resize
      320 280
      (ui/stack
        (ui/fancy-box 2)
        (ui/center "roster")))))

(ui/defscene :new
  ui/back-handler
  (ui/center
    (ui/resize
      320 280
      (ui/stack
        (ui/fancy-box 2)
        (ui/center "new")))))

(ui/defscene :continue
  ui/back-handler
  (ui/center
    (ui/resize
      320 280
      (ui/stack
        (ui/fancy-box 2)
        (ui/center "continue")))))

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

(defn options-changed?
  [gs]
  (some? (-> gs :ui :options)))

(defn dissoc-in
  "Dissociate a value in a nested assocative structure, identified by a sequence
  of keys. Any collections left empty by the operation will be dissociated from
  their containing structures."
  [m ks]
  (if-let [[k & ks] (seq ks)]
    (if (seq ks)
      (let [v (dissoc-in (get m k) ks)]
        (if (empty? v)
          (dissoc m k)
          (assoc m k v)))
      (dissoc m k))
    m))

(defn select-resolution
  [gs size]
  (if (= size (active-resolution gs))
    (dissoc-in gs [:ui :options :resolution])
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

(defn apply-options!
  []
  (let [opts (-> @state/game :ui :options)]
    (swap! state/game #(-> %
                           (dissoc-in [:ui :options])
                           (update :settings merge opts)))
    (apply
      gdx/configure!
      (apply concat (set/rename-keys opts {:resolution :size})))))

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
              (ui/center "Resolution")
              resolution-cycler)))))
    (ui/hug #{:bottom :right}
      (ui/resize 96 64
        (ui/rows
          (ui/if-elem (ui/gs-pred options-changed?)
            (ui/button "Apply" :on-click! (fn [_]
                                            (apply-options!)))
            (ui/disabled-button "Apply"))
          (ui/button "Cancel" :on-click ui/back))))))

(def main-menu
  (ui/center
    (ui/resize
      320 280
      (ui/rows
        (ui/button "Roster" :on-click [ui/goto :roster])
        (ui/button "New Adventure" :on-click [ui/goto :new])
        (ui/button "Continue Adventure" :on-click [ui/goto :continue])
        (ui/button "Options" :on-click [ui/goto :options])
        (ui/button "Quit" :on-click! (fn [_] (println "clicked quit...")))))))

(ui/defscene :main-menu
  main-menu)

(gdx/on-tick tick
  [tick]
  (let [frame (state/current-frame tick)
        [w h] (:size (:config tick))]
    (ui/handle!
      (ui/scene frame)
      frame)))

(defn init!
  [])