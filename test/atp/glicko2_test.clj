(ns atp.glicko2-test
  (:refer-clojure :exclude [update])
  (:require [atp.glicko2 :refer [from-glicko update expected rankings]]
            [clojure.test :refer [deftest is]]))

(defn roughly [precision x y] (and (number? precision) (number? x) (number? y) (< (- x y) precision)))

(deftest update-ratings
  (let [db {:p [1500 200 0.06] :c1 [1400 30 0.06] :c2 [1550 100 0.06] :c3 [1700 300 0.06]}
        results [{:winner :p :loser :c1} {:winner :c2 :loser :p} {:winner :c3 :loser :p}]]
    (let [ratings (update db results)]
      (is (roughly 1 1464 (get-in ratings [:p 0])))
      (is (roughly 0.1 151.4 (get-in ratings [:p 1]))))))

(deftest predict-expected-outcomes
  (is (roughly 0.001 0.376
               (expected {:p0 [1400 80 0.06] :p1 [1500 150 1.0]} :p0 :p1))))

(deftest produce-rankings
  (let [db {:p [1500 200 0.06] :c1 [1400 30 0.06] :c2 [1550 100 0.06] :c3 [1700 300 0.06]}
        results [{:winner :p :loser :c1} {:winner :c2 :loser :p} {:winner :c3 :loser :p}]]
    (let [rankings (rankings (update db results))]
      (is (= :c3 (ffirst rankings))))))
