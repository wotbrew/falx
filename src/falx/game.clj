(ns falx.game
  (:require [clojure.core.async :as async]))

(defn out
  [gs f]
  (let [out-id (::out-index gs 0)]
    (-> gs
        (assoc ::out-index (inc out-id))
        (assoc-in [::out out-id] {::out.fn f
                                  ::out.id out-id}))))

(defonce state
  (volatile! nil))

(defonce in
  (let [c (async/chan 32)]
    (async/go-loop
      [out-index 0]
      (when-some [[r f] (async/<! c)]
        (let [ret (try (vswap! state f) (catch Throwable e nil))
              new-out-index (::out-index ret 0)]
          (when r
            (async/>! r ret))
          (if (< out-index new-out-index)
            (let [outs (select-keys (::out ret) (range out-index new-out-index))]
              (doseq [o (vals outs)
                      :let [id (::out.id o)
                            fn (::out.fn o)]]
                (async/go (try (fn) (catch Throwable e ))
                          (async/>! c [nil #(update % ::out dissoc id)])))
              (recur new-out-index))
            (recur new-out-index)))))
    c))

(defn req
  [f]
  [(async/promise-chan) f])

(defn gswap!
  ([f]
    (let [req (req f)
          [ret] req]
      (async/>!! in req)
      ret))
  ([f & args]
    (gswap! #(apply f % args))))


