(ns atp-rankings
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]))

;; Reference: https://www.grandslamtennistours.com/wimbledon/schedule-of-play
(defn date-offset
  "Estimate the number of days after that start of the tournament a given match took place"
  [{:keys [draw level round]}]
  {:pre [(string? level) (string? round)] :post [(int? %)]}
  (assert (#{"128" "64" "32" "16" "8" "4"} draw) (format "Unknown draw: %s" draw))
  (if (or (= "F" level) (= "RR" round))
    0 ; Round Robin -no way to know the ordering AFAICT; same with Tour Finals (draw of eight)
    (let [offsets (case draw
                    ("32") {"F" 5 "SF" 3 "QF" 2 "R16" 1 "R32" 0}
                    ("64") {"F" 6 "SF" 4 "QF" 3 "R16" 2 "R32" 1 "R64" 0}
                    ("128") {"F" 13 "SF" 10 "QF" 8 "R16" 6 "R32" 4 "R64" 2 "R128" 0}
                    (constantly 0))]
      (assert (map? offsets) (format "%s %s %s" level draw round))
      (let [o (offsets round)]
        (assert (int? o) (format "%s %s %s %s" level draw round offsets))
        o))))

(defn estimate-match-date
  [{start :tournament-start :as record}]
  (let [offset (date-offset record)]
    (assoc record :date (java.time.LocalDate/ofEpochDay (+ (.toEpochDay start) offset)))))

(let [dtf java.time.format.DateTimeFormatter/BASIC_ISO_DATE]
  (defn make-date-hydrater
    [k]
    (fn [record] (update record k #(java.time.LocalDate/parse % dtf)))))

(defn make-week-calculator
  [epoch-start]
  (let [d0 (.toEpochDay (.adjustInto java.time.DayOfWeek/MONDAY epoch-start))] ; ranking week starts on Monday
    (fn [{date :date :as record}]
      (assoc record :week (quot (- (.toEpochDay date) d0) 7)))))

(defn record-maker
  [fields header]
  (let [indices (reduce (fn join [acc [column-name i]]
                          (if-let [k (fields column-name)]
                            (conj acc [i k])
                            acc))
                        []
                        (map vector header (range)))]
    (fn csv->record
      [data]
      (reduce (fn [acc [i k]] (assoc acc k (data i)))
              {}
              indices))))

(defn generate-id
  [{:keys [tournament match] :as record}]
  (assoc record :id (hash [tournament match])))

(def fields {"tourney_id"    :tournament
             "match_num"     :match
             "winner_name"   :winner
             "loser_name"    :loser
             "score"         :score
             "tourney_date"  :tournament-start
             "round"         :round
             "draw_size"     :draw
             "tourney_level" :level})

(defn main
  []
  (let [rankings    {}
        results     (with-open [reader (io/reader "results/atp_matches_2019.csv")]
                      (let [csv    (csv/read-csv reader)
                            xform  (comp (map (record-maker fields (first csv)))
                                         (map generate-id)
                                         (map (make-date-hydrater :tournament-start))
                                         (map estimate-match-date))]
                        (doall (eduction xform (rest csv)))))
        epoch-start (transduce (map :date) (fn
                                             ([] java.time.LocalDate/MAX)
                                             ([result] result)
                                             ([d0 d1] (if (pos? (compare d0 d1)) d1 d0)))
                               results)
        weekly-results     (group-by :week (map (make-week-calculator epoch-start) results))]
    (weekly-results 0)))
