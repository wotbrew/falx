(ns falx.engine.scene.protocols)

(defprotocol INode
  (-layout [this result rect]))

(defprotocol ISized
  (-size [this rect]))

(defprotocol IWrapMany
  (-children [this]))

(defprotocol IWrap
  (-child [this]))