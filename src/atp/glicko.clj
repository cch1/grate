;; Reference: http://www.glicko.net/glicko/glicko.pdf
(ns atp.glicko
  (:refer-clojure :exclude [update]))

(defn advance-week
  [d]
  (.plusWeeks d 1))

(def c (Math/sqrt (+ (* 50 50) 104))) ; Typical player (with a ratings deviation of 50) would be as unpredicatble as an unranked player after two years

(defn update-vals ; This will be obviated by a built-in function of the same name in Clojure 1.11.0
  "Update the values in `m` with the function `f`"
  [m f]
  (with-meta
    (persistent!
     (reduce-kv (fn [acc k v] (assoc! acc k (f v)))
                (if (instance? clojure.lang.IEditableCollection m)
                  (transient m)
                  (transient {}))
                m))
    (meta m)))

(let [default-rating [1500 350]] ; default values for unseen player
  (defn rating [db k] (get db k default-rating)))

(defn decay-confidence [[rating deviation]] [rating (max (min (Math/sqrt (+ (* deviation deviation) (* c c))) 350) 5)])

(def q (/ (Math/log 10) 400))

(defn g [rd] (/ 1 (Math/sqrt (+ 1 (/ (* 3 (* q q) (* rd rd)) (* Math/PI Math/PI))))))

(defn e* [[r rd] [rj rdj]] (/ 1 (+ 1 (Math/pow 10 (* -1 (g rdj) (- r rj) (/ 1 400))))))

(defn e [db k0 k1] (e* (rating db k0) (rating db k1)))

(defn d2 [db player results]
  (let [prating (rating db player)]
    (/ 1 (* q q (transduce (map (fn [[o _]] (let [[ro rdo :as orating] (rating db o)
                                                  e (e* prating orating)
                                                  g (g rdo)]
                                              (* g g e (- 1 e)))))
                           +
                           0
                           results)))))

(defn update-player [db [player results]]
  (let [[r rd :as prating] (rating db player)
        d2 (d2 db player results)
        r' (+ r (* (/ q (+ (/ 1 (* rd rd)) (/ 1 d2)))
                   (transduce (map (fn [[o s]] (let [[ro rdo :as orating] (rating db o)
                                                     e (e* prating orating)
                                                     g (g rdo)]
                                                 (* g (- s e)))))
                              +
                              0
                              results)))
        rd' (Math/sqrt (/ 1 (+ (/ 1 (* rd rd)) (/ 1 d2))))]
    [player [r' rd']]))

(defn- normalize-results
  [[player results]]
  [player (map (fn [{:keys [winner loser]}] (condp = player
                                              winner [loser 1]
                                              loser [winner 0]))
               results)])

(defn update
  "Update given ratings db with given results for next rating period"
  [db results]
  (let [db' (update-vals db decay-confidence)
        winners (group-by :winner results)
        losers (group-by :loser results)
        results-by-player (into {} (map normalize-results) (merge-with concat winners losers))]
    (into {} (map (partial update-player db')) results-by-player)))

;; Predictions
(defn expected* [[ri di] [rj dj]] (/ 1 (+ 1 (Math/pow 10 (* -1 (g (Math/sqrt (+ (* di di) (* dj dj)))) (/ (- ri rj) 400))))))
(defn expected [db k0 k1] (expected* (rating db k0) (rating db k1)))
