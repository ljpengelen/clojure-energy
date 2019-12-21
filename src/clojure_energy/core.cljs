(ns clojure-energy.core
  (:require
    [clojure-energy.words :as w]
    [cljs.core.async :as a :refer [<! >! chan go put!]]
    [reagent.core :as r]))

(defonce view (r/atom :filter))

(defonce word-index (r/atom 0))

(defonce words-to-keep (r/atom []))

(defonce words-to-discard (r/atom []))

(defonce sorted-words (r/atom []))

(defonce option-a (r/atom ""))

(defonce option-b (r/atom ""))

(defn current-word [] (nth w/words @word-index))

(defn advance-word []
  (swap! word-index inc)
  (when (= @word-index (count w/words)) (reset! view :filter-summary)))

(defn keep-word []
  (swap! words-to-keep conj (current-word))
  (advance-word))

(defn discard-word []
  (swap! words-to-discard conj (current-word))
  (advance-word))

(defn word-list [type words]
  [type (map (fn [word] [:li {:key word} word]) words)])

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
  [:div
    (current-word)
    [:br]
    [:button {:on-click keep-word} "👍"]
    [:button {:on-click discard-word} "👎"]])

(defn start-sort [] (reset! view :sort))

(defn filter-summary-view []
  [:div
    [:p "Words to keep:"]
    (word-list :ul @words-to-keep)
    [:p "Words to discard:"]
    (word-list :ul @words-to-discard)
    [:button {:on-click start-sort} "Let's sort"]])

(defn sort-view []
  (let [res-c (async-sort @words-to-keep a-and-b a-over-b)]
    (update-a-and-b)
    (go (let [res (<! res-c)]
        (reset! sorted-words res)
        (reset! view :sorted-summary))))
  (fn []
    [:div
      [:button {:on-click prefer-a} @option-a]
      [:button {:on-click prefer-b} @option-b]]))

(defn sorted-summary-view []
    [:div
      [:p "Words in order:"]
      (word-list :ol @sorted-words)])

(defn page []
  [:div
    [:h1 "NRG"]
    (condp = @view
      :filter [filter-view]
      :filter-summary [filter-summary-view]
      :sort [sort-view]
      :sorted-summary [sorted-summary-view])])

(defn mount-root []
  (r/render [page] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
