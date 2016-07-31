(ns falx.scene.protocols)

(defprotocol INode
  (-layout [this result rect]))
