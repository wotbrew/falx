(ns falx.ui.edit
  (:require [falx.ui.widget :refer :all]
            [falx.ui :as ui]
            [clj-gdx :as gdx]
            [falx.ui.widget :as widget]))

(def get-palette-name
  {:terrain "Terrain"
   :creatures "Creatures"
   :decor "Decor"})

(def palette-cycle-right
  {:terrain :decor
   :decor :creatures
   :creatures :terrain})

(def palette-cycle-left
  {:creatures :decor
   :decor :terrain
   :terrain :creatures})

(defmethod widget/clicked :screen.edit/palette-left
  [ui _ _]
  (assoc ui ::palette (get palette-cycle-left (::palette ui :terrain))))

(defmethod widget/clicked :screen.edit/palette-right
  [ui _ _]
  (assoc ui ::palette (get palette-cycle-right (::palette ui :terrain))))

(defn palette
  [ui rect]
  (let [bb (block-box rect)
        [x y w h] (block-box-inner-rect bb)
        key (::palette ui :terrain)]
    (panel
      [bb
       (ctext (get-palette-name key) [x y w 32])
       (-> (as :screen.edit/palette-left
             (arrow-left [x y 32 32]))
           clickable)
       (-> (as :screen.edit/palette-right
             (arrow-right [(+ x w -32) y 32 32]))
           clickable)])))

(defmethod ui/screen :screen/edit
  [ui frame]
  (let [display (:display frame)
        [w h :as size] (:size display)]
    (as :screen/edit
      (screen [(palette ui [0 96 256 (let [n (- h 96)] (- n (mod n 32)))])
               (text (:input frame) [0 32 256 0])
               (fps frame)]))))

(defmethod update-ui-screen :screen/edit
  [ui widget frame]
  (let [keyboard (-> frame :input :keyboard)]
    (if (contains? (:hit keyboard) :e)
      (ui/revert-screen ui)
      ui)))