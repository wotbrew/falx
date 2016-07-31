(ns falx.scene
  (:require [falx.rect :as rect]
            [falx.size :as size]
            [falx.scene.protocols :refer [INode -layout]])
  (:import (clojure.lang Var)))

(defn layout
  ([scene rect]
   (layout [] scene rect))
  ([result scene rect]
   (-layout scene result rect)))

(extend-protocol INode
  Object
  (-layout [this result rect]
    (conj result [this rect]))
  Var
  (-layout [this result rect]
    (-layout (var-get this) result rect)))

(defrecord Stack [nodes]
  INode
  (-layout [this result rect]
    (reduce #(layout %1 %2 rect) result nodes)))

(defn coll->stack
  [nodes]
  (->Stack (vec nodes)))

(defn stack
  ([& nodes]
   (coll->stack nodes)))

(defrecord Pad [node left top right bottom]
  INode
  (-layout [this result rect]
    (let [[x y w h] rect]
      (layout result node
              [(+ x left)
               (+ y top)
               (- w left right)
               (- h top bottom)]))))

(defn pad
  ([node padding]
   (pad node
        (:left padding 0)
        (:top padding 0)
        (:right padding 0)
        (:bottom padding 0)))
  ([node left top bottom right]
   (->Pad node left top bottom right)))

(defrecord At [node pt]
  INode
  (-layout [this result rect]
    (layout result node (rect/shift rect pt))))

(defn at
  "Returns a node offset by `x` and `y`."
  ([node pt]
   (->At node pt))
  ([node x y]
   (at node [x y])))

(defrecord AtRight [node pt]
  INode
  (-layout [this result rect]
    (let [[x y w h] rect
          [x2 y2] pt
          x (- (+ x w) x2)
          y (+ y2 y)]
      (layout result node [x y w h]))))

(defn at-right
  [node pt]
  (->AtRight node pt))

(defrecord Center [node size]
  INode
  (-layout [this result rect]
    (let [rect (size/center size rect)]
      (layout result node rect))))

(defn center
  "Centers the node according to the given size."
  ([node size]
   (->Center node size))
  ([node w h]
   (->Center node [w h])))

(defrecord Fit [node size]
  INode
  (-layout [this result rect]
    (layout result node (rect/fit rect size))))

(defn fit
  "Fits the node to the size (if the requested size is larger)"
  ([node size]
   (->Fit node size))
  ([node w h]
   (fit node [w h])))

(defrecord Rows [nodes]
  INode
  (-layout [this result rect]
    (if (empty? nodes)
      result
      (let [n (count nodes)
            [x y w h] rect
            ih (long (/ h n))]
        (reduce-kv
          (fn [result i node]
            (layout result node [x (+ y (* ih i)) w ih]))
          result nodes)))))

(defn coll->rows
  ([nodes]
   (->Rows (vec nodes))))

(defn rows
  [& nodes]
  (coll->rows nodes))

(defrecord FixedRows [h nodes]
  INode
  (-layout [this result rect]
    (let [[x y w] rect]
      (reduce-kv
        (fn [result i node]
          (layout result node [x (+ y (* h i)) w h]))
        result nodes))))

(defn coll->frows
  ([h nodes]
   (->FixedRows h (vec nodes))))

(defn frows
  [h & nodes]
  (coll->frows h nodes))

(defrecord Cols [nodes]
  INode
  (-layout [this result rect]
    (if (empty? nodes)
      result
      (let [n (count nodes)
            [x y w h] rect
            iw (long (/ w n))]
        (reduce-kv
          (fn [result i node]
            (layout result node [(+ x (* iw i)) y iw h]))
          result nodes)))))

(defn coll->cols
  ([nodes]
   (->Cols (vec nodes))))

(defn cols
  [& nodes]
  (coll->cols nodes))

(defrecord FixedCols [w nodes]
  INode
  (-layout [this result rect]
    (let [[x y _ h] rect]
      (reduce-kv
        (fn [result i node]
          (layout result node [(+ x (* i w)) y w h]))
        result nodes))))

(defn coll->fcols
  ([w nodes]
   (->FixedCols w (vec nodes))))

(defn fcols
  [w & nodes]
  (coll->fcols w nodes))

(defrecord HItems [size nodes]
  INode
  (-layout [this result rect]
    (let [[w h] size
          [x y w2 h2] rect
          cols (long (/ w2 w))
          rows (long (/ h2 h))]
      (reduce-kv
        (fn [result i node]
          (let [col (mod i cols)
                row (mod (long (/ i cols)) rows)]
            (layout result
                    node
                    [(+ x (* w col))
                     (+ y (* h row))
                     w
                     h])))
        result
        nodes))))

(defn coll->hitems
  ([size nodes]
   (->HItems size (vec nodes))))

(defn hitems
  [size & nodes]
  (coll->hitems size nodes))

(def coll->items coll->hitems)

(def items hitems)

(defrecord VItems [size nodes]
  INode
  (-layout [this result rect]
    (let [[w h] size
          [x y w2 h2] rect
          cols (long (/ w2 w))
          rows (long (/ h2 h))]
      (reduce-kv
        (fn [result i node]
          (let [row (mod i rows)
                col (mod (long (/ i rows)) cols)]
            (layout result
                    node
                    [(+ x (* w col))
                     (+ y (* h row))
                     w
                     h])))
        result
        nodes))))

(defn coll->vitems
  ([size nodes]
   (->VItems size (vec nodes))))

(defn hitems
  [size & nodes]
  (coll->hitems size nodes))

(defn table*
  [head & rows]
  (apply falx.scene/rows
         (coll->cols head)
         (map coll->cols rows)))

(defn htable*
  [head & rows]
  (apply falx.scene/cols
         (coll->rows head)
         (map coll->rows rows)))

(defn maptable
  ([kvs]
   (maptable kvs {}))
  ([kvs opts]
    ;;remember to support ordered (kv) colls too!
   (let [keys (map first kvs)
         vals (map second kvs)]
     (if (:vertical? opts)
       (falx.scene/rows
         (coll->cols keys)
         (coll->cols vals))
       (falx.scene/cols
         (coll->rows keys)
         (coll->rows vals))))))

(defn table
  [& kvs]
  (maptable (partition 2 kvs) {:vertical? true}))

(defn htable
  [& kvs]
  (maptable (partition 2 kvs) {:vertical? false}))