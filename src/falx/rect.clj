(ns falx.rect)

(def get-x #(nth % 0))

(def get-y #(nth % 1))

(def get-width #(nth % 2))

(def get-height #(nth % 3))

(defn get-size
  [rect]
  [(get-width rect)
   (get-height rect)])