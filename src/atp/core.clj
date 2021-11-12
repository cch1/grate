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

