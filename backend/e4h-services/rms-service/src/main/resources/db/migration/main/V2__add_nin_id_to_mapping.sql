-- Add NIN ID column to center_id_to_hfr_id_mapping table
ALTER TABLE center_id_to_hfr_id_mapping ADD COLUMN IF NOT EXISTS nin_id VARCHAR(255);

CREATE INDEX IF NOT EXISTS idx_mapping_nin_id ON center_id_to_hfr_id_mapping(nin_id);

