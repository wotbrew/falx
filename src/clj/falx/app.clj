(ns falx.app)

(defrecord AppState [game-state
                     ui-state
                     ;;map of key to val
                     settings])