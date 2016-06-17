(ns falx.rect.spec
  (:require [falx.rect :as rect]
            [clojure.spec :as s]))

(s/def ::rect/rect
  (s/tuple integer? integer? integer? integer?))

(s/fdef rect/add
  :args (s/or
          :arity0 #{[]}
          :arity1 (s/tuple ::rect/rect)
          :arity2 (s/tuple ::rect/rect ::rect/rect)
          :arityn (s/* ::rect/rect))
  :ret ::rect/rect)

(s/fdef rect/shift
  :args (s/tuple ::rect/rect integer?)
  :ret ::rect/rect)

(s/fdef rect/lshift
  :args (s/tuple ::rect/rect integer?)
  :ret ::rect/rect)

(s/fdef rect/mult
  :args (s/or
          :arity0 #{[]}
          :arity1 (s/tuple ::rect/rect)
          :arity2 (s/tuple ::rect/rect ::rect/rect)
          :arityn (s/* ::rect/rect))
  :ret ::rect/rect)

(s/fdef rect/sub
  :args (s/or
          :arity0 #{[]}
          :arity1 (s/tuple ::rect/rect)
          :arity2 (s/tuple ::rect/rect ::rect/rect)
          :arityn (s/* ::rect/rect))
  :ret ::rect/rect)

(s/fdef rect/scale
  :args (s/tuple ::rect/rect integer?)
  :ret ::rect/rect)

(s/fdef rect/expand
  :args (s/or
          :arity2 (s/tuple ::rect/rect integer?)
          :arity3 (s/tuple ::rect/rect integer? integer?))
  :ret ::rect/rect)