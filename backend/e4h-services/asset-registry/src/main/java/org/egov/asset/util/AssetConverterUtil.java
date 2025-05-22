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

        inverterDetails.setCurrentUnit((String) map.get("currentUnit"));
        inverterDetails.setVoltageUnit((String) map.get("voltageUnit"));
        inverterDetails.setTotalCapacityUOM((String) map.get("totalCapacityUOM"));
        String inverterCap = (String) map.get("invertorCapacity");
        if (inverterCap == null) {
            inverterCap = (String) map.get("inverterCapacity");
        }
        inverterDetails.setInverterCapacity(inverterCap);

        inverterDetails.setInverterCapacityUnit((String) map.get("invertorCapacityUnit"));
        inverterDetails.setOutputPhase((String) map.get("outputPhase"));

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
        batteryDetails.setBatteryCapacity(getDoubleValue(map.get("batteryCapacity")));

        batteryDetails.setTotalCapacityUOM((String) map.get("totalCapacityUOM"));
        batteryDetails.setVoltageUnit((String) map.get("voltageUnit"));
        batteryDetails.setCapacityUnit((String) map.get("capacityUnit"));
        batteryDetails.setBatteryType((String) map.get("batteryType"));

        return batteryDetails;
    }

    public static PanelDetails convertMapToPanelDetails(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        PanelDetails panelDetails = new PanelDetails();
        panelDetails.setTotalCapacity(getDoubleValue(map.get("totalCapacity")));
        panelDetails.setPanelCapacity(getDoubleValue(map.get("panelCapacity")));

        panelDetails.setTotalCapacityUnit((String) map.get("totalCapacityUnit"));
        panelDetails.setCapacityUnit((String) map.get("capacityUnit"));

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
}
