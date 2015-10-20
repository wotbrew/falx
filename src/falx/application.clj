(ns falx.application
  (:require [clojure.tools.logging :refer [error]])
  (:import (com.badlogic.gdx ApplicationListener Gdx)
           (com.badlogic.gdx.backends.lwjgl LwjglApplication)))


(def ^:dynamic *on-render-thread* false)

(defn render-call
  [f]
  (binding [*on-render-thread* true]
    (try
      (f)
      (catch Throwable e
        (error e "An error occurred running frame")
        (Thread/sleep 5000)))))

(defn on-render-thread-call
  [f]
  (let [p (promise)
        f' (fn [] (deliver p (try (f) (catch Throwable e [::error e]))))]
    (.postRunnable Gdx/app f')
    (let [r @p]
      (if (and (vector? r) (= (first r) ::error))
        (throw (second r))
        r))))

(defmacro on-render-thread
  [& body]
  `(if *on-render-thread*
     (do ~@body)
     (on-render-thread-call (fn [] ~@body))))

(defn listener
  [render-fn]
  (proxy
    [ApplicationListener]
    []
    (render []
      (render-call render-fn))
    (create [])
    (resize [w h])
    (pause [])
    (resume [])
    (dispose [])))

(defn application
  [render-fn]
  (LwjglApplication. (listener render-fn)))
