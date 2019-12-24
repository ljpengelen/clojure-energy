(ns clojure-energy.core
  (:require
    [clojure-energy.words :as w]
    [cljs.core.async :as a :refer [<! >! chan go put!]]
    [reagent.core :as r]))

(defonce view (r/atom :filter))

(defonce word-index (r/atom 0))

(def words (shuffle w/words))
(defonce partitioned-words (r/atom (zipmap words (repeat false))))

(defonce sorted-words (r/atom []))

(defonce option-a (r/atom ""))

(defonce option-b (r/atom ""))

(defn advance-word []
  (swap! word-index inc)
  (when (= @word-index (count words)) (reset! view :filter-summary)))

(defn keep-word [word]
  (swap! partitioned-words assoc word true)
  (advance-word))

(defn discard-word [word]
  (swap! partitioned-words assoc word false)
  (advance-word))

(defn async-merge
  ([left-c right-c opts-c prefs-c]
    (let [out-c (chan)]
      (go
        (let [left (<! left-c) right (<! right-c)]
          (async-merge left right opts-c prefs-c [] out-c)))
      out-c))
  ([[l & *left :as left] [r & *right :as right] opts-c prefs-c acc out-c]
    (if (and (not-empty left) (not-empty right))
      (go
          (>! opts-c [l r])
          (if (<! prefs-c)
            (async-merge *left right opts-c prefs-c (conj acc l) out-c)
            (async-merge left *right opts-c prefs-c (conj acc r) out-c)))
      (put! out-c (concat acc left right)))))

(defn async-sort [vals opts-c prefs-c]
  (if (> (count vals) 1)
    (let [[left right] (split-at (/ (count vals) 2) vals)]
      (async-merge
        (async-sort left opts-c prefs-c)
        (async-sort right opts-c prefs-c)
        opts-c prefs-c))
    (let [out-c (chan)]
      (put! out-c vals)
      out-c)))

(def a-and-b (chan))
(def a-over-b (chan))

(defn update-a-and-b []
  (a/go
    (let [[a b] (<! a-and-b)]
    (reset! option-a a)
    (reset! option-b b))))

(defn prefer-a [] (put! a-over-b true) (update-a-and-b))
(defn prefer-b [] (put! a-over-b false) (update-a-and-b))

(defn filter-view []
  (let [word (nth words @word-index)]
    [:div
      [:p
       "Hieronder verschijnen één voor één " (count words) " woorden. "
       "Gebruik de knoppen om voor elk woord aan te geven of het voor jou een energiewoord is. "
       "Achteraf is er nog de mogelijkheid om eventuele fouten te herstellen."]
      word
      [:br]
      [:button {:on-click #(keep-word word)} "👍"]
      [:button {:on-click #(discard-word word)} "👎"]]))

(defn word-list [type words controls]
  [type (map-indexed (fn [i word] [:li {:key word} word (controls word i)]) words)])

(defn start-sort [] (reset! view :sort))

(defn words-to-keep [words]
  (->> (seq words)
      (filter (fn [[k v] p] (= v true)))
      (map (fn [[k v] p] k))))

(defn words-to-discard [words]
  (->> (seq words)
      (filter (fn [[k v] p] (= v false)))
      (map (fn [[k v] p] k))))

(defn filter-summary-view []
  [:div
    [:p
      "Ben je tevreden met de onderstaande indeling? Zo niet, "
      "dan kun je fouten corrigeren met de knoppen achter de woorden."]
    [:p
      "Hoe meer energiewoorden je kiest, hoe meer werk je straks hebt om ze sorteren. "
      "Het loont dus de moeite om selectief te zijn."]
    [:p "Energiewoorden:"]
    (word-list :ul (words-to-keep @partitioned-words) (fn [word] [:button {:on-click #(discard-word word)} "👎"]))
    [:p "Afvallers:"]
    (word-list :ul (words-to-discard @partitioned-words) (fn [word] [:button {:on-click #(keep-word word)} "👍"]))
    [:p "Als je tevreden bent deze indeling, dan kun je nu je energiewoorden gaan sorteren."]
    [:button {:on-click start-sort} "Sorteren maar"]])

(defn sort-view []
  (let [res-c (async-sort (words-to-keep @partitioned-words) a-and-b a-over-b)]
    (update-a-and-b)
    (go (let [res (<! res-c)]
        (reset! sorted-words (into [] res))
        (reset! view :sorted-summary))))
  (fn []
    [:div
      [:p
        "Hieronder verschijnen steeds twee woorden. "
        "Kies voor elk tweetal woorden het woord dat je voorkeur heeft als energiewoord. "
        "Achteraf is er weer de mogelijkheid om fouten te corrigeren."]
      [:button {:on-click prefer-a} @option-a]
      [:button {:on-click prefer-b} @option-b]]))

(defn swap [v i1 i2] (assoc v i1 (v i2) i2 (v i1)))

(defn down [i] (swap! sorted-words swap i (inc i)))

(defn up [i] (swap! sorted-words swap i (dec i)))

(defn sorted-summary-view []
  (let [last (dec (count @sorted-words))]
    [:div
      [:p
        "Hieronder staan je energiewoorden in de volgorde van jouw voorkeur. "
        [:span
         { :class "unprintable"}
         "Je kunt nog aanpassingen doen met de knoppen achter de woorden."]]
      (word-list :ol @sorted-words
        (fn [word i]
          [:span
            (if (pos? i) [:button {:on-click #(up i)} "↑"] nil)
            (if (< i last) [:button {:on-click #(down i)} "↓"] nil)]))]))

(defn page []
  [:div
    [:h1 "NRG"]
    (case @view
      :filter [filter-view]
      :filter-summary [filter-summary-view]
      :sort [sort-view]
      :sorted-summary [sorted-summary-view])])

(defn mount-root []
  (r/render [page] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
