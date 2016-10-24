(ns falx.rect)

(defn contains-pt?
  ([rect pt]
   (let [[x2 y2] pt]
     (contains-pt? rect x2 y2)))
  ([rect x2 y2]
   (let [[x y w h] rect]
     (contains-pt? x y w h x2 y2)))
  ([x y w h pt]
   (let [[x2 y2] pt]
     (contains-pt? x y w h x2 y2)))
  ([x y w h x2 y2]
   (and (<= x x2 (+ x w))
        (<= y y2 (+ y h)))))