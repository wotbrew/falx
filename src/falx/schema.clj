(ns falx.schema
  (:require [schema.core :as s]))

(def Point
  [(s/one s/Int "i")
   (s/one s/Int "i")])

(def Camera
  s/Any)

(def Level
  s/Any)

(def Layer
  s/Any)

(def Cell
  {:level Level
   :point Point})

(def Slice
  {:level Level
   :layer Layer})

(def ActorType
  s/Any)

(def ActorID
  s/Any)

(def Actor
  {:id ActorID
   :type ActorType
   s/Any s/Any})

;; Goals

(def GoalType
  s/Any)

(def Goal
  {:type GoalType
   s/Any s/Any})

(def GoalMove
  {:type (s/eq :goal.type/move)
   :exclusive? (s/eq true)
   :cell Cell})

;; World

(def World
  s/Any)

;; Widgets

(def WidgetType
  s/Any)

(def Widget
  {:type WidgetType
   s/Any s/Any})

;; Messages

(def MessageType
  s/Any)

(def Message
  {:type MessageType
   s/Any s/Any})

(def Button
  s/Any)

;; Messages - Requests

(def RequestPrintMessage
  {:type (s/eq :request/print-message)
   :silent? (s/eq true)
   :message Message})

(def RequestPrintActor
  {:type (s/eq :request/print-actor)
   :actor Actor})

(def RequestMoveSelected
  {:type (s/eq :request/move-selected)
   :cell Cell})

(def RequestMove
  {:type (s/eq :request/move)
   :actor Actor
   :cell Cell})

(def RequestSelect
  {:type (s/eq :request/select)
   :actor Actor})

;; Messages - Actor Events

(def ActorEventPut
  {:type (s/eq :actor.event/put)
   :actor Actor
   :cell Cell})

(def ActorEventUnput
  {:type (s/eq :actor.event/unput)
   :actor Actor
   :cell Cell})

;; Messages - Creature Events

(def CreatureEventSelected
  {:type (s/eq :creature.event/selected)
   :actor Actor})

(def CreatureEventUnselected
  {:type (s/eq :creature.event/unselected)
   :actor Actor})

(def CreatureEventGoalGiven
  {:type  [(s/one (s/eq :creature.event/goal-given) "k")
           GoalType]
   :actor Actor
   :goal  Goal})

(def CreatureEventGoalRemoved
  {:type  [(s/one (s/eq :creature.event/goal-removed) "k")
           GoalType]
   :actor Actor
   :goal  Goal})

;; Messages - World Events

(def WorldEventActorChanged
  {:type (s/eq :world.event/actor-changed)
   :old-actor Actor
   :actor Actor
   :world World})

;; Messages - UI Events

(def UIEventClicked
  {:type    [(s/one (s/eq :ui.event/clicked) "k")
             WidgetType]
   :element Widget
   :point   Point
   :button  Button})

(def UIEventHoverEnter
  {:type    [(s/one (s/eq :ui.event/hover-enter) "k")
             WidgetType]
   :element Widget
   :point   Point})

(def UIEventHoverExit
  {:type    [(s/one (s/eq :ui.event/hover-exit) "k")
             WidgetType]
   :element Widget
   :point   Point})

(def UIEventWorldClicked
  {:type (s/either (s/eq :ui.event/world-clicked)
                   [(s/one (s/eq :ui.event/world-clicked) "k")
                    Button])
   :button Button
   :cell Cell})

(def UIEventActorClicked
  {:type (s/either (s/eq :ui.event/actor-clicked)

                   [(s/one (s/eq :ui.event/actor-clicked) "k")
                    Button]

                   [(s/one (s/eq :ui.event/actor-clicked) "k")
                    ActorType]

                   [(s/one (s/eq :ui.event/actor-clicked) "k")
                    ActorType
                    Button])
   :button Button
   :actor Actor})