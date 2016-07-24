(ns falx.rect)

(def id
  [0 0 0 0])

(defn x
  [rect]
  (nth rect 0))

(defn y
  [rect]
  (nth rect 1))

(defn w
  [rect]
  (nth rect 2))

(defn h
  [rect]
  (nth rect 3))

(defn shift
  ([rect pt]
    (shift (x rect) (y rect) (w rect) (h rect) pt))
  ([x y w h pt]
   (let [[x2 y2] pt]
     (shift x y w h x2 y2)))
  ([x y w h x2 y2]
    [(+ x x2) (+ y y2) w h]))

(defn fit
  ([rect size]
    (fit (x rect) (y rect) (w rect) (h rect) size))
  ([x y w h size]
   (let [[w2 h2] size]
     (fit x y w h w2 h2)))
  ([x y w h w2 h2]
    [x y (min w w2) (min h h2)]))