(ns falx.gender)

(def male
  {:key :male
   :name "Male"})

(def female
  {:key :female
   :name "Female"})

(def all
  [male
   female])

(def by-key
  (reduce #(assoc %1 (:key %2) %2) {} all))