(ns falx.frame
  (:require [falx.screen :as screen]
            [falx.engine.input :as input]
            [falx.gdx :as gdx]
            [falx.engine.keyboard :as keyboard]
            [falx.user :as user]))

(def ^:private debug-input
  (volatile! nil))

(def ^:private debug-screen
  (volatile! nil))

(def back-requested?
  (user/binding ::user/bind.back))

(def resolution-default
  (user/default-settings ::user/setting.resolution))

(def resolution-setting
  (user/setting ::user/setting.resolution))

(def initial-screen
  (screen/screen ::screen/id.menu resolution-default))

(def render
  (let [input-state (volatile! nil)
        screen-state (volatile! initial-screen)]
    (fn [world user]
      (vreset! debug-input @input-state)
      (vreset! debug-screen @screen-state)
      (let [input (vswap! input-state input/combine (input/now))
            _ (vswap! screen-state assoc
                      ::screen/size (resolution-setting user)
                      ::world world
                      ::user user
                      ::fps (gdx/fps)
                      ::frame-id (gdx/frame-id)
                      ::delta (gdx/delta-time))
            screen (vswap! screen-state screen/handle input)
            screen (if (back-requested? user input)
                     (vswap! screen-state screen/back)
                     screen)]
        (screen/draw! screen input)))))