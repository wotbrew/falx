(ns falx.race
  (:require [falx.sprite :as sprite]
            [falx.gender :as gender]))

(def human
  {:key :human
   :name "Human"
   :description "Average at everything, no inherent traits or weaknesses."
   :male-body-sprite sprite/human-male
   :female-body-sprite sprite/human-female})

(def dwarf
  {:key :dwarf
   :name "Dwarf"
   :description "Sturdy and strong, though a little slow."
   :male-body-sprite sprite/dwarf-male
   :female-body-sprite sprite/dwarf-female})

(defn get-body-sprite
  [race gender]
  (if (= gender gender/male)
    (:male-body-sprite race)
    (:female-body-sprite race)))

(def all
  [human
   dwarf])

(def by-key
  (reduce #(assoc %1 (:key %2) %2) {} all))