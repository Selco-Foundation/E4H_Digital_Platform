package org.egov.asset.util;

import lombok.extern.slf4j.Slf4j;
import org.egov.asset.web.models.BatteryDetails;
import org.egov.asset.web.models.InverterDetails;
import org.egov.asset.web.models.PanelDetails;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
@Slf4j
public class AssetConverterUtil {

    public static InverterDetails convertMapToInverterDetails(Map<String, Object> map) {
        log.trace("AssetConverterUtil::convertMapToInverterDetails called");
        if (map == null) {
            log.debug("Map is null, returning null");
            return null;
        }
        log.debug("Converting map to inverter details | keysCount={}", map.keySet().size());
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
        log.debug("Inverter details converted | inverterCapacity={} capacityUOM={}",
                inverterDetails.getInverterCapacity(), inverterDetails.getTotalCapacityUOM());
        return inverterDetails;
    }

    public static BatteryDetails convertMapToBatteryDetails(Map<String, Object> map) {
        log.trace("AssetConverterUtil::convertMapToBatteryDetails called");
        if (map == null) {
            log.debug("Map is null, returning null");
            return null;
        }
        log.debug("Converting map to battery details | keysCount={}", map.keySet().size());

        BatteryDetails batteryDetails = new BatteryDetails();

        batteryDetails.setTotalCapacity(getDoubleValue(map.get("totalCapacity")));
        batteryDetails.setBatteryVoltage(getDoubleValue(map.get("batteryVoltage")));
        batteryDetails.setBatteryCapacity(getDoubleValue(map.get("batteryCapacity")));

        batteryDetails.setTotalCapacityUOM((String) map.get("totalCapacityUOM"));
        batteryDetails.setVoltageUnit((String) map.get("voltageUnit"));
        batteryDetails.setCapacityUnit((String) map.get("capacityUnit"));
        batteryDetails.setBatteryType((String) map.get("batteryType"));
        log.debug("Battery details converted | totalCapacity={} capacityUnit={}",
                batteryDetails.getTotalCapacity(), batteryDetails.getCapacityUnit());
        return batteryDetails;
    }

    public static PanelDetails convertMapToPanelDetails(Map<String, Object> map) {
        log.trace("AssetConverterUtil::convertMapToPanelDetails called");
        if (map == null) {
            log.debug("Map is null, returning null");
            return null;
        }
        log.debug("Converting map to panel details | keysCount={}", map.keySet().size());

        PanelDetails panelDetails = new PanelDetails();
        panelDetails.setTotalCapacity(getDoubleValue(map.get("totalCapacity")));
        panelDetails.setPanelCapacity(getDoubleValue(map.get("panelCapacity")));

        panelDetails.setTotalCapacityUnit((String) map.get("totalCapacityUnit"));
        panelDetails.setCapacityUnit((String) map.get("capacityUnit"));
        log.debug("Panel details converted | totalCapacity={} panelCapacity={}",
                panelDetails.getTotalCapacity(), panelDetails.getPanelCapacity());
        return panelDetails;
    }

    private static Double getDoubleValue(Object value) {
        log.trace("AssetConverterUtil::getDoubleValue called");
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
                log.debug("Failed to parse string to double | value={}", value);
                return null;
            }
        }

        return null;
    }
}
