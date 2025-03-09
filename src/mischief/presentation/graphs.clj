(ns mischief.presentation.graphs
  (:require [incanter.charts :as chart]
            [incanter.core :as graph]))

(defn save-bar-chart
  [rows]
  (let [axis (for [cols (keys (first rows))] (map cols rows))]

    (graph/save
     (chart/bar-chart
      (first axis)
      (second axis))
     "res/myplot.png")))

(defn save-line-chart
  [rows]
  (let [axis (for [cols (keys (first rows))] (map cols rows))]

    (graph/save
     (chart/line-chart
      (first axis)
      (second axis))
     "res/myplot.png")))
