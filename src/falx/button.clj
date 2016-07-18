(ns falx.button)

(def gdx-button->button
  {:left ::mouse.left
   :right ::mouse.right})

(defn gdx-buttons->buttons
  [coll]
  (into #{} (keep gdx-button->button) coll))

(def gdx-key->button
  {})

(defn- flip-map
  [m]
  (reduce-kv (fn [m k v] (assoc m v k)) {} m))

(def button->gdx
  (flip-map gdx-button->button))

(defn next-state
  [state {:keys [pressed? hit? delta-time]}]
  {::button (::button state)
   ::pressed? pressed?
   ::pressed-time (if (and pressed? (::pressed? state))
                    (+ (::pressed-time state) delta-time)
                    0)
   ::hit? hit?})

(defn initial-state
  [button]
  {::button button
   ::pressed? false
   ::pressed-time 0
   ::hit? false})