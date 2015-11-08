(ns falx.screen.create
  (:require [falx.widget :as widget]
            [falx.theme :as theme]
            [falx.rect :as rect]))

(defn centered-rect
  [size]
  (let [[width height] size]
    (rect/center-rect [0 0 width height] [640 480])))

(defn title-text
  [rect]
  (widget/panel
    [(widget/box rect)
     (-> (widget/label rect ":: CREATE ::")
         (assoc :context {:color theme/red}))]))

(defn menu-button
  [rect]
  (-> (widget/text-button "(M)enu" rect)
      (assoc :click-event {:type :event/goto-menu})))

(defn create-button
  [rect]
  (widget/text-button "(C)reate" rect))

(defn kill-button
  [rect]
  (-> (widget/text-button "(K)ill" rect)
      (assoc :on-update (fn [m game]
                          (assoc m
                            :disabled? (nil? (-> game :roster :selected)))))))

(defn details-button
  [rect]
  (-> (widget/text-button "(D)etails" rect)
      (assoc :on-update (fn [m game]
                          (assoc m
                            :disabled? (nil? (-> game :roster :selected)))))))


(defn buttons
  [rect]
  (let [coll [create-button
              details-button
              kill-button
              menu-button]
        rects (rect/fit-horizontal rect (count coll))]
    (widget/panel
      (mapv (fn [f rect] (f rect)) coll rects))))

(defn screen
  ([game]
   (screen game (-> game :ui-camera :size)))
  ([game size]
   (let [[width height] size
         [x y w h :as r] (centered-rect size)]
     (-> (widget/panel
           [(widget/fps-label [0 0 64 32])
            (widget/filler-border (rect/extend r 32))
            (title-text [x y w 32])
            (buttons [x (+ y 32) w 32])])
         (widget/update-widget
           game)))))