(ns falx.scene
  (:require [falx.rect :as rect]
            [falx.size :as size]))

(defprotocol INode
  (-layout [this scene rect]))

(defn layout
  ([node rect]
   (layout {} node rect))
  ([scene node rect]
   (-layout node scene rect)))

(extend-protocol INode
  Object
  (-layout [this scene rect]
    (assoc scene this rect)))

(defrecord Stack [nodes]
  INode
  (-layout [this scene rect]
    (reduce #(layout %1 %2 rect) scene nodes)))

(defn stack
  [nodes]
  (->Stack nodes))

(defrecord At [node pt]
  INode
  (-layout [this scene rect]
    (layout scene node (rect/shift rect pt))))

(defn at
  "Returns a node offset by `x` and `y`."
  ([node pt]
   (->At node pt))
  ([node x y]
   (at node [x y])))

(defrecord Center [node size]
  INode
  (-layout [this scene rect]
    (let [rect (size/center size rect)]
      (layout scene node rect))))

(defn center
  "Centers the node according to the given size."
  ([node size]
   (->Center node size))
  ([node w h]
   (->Center node [w h])))

(defrecord Fit [node size]
  INode
  (-layout [this scene rect]
    (layout scene node (rect/fit rect size))))

(defn fit
  "Fits the node to the size (if the requested size is larger)"
  ([node size]
   (->Fit node size))
  ([node w h]
   (fit node [w h])))

(defrecord Rows [nodes]
  INode
  (-layout [this scene rect]
    (let [n (count nodes)
          [x y w h] rect
          ih (long (/ h n)) ]
      (reduce-kv
        (fn [scene i node]
          (layout scene node [x (+ y (* ih i)) w ih]))
        scene nodes))))

(defn rows
  ([nodes]
   (->Rows (vec nodes))))

(defrecord FixedRows [h nodes]
  INode
  (-layout [this scene rect]
    (let [[x y w] rect]
      (reduce-kv
        (fn [scene i node]
          (layout scene node [x (+ y (* h i)) w h]))
        scene nodes))))

(defn frows
  ([h nodes]
   (->FixedRows h (vec nodes))))

(defrecord Cols [nodes]
  INode
  (-layout [this scene rect]
    (let [n (count nodes)
          [x y w h] rect
          iw (long (/ w n)) ]
      (reduce-kv
        (fn [scene i node]
          (layout scene node [(+ x (* iw i)) y w h]))
        scene nodes))))

(defn cols
  ([nodes]
   (->Cols (vec nodes))))

(defrecord FixedCols [w nodes]
  INode
  (-layout [this scene rect]
    (let [[x y _ h] rect]
      (reduce-kv
        (fn [scene i node]
          (layout scene node [(+ x (* i w)) y w h]))
        scene nodes))))

(defn fcols
  ([w nodes]
   (->FixedCols w (vec nodes))))

(defrecord HItems [size nodes]
  INode
  (-layout [this scene rect]
    (let [[w h] size
          [x y w2 h2] rect
          cols (long (/ w2 w))
          rows (long (/ h2 h))]
      (reduce-kv
        (fn [scene i node]
          (let [col (mod i cols)
                row (mod (long (/ i cols)) rows)]
            (layout scene
                    node
                    [(+ x (* w col))
                     (+ y (* h row))
                     w
                     h])))
        scene
        nodes))))

(defn hitems
  ([size nodes]
   (->HItems size (vec nodes))))

(def items hitems)