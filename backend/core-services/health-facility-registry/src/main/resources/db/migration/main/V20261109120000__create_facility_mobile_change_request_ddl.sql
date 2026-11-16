-- Facility POC users ask to change their registered mobile number; a CRM (COMPLAINT_ASSESSOR)
-- approves or rejects, or the user cancels while it is still pending. Each row is one request
-- together with the decision on it, so the table doubles as the audit trail.
CREATE TABLE IF NOT EXISTS facility_mobile_change_request (
    id                      VARCHAR(64) PRIMARY KEY,
    tenant_id               VARCHAR(64) NOT NULL,
    user_uuid               VARCHAR(64) NOT NULL,
    user_name               VARCHAR(256),
    facility_id             VARCHAR(64),
    facility_name           VARCHAR(256),
    boundary_code           VARCHAR(256),
    -- Both numbers are stored encrypted (egov-enc-service), like facility.facility_poc_phone.
    current_mobile_number   VARCHAR(256),
    new_mobile_number       VARCHAR(256) NOT NULL,
    status                  VARCHAR(32) NOT NULL,
    comment                 VARCHAR(1024),
    action_by               VARCHAR(64),
    action_time             BIGINT,
    created_by              VARCHAR(64),
    created_time            BIGINT,
    last_modified_by        VARCHAR(64),
    last_modified_time      BIGINT
);

CREATE INDEX IF NOT EXISTS idx_fmcr_tenant_status
    ON facility_mobile_change_request (tenant_id, status, created_time DESC);
CREATE INDEX IF NOT EXISTS idx_fmcr_user_uuid
    ON facility_mobile_change_request (user_uuid, created_time DESC);

-- At most one pending request per user; also closes the race between two concurrent creates.
CREATE UNIQUE INDEX IF NOT EXISTS uniq_fmcr_user_pending
    ON facility_mobile_change_request (user_uuid) WHERE status = 'PENDING';
