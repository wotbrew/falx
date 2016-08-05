(ns falx.time
  (:require [falx.state :as state]
            [falx.gdx :as gdx]))

(state/defsignal
  ::delta
  gdx/delta-time)