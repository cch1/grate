(ns atp.core-test
  (:refer-clojure :exclude [update])
  (:require [atp.core :refer [main]]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest is]])
  (:import [java.io File]))

(deftest run-as-cli-utility
  (let [outfile (File/createTempFile "test" ".csv")]
    (with-open [reader (io/reader (io/resource "atp_matches_2020_sample.csv") :encoding "UTF-8")
                writer (io/writer outfile :encoding "UTF-8")]
      (binding [*in* reader *out* writer]
        (main {})))
    (let [result (csv/read-csv (slurp outfile))]
      (is (#{"Roberto Bautista Agut" "Novak Djokovic"} (ffirst result))))))
