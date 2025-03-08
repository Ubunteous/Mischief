(ns mischief.presentation.html
  (:require
   [clojure.string :as string]
   [hiccup2.core :as hiccup]
   [incanter.charts :as chart]
   [incanter.core :as graph]
   [mischief.presentation.html :as presentation]))

(defn view [& {:keys [body title]
               :or {title "The Website"}}]
  [:html
   [:head
    [:link {:rel "stylesheet", :href "/stylesheet.css"}]
    [:meta {:name "viewport"
            :content "width=device-width, initial-scale=1.0"
            :charset "UTF-8"}]]
   [:body body]])

(defn hello
  []
  (str
   (hiccup/html
    (presentation/view)
    [:h1 (str "Hello")])))

;;;;;;;;;;;;;;;;;;;;;
;; HTML GENERATION ;;
;;;;;;;;;;;;;;;;;;;;;

(defn show-res
  [name path]
  (str
   (hiccup/html
    [:h1 name]
    [:img {:src path}])))

(defn make-list
  [title rows]
  (str
   (hiccup/html
    (presentation/view)
    [:h1 title]

    (for [row rows]
      [:li (string/join
            " - "
            (remove nil? (vals row)))]))))

(defn make-table
  [name rows]
  (str
   (hiccup/html
    (presentation/view)
    [:h1 name]

    [:div
     [:table
      [:body
       ;; categories
       [:tr
        (for [categories (map symbol (keys (first rows)))]
          (into [:th]
                (map string/capitalize
                     (rest (string/split (str categories) #"/")))))]
       ;; content
       (for [row rows]
         (into [:tr]
               (let [replace-symbol {true "X" false "_" nil "null"}]
                 (map #(into [:th] (or (replace-symbol %) (str %)))
                      (vals row)))))]]])))

(defn make-bar-chart
  [rows]
  (let [axis (for [cols (keys (first rows))] (map cols rows))]

    (graph/save
     (chart/bar-chart
      (first axis)
      (second axis))
     "res/myplot.png"))
  (show-res "Bar Chart" "/myplot.png"))

(defn make-line-chart
  [rows]
  (let [axis (for [cols (keys (first rows))] (map cols rows))]

    (graph/save
     (chart/line-chart
      (first axis)
      (second axis))
     "res/myplot.png"))
  (show-res "Line Chart" "/myplot.png"))
