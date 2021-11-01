(ns atp.glicko-test
  (:refer-clojure :exclude [update])
  (:require [atp.glicko :refer [update expected rankings]]
            [clojure.test :refer [deftest is]]))

(defn roughly [precision x y] (and (number? precision) (number? x) (number? y) (< (- x y) precision)))

(deftest update-ratings
  (let [db {:p [1500 200] :c1 [1400 30] :c2 [1550 100] :c3 [1700 300]}
        results [{:winner :p :loser :c1} {:winner :c2 :loser :p} {:winner :c3 :loser :p}]]
    (let [ratings (with-redefs [atp.glicko/decay-confidence identity] ; to match sample data which is pre-decayed
                    (update db results))]
      (is (roughly 1 1464 (get-in ratings [:p 0])))
      (is (roughly 0.1 151.4 (get-in ratings [:p 1]))))))

(deftest predict-expected-outcomes
  (is (roughly 0.001 0.376
               (expected {:p0 [1400 80] :p1 [1500 150]} :p0 :p1))))

(deftest produce-rankings
  (let [db {:p [1500 200] :c1 [1400 30] :c2 [1550 100] :c3 [1700 300]}
        results [{:winner :p :loser :c1} {:winner :c2 :loser :p} {:winner :c3 :loser :p}]]
    (let [rankings (rankings (update db results))]
      (is (= :c3 (ffirst rankings))))))
