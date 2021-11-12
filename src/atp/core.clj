(ns atp.core
  (:require [atp.db :as db]
            [atp.glicko :as rater]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io])
  (:import [java.time LocalDate]))

(defn- advance-week
  [d]
  (.plusWeeks d 1))

(defn emit
  [writer rankings]
  (csv/write-csv writer rankings))

(defn rate
  [reader]
  (let [[[epoch-start epoch-end] results] (db/load reader)
        weeks (inc (quot (- (.toEpochDay epoch-end) (.toEpochDay epoch-start)) 7))
        weeks (take weeks (iterate advance-week epoch-start))
        results-by-week (group-by :week-of results)
        weekly-results (sequence (map (fn [week] [week (results-by-week week ())])) weeks)]
    (rest (reductions (fn [[_ ratings] [week results]]
                        [week (rater/update ratings results)])
                      [nil {}] weekly-results))))

;; This is the main entry point for CLI usage
(defn main
  [& args]
  (let [ratings (rate *in*)
        rankings (map (fn [[week ratings]] (rater/rankings ratings)) ratings)]
    (emit *out* (last rankings))))

(defn event03
  [{field-name 'field-name record-index 'record-index :as args}]
  (let [db (csv/read-csv *in*)
        record (nth db record-index)]
    (prn ((zipmap (first db) record) (str field-name)))))

(defn event04
  [& args]
  (let [[[epoch-start epoch-end] results] (db/load *in*)
        num-weeks (inc (quot (- (.toEpochDay epoch-end) (.toEpochDay epoch-start)) 7))
        weeks (take num-weeks (iterate advance-week epoch-start))
        results-by-week (group-by :week-of results)
        weekly-results (sequence (map (fn [week] [week (results-by-week week ())])) weeks)
        [ampw mmpw pactivity] (reduce (fn [acc [week results]] (let [n (count results)
                                                                     winners (frequencies (map :winner results))
                                                                     losers (frequencies (map :loser results))]
                                                                 (-> acc
                                                                     (update 0 + n)
                                                                     (update 1 (fn [max] (if (> max n) max n)))
                                                                     (update 2 (fn [pactivity] (merge-with + pactivity winners losers))))))
                                      [0 0 {}]
                                      weekly-results)
        most-actives (last (reduce (fn [[max-matches most-actives] [player matches]] (case (int (Math/signum (float (compare matches max-matches))))
                                                                                       -1 [max-matches most-actives]
                                                                                       0 [max-matches (conj most-actives player)]
                                                                                       1 [matches [player]]))
                                   [0 []]
                                   pactivity))]
    (println (str epoch-start)
             (str epoch-end)
             (int (/ ampw num-weeks))
             mmpw
             most-actives)))
