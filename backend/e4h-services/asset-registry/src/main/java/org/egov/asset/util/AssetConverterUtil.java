package org.egov.asset.util;

import org.egov.asset.web.models.BatteryDetails;
import org.egov.asset.web.models.InverterDetails;
import org.egov.asset.web.models.PanelDetails;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class AssetConverterUtil {

    public static InverterDetails convertMapToInverterDetails(Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        InverterDetails inverterDetails = new InverterDetails();

        inverterDetails.setCurrentUnit(getStringValue(map.get("currentUnit")));
        inverterDetails.setVoltageUnit(getStringValue(map.get("voltageUnit")));
        inverterDetails.setTotalCapacityUOM(getStringValue(map.get("totalCapacityUOM")));
        String inverterCap = getStringValue(map.get("invertorCapacity"));
        if (inverterCap == null) {
            inverterCap = getStringValue(map.get("inverterCapacity"));
        }
        inverterDetails.setInverterCapacity(inverterCap);

        inverterDetails.setInverterCapacityUnit(getStringValue(map.get("invertorCapacityUnit")));
        inverterDetails.setOutputPhase(getStringValue(map.get("outputPhase")));

        inverterDetails.setChargeControllerCurrent(getDoubleValue(map.get("chargeControllerCurrent")));
        inverterDetails.setChargeControllerVoltage(getDoubleValue(map.get("chargeControllerVoltage")));
        inverterDetails.setTotalCapacity(getDoubleValue(map.get("totalCapacity")));

        return inverterDetails;
    }

    public static BatteryDetails convertMapToBatteryDetails(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        BatteryDetails batteryDetails = new BatteryDetails();

        batteryDetails.setTotalCapacity(getDoubleValue(map.get("totalCapacity")));
        batteryDetails.setBatteryVoltage(getDoubleValue(map.get("batteryVoltage")));
        batteryDetails.setBatteryCapacity(getStringValue(map.get("batteryCapacity")));

        batteryDetails.setTotalCapacityUOM(getStringValue(map.get("totalCapacityUOM")));
        batteryDetails.setVoltageUnit(getStringValue(map.get("voltageUnit")));
        batteryDetails.setCapacityUnit(getStringValue(map.get("capacityUnit")));
        batteryDetails.setBatteryType(getStringValue(map.get("batteryType")));

        return batteryDetails;
    }

    public static PanelDetails convertMapToPanelDetails(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        PanelDetails panelDetails = new PanelDetails();
        panelDetails.setTotalCapacity(getDoubleValue(map.get("totalCapacity")));
        panelDetails.setPanelCapacity(getStringValue(map.get("panelCapacity")));

        panelDetails.setTotalCapacityUnit(getStringValue(map.get("totalCapacityUnit")));
        panelDetails.setCapacityUnit(getStringValue(map.get("capacityUnit")));

        return panelDetails;
    }

    private static Double getDoubleValue(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        } else if (value instanceof Long) {
            return ((Long) value).doubleValue();
        } else if (value instanceof BigDecimal) {
            return ((BigDecimal) value).doubleValue();
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null;
    }

    /**
     * These asset detail fields are String-typed in the models, but assetDetails is deserialized
     * as a loosely-typed Map<String, Object>: a value that looks numeric in the request JSON
     * (e.g. batteryCapacity=150.0) comes back as a Double/Integer/BigDecimal, not a String, and a
     * raw (String) cast on it throws ClassCastException. Whole numbers are rendered without a
     * trailing ".0" (150.0 -> "150") since these are free-text capacity fields, not numeric ones.
     */
    private static String getStringValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof Double || value instanceof Float) {
            double d = ((Number) value).doubleValue();
            if (!Double.isInfinite(d) && !Double.isNaN(d) && d == Math.floor(d)) {
                return String.valueOf((long) d);
            }
            return String.valueOf(d);
        }
        return value.toString();
    }
}
