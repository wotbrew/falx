(ns falx.size)

(defn centered-rect
  ([size rect]
   (let [[sw sh] size]
     (centered-rect sw sh rect)))
  ([sw sh rect]
   (let [[x y w h] rect]
     (centered-rect sw sh x y w h)))
  ([size x y w h]
   (let [[sw sh] size]
     (centered-rect sw sh x y w h)))
  ([sw sh x y w h]
   [(int (+ x (- (/ w 2) (/ sw 2))))
    (int (+ y (- (/ h 2) (/ sh 2))))
    sw
    sh]))