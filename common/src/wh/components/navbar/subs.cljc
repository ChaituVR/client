(ns wh.components.navbar.subs
  (:require [re-frame.core :refer [reg-sub]]
            [wh.common.user :as user-common]
            [wh.components.navbar.db :as db]
            [wh.job.db :as job]))

(reg-sub
  ::profile-image
  :<- [:user/sub-db]
  (fn [{:keys [wh.user.db/type wh.user.db/image-url wh.user.db/company] :as user} _]
    (if (user-common/company-type? type) (get company :logo) image-url)))

(reg-sub
  ::company-slug
  :<- [:user/sub-db]
  (fn [user _]
    (get-in user [:wh.user.db/company :slug])))

(reg-sub
  ::search-value
  (fn [db _]
    (db ::db/search-value)))

(reg-sub
  ::search-focus
  (fn [db _]
    (db ::db/search-focus)))

(reg-sub
  ::local-search
  (fn [db _]
    (db ::db/local-search)))
    
(reg-sub
  ::can-publish-jobs?
  (fn [db _]
    (job/can-publish-jobs? db)))
