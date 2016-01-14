(ns falx.game.solid
  (:require [falx.world :as world]))

(defn- solid-cell?*
  [world cell]
  (some :solid? (world/get-things-by-value world :cell cell)))

(defn solidity-changed
  [world cell]
  (let [solid? (solid-cell?* world cell)]
    (assoc-in world [::point-cache (:level cell) (:point cell)] solid?)))

(defn solid-cell?
  [world cell]
  (let [{:keys [level point]} cell]
    (boolean (-> world ::point-cache (get level) (get point)))))

(defn get-solid-point-pred
  [world level]
  (let [cache (-> world ::point-cache level)]
    (fn [point]
      (boolean (get cache point)))))

(world/defreaction
  :event.thing/unput
  ::thing-unput
  (fn [world {:keys [thing]}]
    (if (and (:solid? thing) (:cell thing))
      (solidity-changed world (:cell thing))
      world)))

(world/defreaction
  :event.thing/put
  ::thing-put
  (fn [world {:keys [thing cell]}]
    (if (:solid? thing)
      (cond-> (solidity-changed world cell)
              (:cell thing) (solidity-changed (:cell thing)))
      world)))

(world/defreaction
  :event.thing/removed
  ::thing-removed
  (fn [world {:keys [thing]}]
    (if (and (:solid? thing) (:cell thing))
      (solidity-changed world (:cell thing))
      world)))

(world/defreaction
  [:event.thing/attribute-removed :solid?]
  ::solid-removed
  (fn [world {:keys [thing]}]
    (if (:cell thing)
      (solidity-changed world (:cell thing))
      world)))

(world/defreaction
  [:event.thing/attribute-set :solid?]
  ::solid-set
  (fn [world {:keys [thing]}]
    (if (:cell thing)
      (solidity-changed world (:cell thing))
      world)))