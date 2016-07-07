(ns falx.geom.spec
  (:require [falx.geom :as g]
            [clojure.spec :as s]))

(s/def ::valid-number
  (-> (s/and number?
             #(cond (double? %) (not (or (.isNaN ^Double %)
                                         (.isInfinite ^Double %)))
                    (float? %) (not (or (.isNaN ^Float %)
                                        (.isInfinite ^Float %)))
                    :else true))
      (s/with-gen
        #(s/gen (s/or :long (s/int-in -10 10)
                      :double (s/double-in :min -10 :max 10
                                           :infinite? false
                                           :NaN? false))))))

(s/def ::g/x ::valid-number)
(s/def ::g/y ::valid-number)
(s/def ::g/w ::valid-number)
(s/def ::g/h ::valid-number)

(s/def ::g/point
  (s/keys :req [::g/x ::g/y]))
(s/def ::g/size
  (s/keys :req [::g/w ::g/h]))
(s/def ::g/rect
  (s/keys :req [::g/x ::g/y ::g/w ::g/h]))

(s/def ::g/geom
  (s/or :point ::g/point
        :size ::g/size
        :rect ::g/rect))

(s/fdef g/point
  :args (s/or :arity0 empty?
              :arity1 (s/tuple ::g/geom)
              :arity2 (s/tuple ::g/x ::g/y))
  :ret (s/nilable ::g/point))

(s/fdef g/size
  :args (s/or :arity0 empty?
              :arity1 (s/tuple ::g/geom)
              :arity2 (s/tuple ::g/w ::g/h))
  :ret (s/nilable ::g/size))

(s/fdef g/rect
  :args (s/or :arity0 empty?
              :arity1 (s/tuple ::g/geom)
              :arity2 (s/tuple ::g/x ::g/y ::g/w ::g/h))
  :ret (s/nilable ::g/rect))

(s/fdef g/add
  :args (s/or :arity1 (s/tuple ::g/geom)
              :arity2 (s/tuple ::g/geom ::g/geom)
              :arityn (s/+ ::g/geom))
  :ret ::g/geom)

(s/fdef g/sub
  :args (s/or :arity1 (s/tuple ::g/geom)
              :arity2 (s/tuple ::g/geom ::g/geom)
              :arityn (s/+ ::g/geom))
  :ret ::g/geom)

(s/fdef g/mult
  :args (s/or :arity1 (s/tuple ::g/geom)
              :arity2 (s/tuple ::g/geom ::g/geom)
              :arityn (s/+ ::g/geom))
  :ret ::g/geom)

(s/fdef g/put
  :args (s/or :arity2 (s/tuple ::g/geom ::g/geom)
              :arity3 (s/tuple ::g/geom ::g/x ::g/y))
  :ret ::g/geom)

(s/fdef g/resize
  :args (s/or :arity2 (s/tuple ::g/geom ::g/geom)
              :arity3 (s/tuple ::g/geom ::g/w ::g/h))
  :ret ::g/geom)

(s/fdef g/points
  :args (s/or :arity0 (s/tuple ::g/geom))
  :ret (s/* ::g/point))

(s/def ::g/point-tuple (s/tuple ::g/x ::g/y))

(s/def ::g/rect-tuple (s/tuple ::g/x ::g/y ::g/w ::g/h))

(s/def ::g/size-tuple (s/tuple ::g/w ::g/h))

(s/fdef g/->point-tuple
  :args (s/or :arity1 (s/tuple ::g/geom))
  :ret ::g/point-tuple)

(s/fdef g/->rect-tuple
  :args (s/or :arity1 (s/tuple ::g/geom))
  :ret ::g/rect-tuple)

(s/fdef g/->size-tuple
  :args (s/or :arity1 (s/tuple ::g/geom))
  :ret ::g/size-tuple)

(s/fdef g/point-tuple->geom
  :args (s/or :arity1 (s/tuple ::g/point-tuple))
  :ret ::g/point)

(s/fdef g/rect-tuple->geom
  :args (s/or :arity1 (s/tuple ::g/rect-tuple))
  :ret ::g/rect)

(s/fdef g/size-tuple->geom
  :args (s/or :arity1 (s/tuple ::g/size-tuple))
  :ret ::g/size)
