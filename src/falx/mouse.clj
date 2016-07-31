(ns falx.mouse
  (:require [falx.state :as state]
            [falx.gdx :as gdx]
            [falx.point :as pt]
            [falx.gdx.mouse :as gdx-mouse]))

(state/defsignal
  ::mouse
  (gdx/signal
    {::point @gdx-mouse/point}))

(defn in?
  [mouse rect]
  (pt/in? (::point mouse [0 0]) rect))