(ns falx.action)

(comment
  (declare
    all
    begin-turn
    end-turn
    select-creature
    move
    move-player
    show
    attack
    fire
    cast
    say
    open
    close
    quaff
    speak
    pickup
    drop
    equip
    unequip
    select-los
    select-adjacent
    look))

(defmulti run (fn [g action] (first action)))

(defmethod run :default
  [g _]
  g)

(defmethod run :all
  [g [_ & actions]]
  (reduce run g actions))

(defmacro bind
  [k f]
  `(let [f# ~f]
     (defmethod run ~k [g# [_# & args#]] (apply f# g# args#))))