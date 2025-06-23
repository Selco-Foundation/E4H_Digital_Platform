CREATE TABLE IF NOT EXISTS public.project_transaction (
    id VARCHAR(64) PRIMARY KEY,
    project_id VARCHAR(64) NOT NULL,
    process_instance_id VARCHAR(256),
    created_by VARCHAR(64),
    last_modified_by VARCHAR(64),
    created_time BIGINT,
    last_modified_time BIGINT,
    CONSTRAINT fk_project_transaction_project FOREIGN KEY (project_id)
        REFERENCES public.project(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS public.project_transaction_comment (
    id VARCHAR(64) PRIMARY KEY,
    transaction_id VARCHAR(64) NOT NULL,
    comment_message TEXT,
    asset_type VARCHAR(64),
    created_by VARCHAR(64),
    last_modified_by VARCHAR(64),
    created_time BIGINT,
    last_modified_time BIGINT
);

