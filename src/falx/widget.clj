(ns falx.widget
  (:require [falx.engine.draw :as d]
            [falx.engine.ui :as ui]
            [falx.engine.input :as input]
            [falx.engine.mouse :as mouse]
            [falx.engine.scene :as scene]))

(def focused?
  (fn [model input rect]
    (and
      (not (::disabled? model))
      (let [mouse (::input/mouse input)]
        (mouse/in? mouse rect)))))

(def disabled?
  (ui/pred ::disabled?))

(defn button-text-disabled
  [s]
  (d/text s {:centered? true :color [0.3 0.3 0.3 1]}))

(defn button-disabled
  [opts]
  (let [{:keys [s]} opts]
    (scene/stack
      (d/recolor d/pixel [0 0 0 1])
      (d/box {:color [0.3 0.3 0.3 1]
              :shaded? true
              :thickness 2})
      (if s
        (button-text-disabled s)
        (ui/dynamic (comp button-text-disabled :s))))))

(defn button-text-focused
  [s]
  (d/text (str "- " s " -") {:centered? true :color [0 1 0 1]}))

(def default-bindmap
  {::event.click (input/hit ::mouse/button.left)
   ::event.alt-click (input/hit ::mouse/button.right)})

(defn button-focused
  [opts]
  (let [{:keys [id s]} opts]
    (-> (scene/stack
          (d/recolor d/pixel [0 0 0 1])
          (d/box {:color [0 1 0 1]
                  :shaded? true
                  :thickness 2})
          (if s
            (button-text-focused s)
            (ui/dynamic (comp button-text-focused :s))))
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
  (let [{:keys [s]} opts]
    (scene/stack
      (d/recolor d/pixel [0 0 0 1])
      (d/box {:color [0.7 0.7 0.7 1]
              :shaded? true
              :thickness 2})
      (if s
        (button-text-default s)
        (ui/dynamic (comp button-text-default :s))))))

(defn button
  ([opts]
   (let [{:keys [disable-pred
                 focused-pred]
          :or {focused-pred focused?
               disable-pred disabled?}} opts]
     (->> (button-default opts)
          (ui/if-pred focused? (button-focused opts))
          (ui/if-pred disable-pred (button-disabled opts))))))
