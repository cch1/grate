;; Reference: http://www.glicko.net/glicko/glicko2.pdf
(ns atp.glicko2
  (:refer-clojure :exclude [update]))

(let [x 173.7178]
  (defn from-glicko
    "Convert a player rating on the Glicko scale to the Glicko2 scale"
    [[r rd & rest]]
    (apply vector (/ (- r 1500) x) (/ rd x) rest))

  (defn to-glicko
    "Convert a player rating on the Glicko2 scale to the Glicko scale"
    [[mu phi sigma]]
    [(+ (* x mu) 1500) (* x phi)]))

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

(def tau "System volatility constraint" 0.5)

(let [default-rating (from-glicko [1500 350 0.06])] ; default values for unseen player
  (defn rating [db k] (get db k default-rating)))

(defn g [phi] (/ 1 (Math/sqrt (+ 1 (/ (* 3 (* phi phi)) (* Math/PI Math/PI))))))

(defn e [[mu phi sigma :as prating] [mu_j phi_j sigma_j :as orating]]
  (/ 1 (+ 1 (Math/exp (* -1 (g phi_j) (- mu mu_j))))))

(defn v
  "Estimated variance of `player`'s rating based on `results`"
  [db player results]
  (let [[mu phi _ :as prating] (rating db player)]
    (/ 1 (transduce (comp (map (fn [[j _]] (let [[mu_j phi_j _ :as orating] (rating db j)
                                                 e (e prating orating)
                                                 g (g phi_j)]
                                             (* g g e (- 1 e))))))
                    +
                    results))))

(defn delta
  "Estimated improved in rating"
  [db player results]
  (let [[mu phi _ :as prating] (rating db player)]
    (* (v db player results)
       (transduce (comp (map (fn [[j s]] (let [[mu_j phi_j _ :as orating] (rating db j)
                                               e (e prating orating)
                                               g (g phi_j)]
                                           (* g (- s e))))))
                  +
                  results))))

(let [epsilon 0.000001] ; convergence tolerance
  (defn sigma'
    "Iteratively compute sigma' from phi using the Illinois Algorithm"
    [sigma phi delta v]
    (let [a (Math/log (* sigma sigma))
          y (+ (* phi phi) v)
          f (fn [x] (let [etox (Math/exp x)
                          z (+ y etox)]
                      (- (/ (* etox (- (* delta delta) z))
                            (* 2 z z))
                         (/ (- x a)
                            (* tau tau)))))
          b (if (> (* delta delta) y)
              (Math/log (- (* delta delta) y))
              (loop [k 1] (let [x (- a (* k tau))]
                            (if (neg? (f x)) (recur (inc k)) x))))]
      (loop [A a fA (f a) B b fB (f b)]
        (if (> (Math/abs (- A B)) epsilon)
          (let [C (+ A (/ (* (- A B) fA) (- fB fA)))
                fC (f C)
                [A' fA' B' fB'] (if (neg? (* fC fB))
                                  [B fB C fC]
                                  [A (/ fA 2) C fC])]
            (recur A' fA B' fB))
          (Math/exp (/ a 2)))))))

(defn phi*
  "Decay confidence by increasing the rating deviation phi"
  [phi sigma'] (Math/sqrt (+ (* phi phi) (* sigma' sigma'))))

(defn update-player [db [player results]]
  (let [[mu phi sigma :as prating] (rating db player)
        v (v db player results)
        delta (delta db player results)
        sigma' (sigma' sigma phi delta v)
        phi* (phi* phi sigma')
        phi' (/ 1 (Math/sqrt (+ (/ 1 (* phi* phi*)) (/ 1 v))))
        mu' (+ mu (* phi' phi'
                     (transduce (map (fn [[o s]] (let [[mu_j phi_j sigma_j :as orating] (rating db o)
                                                       e (e prating orating)
                                                       g (g phi_j)]
                                                   (* g (- s e)))))
                                +
                                results)))]
    [player [mu' phi' sigma']]))

(defn- normalize-results
  [[player results]]
  [player (map (fn [{:keys [winner loser]}] (condp = player
                                              winner [loser 1]
                                              loser [winner 0]))
               results)])

(defn update
  "Update given ratings db with given results for next rating period"
  [db results]
  (let [db' (update-vals db from-glicko)
        winners (group-by :winner results)
        losers (group-by :loser results)
        results-by-player (merge-with concat winners losers)]
    (update-vals (into {} (comp (map normalize-results) (map (partial update-player db'))) results-by-player) to-glicko)))

(defn rankings [db]
  (sort-by (comp #(* -1 %) first val) db))

;; Predictions
(defn expected* [[ri di] [rj dj]] (/ 1 (+ 1 (Math/pow 10 (* -1 (g (Math/sqrt (+ (* di di) (* dj dj)))) (/ (- ri rj) 400))))))
(defn expected [db k0 k1] (expected* (rating db k0) (rating db k1)))
