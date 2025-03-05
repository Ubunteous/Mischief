(ns mischief.shared-html.core)

(defn view [& {:keys [body title]
               :or {title "The Website"}}]
  [:html
   [:head
    [:meta {:name "viewport"
            :content "width=device-width, initial-scale=1.0"
            :charset "UTF-8"}]]
   [:body body]])
