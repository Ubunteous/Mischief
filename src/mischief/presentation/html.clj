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
    [:style
     "html, body {height: 100%;}\n
      html {display: table; margin: auto;}\n
      body {display: table-cell; vertical-align: middle;}\n
      table, th, td {border: 1px solid black;}\n
      h1 {color: blue;}"]

    [:h1 name]

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
              (map #(into [:th] (str %))
                   (replace {nil "null"} (vals row)))))]])))

(defn make-bar-chart
  [rows]
  (let [axis (for [cols (keys (first rows))] (map cols rows))]

    ;; (graph/view
    (graph/save
     (chart/bar-chart
      (first axis)
      (second axis))
     ;; (FileOutputStream. "res/myplot.png"))
     "res/myplot.png")

    (str
     (hiccup/html
      [:h1 (str "Image")]
      [:img {:src "/myplot.png"}]))))
