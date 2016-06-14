(ns falx.point.spec
  (:require [falx.point :as point]
            [clojure.spec :as s]))

(s/def ::point/point
  (s/tuple integer? integer?))

(s/fdef point/add
  :args (s/or
          :arity0 #{[]}
          :arity1 (s/tuple ::point/point)
          :arity2 (s/tuple ::point/point ::point/point)
          :arityn (s/* ::point/point))
  :ret ::point/point)

(s/fdef point/shift
  :args (s/tuple ::point/point integer?)
  :ret ::point/point)

(s/fdef point/lshift
  :args (s/tuple ::point/point integer?)
  :ret ::point/point)

(s/fdef point/mult
  :args (s/or
          :arity0 #{[]}
          :arity1 (s/tuple ::point/point)
          :arity2 (s/tuple ::point/point ::point/point)
          :arityn (s/* ::point/point))
  :ret ::point/point)

(s/fdef point/sub
  :args (s/or
          :arity0 #{[]}
          :arity1 (s/tuple ::point/point)
          :arity2 (s/tuple ::point/point ::point/point)
          :arityn (s/* ::point/point))
  :ret ::point/point)

(s/fdef point/scale
  :args (s/tuple ::point/point integer?)
  :ret ::point/point)

(s/fdef point/line-left
  :args (s/tuple ::point/point (s/with-gen
                                 integer?
                                 #(s/gen
                                   (s/long-in 0 100))))
  :ret (s/* ::point/point))

(s/fdef point/line-right
  :args (s/tuple ::point/point (s/with-gen
                                 integer?
                                 #(s/gen
                                   (s/long-in 0 100))))
  :ret (s/* ::point/point))


(s/fdef point/line-down
  :args (s/tuple ::point/point (s/with-gen
                                 integer?
                                 #(s/gen
                                   (s/long-in 0 100))))
  :ret (s/* ::point/point))

(s/fdef point/line-up
  :args (s/tuple ::point/point (s/with-gen
                                 integer?
                                 #(s/gen
                                   (s/long-in 0 100))))
  :ret (s/* ::point/point))