package org.egov.field_planner.util;

import java.util.LinkedHashMap;
import java.util.Map;

public final class FieldPlanTemplateConstants {

    public static final String TEMPLATE_AC_OFF_GRID =
            "ICC Report_Offgrid_With RMS_Single Phase & Three Phase_.xlsx";
    public static final String TEMPLATE_AC_HYBRID =
            "ICC Report_Hybrid_With RMS_Single Phase & Three Phase_3.0.xlsx";
    public static final String TEMPLATE_DC_OFF_GRID = "ICC Report_DC System.xlsx";
    public static final String TEMPLATE_AC_ON_GRID =
            "ICC Report_Ongrid_Single Phase & Three Phase 2.0.xlsx";

    private static final Map<String, String> SYSTEM_TYPE_TO_TEMPLATE_FILE = buildMappings();

    private FieldPlanTemplateConstants() {
    }

    public static String expectedTemplateFileForSystemType(String systemType) {
        if (systemType == null) {
            return null;
        }
        return SYSTEM_TYPE_TO_TEMPLATE_FILE.get(normalizeSystemType(systemType));
    }

    public static String normalizeSystemType(String systemType) {
        return systemType.trim().toLowerCase().replace('_', ' ').replaceAll("\\s+", " ");
    }

    public static String normalizeFileName(String fileName) {
        if (fileName == null) {
            return "";
        }
        String normalized = fileName.trim();
        int lastSeparator = Math.max(normalized.lastIndexOf('/'), normalized.lastIndexOf('\\'));
        if (lastSeparator >= 0) {
            normalized = normalized.substring(lastSeparator + 1);
        }
        return normalized;
    }

    public static String baseNameWithoutExtension(String fileName) {
        String normalized = normalizeFileName(fileName);
        int dotIndex = normalized.lastIndexOf('.');
        if (dotIndex > 0) {
            return normalized.substring(0, dotIndex);
        }
        return normalized;
    }

    private static Map<String, String> buildMappings() {
        Map<String, String> mappings = new LinkedHashMap<>();
        putMapping(mappings, "AC Off-grid", TEMPLATE_AC_OFF_GRID);
        putMapping(mappings, "AC_OFF_GRID", TEMPLATE_AC_OFF_GRID);
        putMapping(mappings, "AC Hybrid", TEMPLATE_AC_HYBRID);
        putMapping(mappings, "AC_HYBRID", TEMPLATE_AC_HYBRID);
        putMapping(mappings, "DC Off-grid", TEMPLATE_DC_OFF_GRID);
        putMapping(mappings, "DC_OFF_GRID", TEMPLATE_DC_OFF_GRID);
        putMapping(mappings, "AC On-grid", TEMPLATE_AC_ON_GRID);
        putMapping(mappings, "AC_ON_GRID", TEMPLATE_AC_ON_GRID);
        return Map.copyOf(mappings);
    }

    private static void putMapping(Map<String, String> mappings, String systemType, String templateFile) {
        mappings.put(normalizeSystemType(systemType), templateFile);
    }
}
