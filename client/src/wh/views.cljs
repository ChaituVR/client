(ns wh.views
  (:require [clojure.walk :as walk]
            [reagent.core :as r]
            ["smoothscroll-polyfill"]
            [wh.components.error.subs :as error-subs]
            [wh.components.error.views :refer [global-status-box]]
            [wh.components.banner :as banner]
            [wh.components.footer :as footer]
            [wh.components.icons :refer [icon]]
            [wh.components.loader :refer [loader]]
            [wh.components.navbar.navbar :as navbar]
            [wh.pages.router :refer [current-page]]
            [wh.subs :as subs :refer [<sub]]))

(defn version-mismatch []
  [:div.version-mismatch
   [icon "codi"]
   [:h1 "Sorry to interrupt you..."]
   [:p "...but we've just released a new version of our platform and we need you to reload the page so you can use it."]
   [:button.button.button--medium {:on-click #(js/window.location.reload true)}
    "Reload"]])

;; when other modules are loaded, extra components are conj'd onto this atom
;; (currently used by user, logged-in and blogs modules)
(def extra-overlays (r/atom []))

;; DO NOT add anything to this function; things are being moved out and into SSR-land
(defn overlays []
  (into
   [:div.overlays]
   @extra-overlays))

(defn main-panel []
  (let [page             (<sub [:wh.pages.core/page])
        user-type        (<sub [:user/type])
        ;; specify links on the menu that should be restricted
        restricted-links (when-not (<sub [:company/has-permission?
                                          :can_see_applications])
                           #{:company-applications})
        query-params     (walk/keywordize-keys (<sub [::subs/query-params]))
        logged-in?       (<sub [:user/logged-in?])]

    (if (<sub [::subs/version-mismatch])
      [version-mismatch]
      [:div.main-panel
       [banner/banner {:page       page
                       :logged-in? logged-in?}]
       [navbar/top-bar
        {:env               (<sub [::subs/env])
         :vertical          (<sub [::subs/vertical])
         :logged-in?        logged-in?
         :query-params      query-params
         :page              page
         :user-type         user-type
         :restricted-links  restricted-links}]

       [:div.page-container
        (when (<sub [::error-subs/message])
          [global-status-box])
        (if (and (not (<sub [::subs/ssr-page?]))
                 (<sub [::subs/loading?]))
          [:div.main-wrapper
           [:div.loader-wrapper
            [loader]]]
          [current-page])
        (when (<sub [::subs/show-footer?])
          [footer/footer (<sub [::subs/vertical]) logged-in?])]
       [overlays]])))

(defonce remove-all-bsl-locks-when-app-loads
  (js/disableNoScroll))
