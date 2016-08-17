(ns falx.widget
  (:require [falx.engine.draw :as d]
            [falx.engine.ui :as ui]
            [falx.engine.input :as input]
            [falx.engine.mouse :as mouse]
            [falx.engine.scene :as scene]
            [falx.sprite :as sprite]))

(def focused?
  (fn [model input rect]
    (and
      (not (::disabled? model))
      (let [mouse (::input/mouse input)]
        (mouse/in? mouse rect)))))

(def disabled?
  (ui/pred ::disabled?))

(def selected?
  (ui/pred ::selected?))

(defn button-text-disabled
  [s]
  (d/text s {:centered? true :color [0.3 0.3 0.3 1]}))

(defn button-disabled
  [opts]
  (let [{:keys [s sfn] :or {sfn :s}} opts]
    (scene/stack
      (d/recolor d/pixel [0 0 0 1])
      (d/box {:color [0.3 0.3 0.3 1]
              :shaded? true
              :thickness 2})
      (if s
        (button-text-disabled s)
        (ui/target (comp button-text-disabled sfn))))))

(defn button-text-selected
  [s]
  (d/text s {:centered? true :color [0.7 0.7 0 1]}))

(defn button-selected
  [opts]
  (let [{:keys [s sfn] :or {sfn :s}} opts]
    (scene/stack
      (d/recolor d/pixel [0 0 0 1])
      (d/box {:color [0.7 0.7 0 1]
              :shaded? true
              :thickness 2})
      (if s
        (button-text-selected s)
        (ui/target #(button-text-selected (sfn %)))))))

(defn button-text-focused
  [s opts]
  (let [s (if (:text-dash? opts true)
            (str "- " s " -")
            s)]
    (d/text s {:centered? true :color [0 1 0 1]})))

(def default-bindmap
  {::event.click (input/pressed-set ::mouse/button.left)
   ::event.alt-click (input/pressed-set ::mouse/button.right)})

(defn button-focused
  [opts]
  (let [{:keys [id s sfn] :or {sfn :s}} opts]
    (-> (scene/stack
          (d/recolor d/pixel [0 0 0 1])
          (d/box {:color [0 1 0 1]
                  :shaded? true
                  :thickness 2})
          (if s
            (button-text-focused s opts)
            (ui/target #(button-text-focused (sfn %) opts))))
        (ui/behaviour (fn [model input rect]
                        (when (focused? model input rect)
                          (for [event-type (input/query input default-bindmap)]
                            {::event.type event-type
                             ::event.widget id
                             ::event.model model
                             ::event.input input
                             ::event.rect rect})))))))

(defn button-text-default
  [s]
  (d/text s {:centered? true
             :color [0.7 0.7 0.7 1]}) )

(defn button-default
  [opts]
  (let [{:keys [s sfn] :or {sfn :s}} opts]
    (scene/stack
      (d/recolor d/pixel [0 0 0 1])
      (d/box {:color [0.7 0.7 0.7 1]
              :shaded? true
              :thickness 2})
      (if s
        (button-text-default s)
        (ui/target (comp button-text-default sfn))))))

(defn button
  ([opts]
   (let [{:keys [disable-pred
                 focused-pred
                 selected-pred]
          :or {focused-pred focused?
               disable-pred disabled?
               selected-pred selected?}} opts]
     (->> (button-default opts)
          (ui/if-pred selected-pred (button-selected opts))
          (ui/if-pred focused-pred (button-focused opts))
          (ui/if-pred disable-pred (button-disabled opts))))))

(defn drop-down-preview
  [opts]
  (let [selectedfn (:selected opts (comp first (:options opts)))
        sfn selectedfn]
    (button (assoc opts :sfn sfn :text-dash? false))))

(defn drop-down-icon
  [opts]
  (let [tscene #(-> (scene/center % 32 32)
                    (scene/fitw 32)
                    (scene/at-right [32 0]))]
    (ui/if-pred focused? (tscene (d/recolor sprite/down [0 1 0 1])) (tscene sprite/down))))

(defn drop-down
  ([opts]
   (scene/stack
     (drop-down-preview opts)
     (drop-down-icon opts))))