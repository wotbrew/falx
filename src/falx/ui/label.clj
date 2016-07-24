(ns falx.ui.label
  (:require [falx.ui :as ui]
            [falx.draw :as d]))

(defn drawfn
  ([text]
   (let [textfn (if (fn? text) text (constantly text))]
     (fn [view rect]
       (let [txt (textfn view)]
         (d/drawfn
           (d/text txt)
           rect))))))

(defn define
  [k & {:as kvs}]
  (ui/add-elem!
    k
    (merge
      {:drawfn (drawfn (:text kvs identity))}
      kvs)))
