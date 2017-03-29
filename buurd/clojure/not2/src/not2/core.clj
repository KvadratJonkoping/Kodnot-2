(ns not2.core
  (:gen-class)
  (:require [clojure.string :as str]))



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
                (rest
                  (get data :values)))))

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
  (assoc data :no-sequences (clojure.walk/postwalk #(if (sequential? %) (vec %) %) (get data :keyvalue-klubb))))


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
   (assoc (second (nth klubb (find-next-without-team klubb 0))) :team team))
  ([klubb team kön]
   (assoc (second (nth klubb (find-next-without-team-by-kön klubb 0 kön))) :team team)))


(defn update-team [data klubb åkare num]
  (update-in data [:no-sequences klubb num 1] assoc :team "A"))

(defn add-member-to-team
  ([data klubb team]
   (update-team data klubb
                (add-first-member
                  (get
                    (get data :no-sequences) klubb)
                  team)
                (find-next-without-team (get (get data :no-sequences) klubb) 0)))
  ([data klubb team kön]
   (update-team data klubb
                (add-first-member
                  (get (get data :no-sequences) klubb)
                  team)
                (find-next-without-team-by-kön (get (get data :no-sequences) klubb) 0 kön))))

(defn add-first-two-members-to-team [data klubb team]
  (add-member-to-team (add-member-to-team data klubb team) klubb team))

(defn first-two-in-team [data klubb team]
                   (filter (fn [x] (= team (get (second x) :team))) (get (get data :no-sequences) klubb)))

(defn missing-gender-in-team [data klubb team]
  (if (= (get (second (first (first-two-in-team data klubb team))) :kön) (get (second (second (first-two-in-team data klubb team))) :kön))
    (if (= "H" (get (second (first (first-two-in-team data klubb team))) :kön))
      "D"
      "H")
    :inget))

(defn add-last-member
  ([data klubb team]
   (add-member-to-team data klubb team))
  ([data klubb team kön]
   (add-member-to-team data klubb team kön))
  )

(defn add-third-member-to-team [data klubb team]
  (if (= :inget (missing-gender-in-team data klubb team))
    (add-last-member data klubb team)
    (if (= "H" (missing-gender-in-team data klubb team))
      (add-last-member data klubb team "H")
      (add-last-member data klubb team "D"))))


(def data
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
                                                    (read-file "/home/roland/git/Kodnot-2/resultat.csv"))))))))))))

(def data2 (add-third-member-to-team (add-first-two-members-to-team data "Klubb 6" "A")  "Klubb 6" "A"))