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

;; Input

(def Key
  s/Any)

(def Button
  s/Any)

(def Keyboard
  {:hit #{Key}
   :pressed #{Key}})

(def Mouse
  {:point Point
   :hit #{Button}
   :pressed #{Button}
   :delta s/Num})

(def Input
  {:keyboard Keyboard
   :mouse Mouse})

;; Frame

(def Frame
  {:delta s/Num
   :fps s/Int
   :display s/Any
   :input Input
   :world World})

;; Messages - Requests

(def RequestPrintMessage
  {:type (s/eq :request/print-message)
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

(def RequestSpawnAI
  {:type (s/eq :request/spawn-ai)
   :actor Actor})

(def RequestTickAI
  {:type (s/eq :request/tick-ai)
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

(def WorldEventActorCreated
  {:type (s/either (s/eq :world.event/actor-created)
                   [(s/one (s/eq :world.event/actor-created) "k")
                    ActorType])
   :actor Actor
   :world World})

(def WorldEventActorChanged
  {:type      (s/either (s/eq :world.event/actor-changed)
                        [(s/one (s/eq :world.event/actor-changed) "k")
                         ActorType])
   :old-actor Actor
   :actor     Actor
   :world     World})

;; Messages - Game Events

(def GameEventClosing
  {:type (s/eq :game.event/closing)
   :id s/Str})

(def GameEventStarting
  {:type (s/eq :game.event/starting)
   :id s/Str})

(def GameEventFrame
  {:type (s/eq :game.event/frame)
   :frame Frame})

;; Messages - UI Events

(def UIEventCreatureSelected
  {:type (s/eq :ui.event/creature-selected)
   :actor Actor})

(def UIEventCreatureUnselected
  {:type (s/eq :ui.event/creature-unselected)
   :actor Actor})

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