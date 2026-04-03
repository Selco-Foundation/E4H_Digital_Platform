CREATE TABLE IF NOT EXISTS public.activity_facility_transaction (
    id VARCHAR(64) PRIMARY KEY,
    activity_facility_id VARCHAR(64) NOT NULL,
    process_instance_id VARCHAR(256),
    created_by VARCHAR(64),
    last_modified_by VARCHAR(64),
    created_time BIGINT,
    last_modified_time BIGINT,
    CONSTRAINT fk_activity_facility_transaction FOREIGN KEY (activity_facility_id)
        REFERENCES public.facility_activities(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS public.activity_facility_transaction_comment (
    id VARCHAR(64) PRIMARY KEY,
    transaction_id VARCHAR(64) NOT NULL,
    comment_message TEXT,
    asset_type VARCHAR(64),
    created_by VARCHAR(64),
    last_modified_by VARCHAR(64),
    created_time BIGINT,
    last_modified_time BIGINT
);

