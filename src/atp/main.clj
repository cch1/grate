(ns atp.main
  (:require [atp.db :as db]
            [atp.glicko :as rater]
            [clojure.java.io :as io]))

;; Reference: http://www.glicko.net/glicko/glicko.pdf
;; https://github.com/JeffSackmann/tennis_atp

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
      (assoc record
             :week (quot (- (.toEpochDay date) d0) 7)
             :week-of (.adjustInto java.time.DayOfWeek/MONDAY date)))))

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

(defn advance-week
  [d]
  (.plusWeeks d 1))

(def default-rating [1500 350])

(def c (Math/sqrt (+ (* 50 50) 104))) ; Typical player (with a ratings deviation of 50) would be as unpredicatble as an unranked player after two years

(defn age-ranking [[player [rating deviation]]] [player [rating (max (min (Math/sqrt (+ (* deviation deviation) (* c c))) 350) 5)]])

(defn main
  []
  (let [[[epoch-start _] results] (db/load (io/file "results/atp_matches_2019.csv"))
        weeks (iterate advance-week epoch-start)
        results-by-week (group-by :week-of results)
        weekly-results (sequence (map (fn [week] [week (results-by-week week ())])) weeks)]
    (reductions (fn [rankings [week results]]
                  (let [aged-rankings (map age-ranking rankings)]
                    (reduce (fn [rankings {:keys [winner loser]}]
                              (-> rankings
                                  (update winner (fn [[rating deviation]] [(inc (or rating 1500)) (or deviation 350)]))
                                  (update loser (fn [[rating deviation]] [(dec (or rating 1500)) (or deviation 350)]))))
                            aged-rankings
                            results)))
                {}
                weekly-results)))
