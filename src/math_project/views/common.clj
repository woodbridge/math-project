(ns math-project.views.common
  (:use [noir.core :only [defpartial]]
        [hiccup.page :only [include-css include-js html5]]))

(defpartial layout [& content]
            (html5
              [:head
               [:title "math-project"]
               (include-css "/css/reset.css")
               (include-css "/css/app.css")
               (include-js "/js/jquery.js")
               (include-js "/js/jquery.pjax.js")
               (include-js "/js/app.js")]
              [:body
               [:div#container
                [:header#mast
                 [:h1 "This or That"]
                 [:p.tag "a game by Justin Woodbridge"]]
                content]]))

   [:section#rules]
