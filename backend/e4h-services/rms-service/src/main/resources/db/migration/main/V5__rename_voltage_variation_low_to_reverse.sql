-- Rename grid sub-type from VOLTAGE_VARIATION_LOW to VOLTAGE_VARIATION_REVERSE
-- to align with the new "Reverse Voltage" condition (range 50V-150V replaces "< 200V").
-- Existing rows must be updated, otherwise Alert.AlertSubType.valueOf will fail when loading them.

UPDATE active_alerts
SET alert_sub_type = 'VOLTAGE_VARIATION_REVERSE',
    updated_at = CURRENT_TIMESTAMP
WHERE alert_sub_type = 'VOLTAGE_VARIATION_LOW';

UPDATE alert_history
SET alert_sub_type = 'VOLTAGE_VARIATION_REVERSE'
WHERE alert_sub_type = 'VOLTAGE_VARIATION_LOW';
