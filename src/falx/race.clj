(ns falx.race
  (:require [falx.sprite :as sprite]))

(def human
  {:male-body-sprite sprite/human-male
   :female-body-sprite sprite/human-female})

(def dwarf
  {:male-body-sprite sprite/dwarf-male
   :female-body-sprite sprite/dwarf-female})

(defn get-body-sprite
  [race gender]
  (case gender
    :male (:male-body-sprite race)
    (:female-body-sprite race)))

(def all
  [human
   dwarf])