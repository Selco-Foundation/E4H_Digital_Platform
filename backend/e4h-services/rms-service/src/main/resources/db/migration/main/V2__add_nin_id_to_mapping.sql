-- Add NIN ID column to center_id_to_hfr_id_mapping table
-- First ensure the table exists (in case V1 didn't create it)
CREATE TABLE IF NOT EXISTS center_id_to_hfr_id_mapping (
    id VARCHAR(255) PRIMARY KEY,
    center_id VARCHAR(255) NOT NULL UNIQUE,
    device_id VARCHAR(255),
    device_instance_id VARCHAR(255),
    hfr_id VARCHAR(255),
    facility_name VARCHAR(500),
    is_active BOOLEAN DEFAULT true,
    last_sync_time TIMESTAMP,
    last_validated_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_center_id UNIQUE (center_id)
);

-- Now add the nin_id column if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'center_id_to_hfr_id_mapping' 
        AND column_name = 'nin_id'
    ) THEN
        ALTER TABLE center_id_to_hfr_id_mapping ADD COLUMN nin_id VARCHAR(255);
    END IF;
END $$;

-- Create index if it doesn't exist
CREATE INDEX IF NOT EXISTS idx_mapping_nin_id ON center_id_to_hfr_id_mapping(nin_id);

-- Also ensure other indexes from V1 exist
CREATE INDEX IF NOT EXISTS idx_mapping_center_id ON center_id_to_hfr_id_mapping(center_id);
CREATE INDEX IF NOT EXISTS idx_mapping_hfr_id ON center_id_to_hfr_id_mapping(hfr_id);
CREATE INDEX IF NOT EXISTS idx_mapping_device_instance_id ON center_id_to_hfr_id_mapping(device_instance_id);
CREATE INDEX IF NOT EXISTS idx_mapping_is_active ON center_id_to_hfr_id_mapping(is_active);
CREATE INDEX IF NOT EXISTS idx_mapping_last_sync_time ON center_id_to_hfr_id_mapping(last_sync_time);

