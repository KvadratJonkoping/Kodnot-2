(ns not2.core
  (:gen-class)
  (:require   [clojure.string :as str]
              [clojure.walk :as walk]
              [clj-time.core :as t]
              [clj-time.format :as f]
              [clj-time.coerce :as c]))

(defn read-file [path]
  "Läs upp innehållet i filen"
  (assoc {} :contents
            (slurp path)))

(defn split-lines [data]
  "Splitta innehållet per rad"
  (assoc data :lines
              (str/split-lines
                (get data :contents))))

(defn split-keys [data]
  "Splitta kolumnerna för rubrikraden"
  (assoc data :keys
              (map keyword
                   (str/split
                     (first
                       (get data :lines)) #";"))))

(defn split-values [data]
  "Splitta raderna med åkare per kolumn"
  (assoc data :values
              (map (fn [x] (str/split x #";")) (rest (get data :lines)))))

(defn combine-keys-and-values [keys values]
  "Sätt ihop nycklar och värden till en map för en åkare"
  (map (fn [x y] [x y]) keys values))

(defn to-map [data]
  "Sätt ihop nycklar och värden till mappar för alla åkare"
  (assoc data :mapped-values
              (map
                (fn [x]
                  (into {}
                        (combine-keys-and-values
                          (get data :keys) x)))
                  (get data :values))))

(defn key-value-åkare [coll data]
  (assoc data :keyvalue-åkare (into {} (map (fn [åkare] (hash-map (get åkare :bib) åkare)) (get data :mapped-values)))))

(defn sort-åkare-by-klubb [data]
  (assoc data :sorted-åkare (into [] (sort-by (fn [x] (get (second x) :klubb)) (get data :keyvalue-åkare)))))

(defn group-by-klubb [data]
  "Gruppera åkarna efter klubb"
  (assoc data :by-klubb (into [] (partition-by (fn [x] (get (second x) :klubb)) (get data :sorted-åkare)))))


(defn sort-partions-by-time [data]
  "Sortera grupperna efter tid"
  (assoc data :sorted (map (fn [x]
                             (sort-by (fn [y]
                                        (:totaltid (second y))) x))
                           (get data :by-klubb))))

(defn key-value-klubb [coll data]
  (assoc data :keyvalue-klubb (into {} (map
                                         (fn [x] (assoc coll (get (second (first x)) :klubb) x))
                                         (get data :sorted)))))

(defn remove-sequences [data]
  (assoc data :no-sequences (walk/postwalk #(if (sequential? %) (vec %) %) (get data :keyvalue-klubb))))


(defn find-next-without-team [klubb, num]
  "Hitta den snabbaste åkare som ännu inte tillhör ett lag"
  (if (< num (count klubb))
    (if
      (contains? (second (nth klubb num)) :team)
      (find-next-without-team klubb (inc num))
      num
      )
    -1))

(defn find-next-without-team-by-kön [klubb num kön]
  "Hitta den första åkaren av ett specifikt kön som ännu inte tillhör ett lag"
  (if (= -1 (find-next-without-team klubb num))
    -1
    (if (= kön (get (second (nth klubb num)) :kön))
      num
      (find-next-without-team-by-kön klubb (inc num) kön))))


(defn add-first-member
  ([klubb team]
   (if (= -1 (find-next-without-team klubb 0))
     klubb
     (assoc (second (nth klubb (find-next-without-team klubb 0))) :team team)))
  ([klubb team kön]
   (if (= -1 (find-next-without-team-by-kön klubb 0 kön))
     klubb
     (assoc (second (nth klubb (find-next-without-team-by-kön klubb 0 kön))) :team team))))


(defn update-team [klubb åkare num team]
  (if (= -1 num)
    klubb
    (update-in klubb [num 1] assoc :team team)))

(defn add-member-to-team
  ([klubb team]
   (update-team klubb
                (add-first-member
                  klubb
                  team)
                (find-next-without-team klubb 0)
                  team))
  ([klubb team kön]
   (update-team klubb
                (add-first-member
                  klubb
                  team)
                (find-next-without-team-by-kön klubb 0 kön)
                team)))

(defn add-first-two-members-to-team [klubb team]
  (add-member-to-team (add-member-to-team klubb team) team))

(defn first-two-in-team [klubb team]
  (filter (fn [x] (= team (get (second x) :team))) klubb))

(defn missing-gender-in-team [klubb team]
  (if (= (get (second (first (first-two-in-team klubb team))) :kön) (get (second (second (first-two-in-team klubb team))) :kön))
    (if (= "H" (get (second (first (first-two-in-team klubb team))) :kön))
      "D"
      "H")
    :inget))

(defn add-last-member
  ([klubb team]
   (add-member-to-team klubb team))
  ([klubb team kön]
   (add-member-to-team klubb team kön))
  )

(defn add-third-member-to-team [klubb team]
  (if (= :inget (missing-gender-in-team klubb team))
    (add-last-member klubb team)
    (if (= "H" (missing-gender-in-team klubb team))
      (add-last-member klubb team "H")
      (add-last-member klubb team "D"))))

(defn not-contains? [data key]
  (not (contains? data key)))

(defn create-team [klubb team]
  (add-third-member-to-team (add-first-two-members-to-team klubb team)  team))


(defn add-teams-to-klubb [klubb team]
  (if (empty? (filter (fn [akare] (not-contains? (second akare) :team))  klubb))
    klubb
    (add-teams-to-klubb (create-team klubb team) (inc team))))

(defn add-teams-to-all-klubb [data]
  (map (fn [x] (add-teams-to-klubb (second x) 0)) (data :no-sequences)))

(defn flatten-akare-teams [data]
  (filter map? (flatten (add-teams-to-all-klubb data))))

(defn is-three [data]
  (= 3 (count (second data))))

(defn remove-non-teams [data]
  (into {} (filter (fn [x] (is-three x)) (sort (group-by (juxt :klubb :team) (flatten-akare-teams data))))))

(defn to-date [date-string]
  (f/parse (f/formatter "HH:mm:ss.SSS") date-string))

(defn to-long [date]
  (c/to-long date))

(defn sum-team-times [team]
  (reduce
    (fn [x y]
      (+ x
         (to-long
           (to-date (:totaltid  y)))))
    0
    team))

(defn get-team-times [data]
  (sort-by (fn [x] (nth x 2)) (map
    (fn [x]
      (update-in x [2]  :num (sum-team-times
                                           (second
                                             x))))
    data)))

(defn klubb-namn [klubb]
  (first (first klubb)))

(defn åkare-namn [klubb index]
  (str (:fornamn (nth (second klubb) index)) " " (:efternamn (nth (second klubb) index))))

(defn format-time [time-in-millis]
  (f/unparse (f/formatter "HH:mm:ss.SSS") (c/from-long time-in-millis)))

(defn print-result [data]
  (map (fn [x]
         (println
           (klubb-namn x)
           (åkare-namn x 0)
           (åkare-namn x 1)
           (åkare-namn x 2)
           (format-time (nth x 2))
           )) data))

(defn -main []
       (print-result
         (get-team-times
           (remove-non-teams
             (remove-sequences
               (key-value-klubb {}
                                (sort-partions-by-time
                                  (group-by-klubb
                                    (sort-åkare-by-klubb
                                      (key-value-åkare {}
                                                       (to-map
                                                         (split-values
                                                           (split-keys
                                                             (split-lines
                                                               (read-file "/home/roland/git/Kodnot-2/resultat.csv")))))))))))))))

