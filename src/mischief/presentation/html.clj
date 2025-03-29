(ns mischief.presentation.html
  (:require
   [clojure.string :as string]
   [hiccup2.core :as hiccup]
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

;;;;;;;;;;;;;;;;;;;;;
;; HTML GENERATION ;;
;;;;;;;;;;;;;;;;;;;;;

(defn show-res
  [name path]
  (str
   (hiccup/html
    [:h1 name]
    [:img {:src path}])))

(defn make-content
  [content]
  (str
   (hiccup/html
    (presentation/view)
    content)))

(defn make-form
  [method action value]
  (str
   (hiccup/html
    [:form {:method method
            :action action}
     [:input {:value value :type "submit"}]])))

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
                     ;; (rest (string/split (str categories) #"/"))) ;; jdbc
                     (str categories))))]

	   ;; content
       (for [row rows]
         (into [:tr]
               (let [replace-symbol {true "X" false "_" nil "null"}]
                 (map #(into [:th] (or (replace-symbol %) (str %)))
                      (vals row)))))]]])))
