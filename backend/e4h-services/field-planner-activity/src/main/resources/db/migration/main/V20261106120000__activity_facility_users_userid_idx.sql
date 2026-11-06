-- Scoping a worklist to its assigned user makes userid the hot predicate on this table, which
-- was indexed on activityfacilityid only. Partial, because every scoped read filters isdeleted.
CREATE INDEX IF NOT EXISTS idx_activity_facility_users_userid_live
    ON ACTIVITY_FACILITY_USERS (userId) WHERE isDeleted = false;
