package org.egov.im.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MDMS data shared by the SEM 4.0 Ticket Reporting tests: components, the HF ServiceDefs entries
 * (observations) of some of them, and CRM ServiceDefs entries sharing their menuPath.
 */
public final class HfServiceDefsFixture {

    private HfServiceDefsFixture() {
    }

    public static Map<String, Object> mdmsData() {
        return Map.of("MdmsRes", Map.of("Incident", Map.of(
                "Component", List.of(
                        component("INVERTER", "FAULT"),
                        component("THEFT", "THEFT"),
                        component("ADDITIONAL_SUPPORT", "SUPPORT_REQUEST")),
                "ServiceDefs", List.of(
                        hf("INVERTER_NOT_WORKING", "INVERTER", "INVERTER", "High"),
                        hf("INVERTER_DISPLAY_ISSUE", "INVERTER", "INVERTER", "High"),
                        hf("INVERTER_OTHER_ISSUE", "INVERTER", "INVERTER", "Medium"),
                        hf("BATTERY_OTHER_ISSUE", "BATTERY", "BATTERY", "Medium"),
                        hf("THEFT_BATTERY", "THEFT", "Theft", "High"),
                        hf("ADDITIONAL_SUPPORT_SHIFTING_REQUIRED", "ADDITIONAL_SUPPORT", "Shifting", "High"),
                        hf("ADDITIONAL_SUPPORT_UNINSTALLATION_REQUIRED", "ADDITIONAL_SUPPORT", "Uninstall", "High"),
                        crm("ShutdownInverter", "INVERTER", "High"),
                        crm("Other", "INVERTER", "Medium"),
                        crm("Other", "Change Over Switch", "Medium")))));
    }

    private static Map<String, Object> component(String code, String category) {
        return Map.of("code", code, "category", category, "active", true);
    }

    private static Map<String, Object> hf(String serviceCode, String componentCode, String menuPath, String priority) {
        Map<String, Object> entry = crm(serviceCode, menuPath, priority);
        entry.put("componentCode", componentCode);
        entry.put("channel", "HF");
        return entry;
    }

    private static Map<String, Object> crm(String serviceCode, String menuPath, String priority) {
        Map<String, Object> entry = new HashMap<>();
        entry.put("serviceCode", serviceCode);
        entry.put("menuPath", menuPath);
        entry.put("priority", priority);
        entry.put("active", true);
        return entry;
    }
}
