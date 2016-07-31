(ns falx.gdx.impl.signal
  (:import (clojure.lang IDeref IFn)
           (com.badlogic.gdx Gdx)))

(defmacro signal
  ([& body]
   `(let [f# (fn [] ~@body)
          cache# (volatile! {:frame-id nil
                             :value nil})]
      (reify
        IDeref
        (deref [this#]
          (let [fid# (when-some [gfx# (when Gdx/app
                                       (.getGraphics Gdx/app))]
                       (.getFrameId gfx#))
                m# @cache#]
            (if (= (:frame-id m#) fid#)
              (:val m#)
              (-> (vreset! cache# {:frame-id fid#
                                   :val (f#)})
                  :val))))
        IFn
        (invoke [this#]
          @this#)
        Object
        (toString [this#]
          (str @this#))))))
