package org.egov.field_planner.util;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class AssessmentBoundaryHelper {

    private AssessmentBoundaryHelper() {
    }

    public record DistrictBlock(String district, String block) {
        public static DistrictBlock empty() {
            return new DistrictBlock(null, null);
        }
    }

    public record DistrictBlockCodes(String districtCode, String blockCode) {
        public static DistrictBlockCodes empty() {
            return new DistrictBlockCodes(null, null);
        }
    }

    public static DistrictBlockCodes extractBoundaryCodes(String boundaryCode) {
        if (StringUtils.isBlank(boundaryCode)) {
            return DistrictBlockCodes.empty();
        }
        String[] parts = boundaryCode.split("_");
        if (parts.length < 3 || !"India".equalsIgnoreCase(parts[0])) {
            return DistrictBlockCodes.empty();
        }
        String districtCode = String.join("_", parts[0], parts[1], parts[2]);
        String blockCode = parts.length >= 4 ? String.join("_", parts[0], parts[1], parts[2], parts[3]) : null;
        return new DistrictBlockCodes(districtCode, blockCode);
    }

    public static boolean isBoundaryCode(String value) {
        return StringUtils.isNotBlank(value) && value.startsWith("India_") && value.contains("_");
    }

    public static String toDistrictDisplayName(String storedValue, FieldPlannerServiceUtil util) {
        if (StringUtils.isBlank(storedValue) || util == null) {
            return storedValue;
        }
        if (isBoundaryCode(storedValue)) {
            String[] parts = storedValue.split("_");
            if (parts.length >= 3) {
                return util.boundaryCodeToName(parts[2]);
            }
        }
        return storedValue;
    }

    public static String toBlockDisplayName(String storedValue, FieldPlannerServiceUtil util) {
        if (StringUtils.isBlank(storedValue) || util == null) {
            return storedValue;
        }
        if (isBoundaryCode(storedValue)) {
            String[] parts = storedValue.split("_");
            if (parts.length >= 4) {
                return util.boundaryCodeToName(parts[3]);
            }
        }
        return storedValue;
    }

    public static DistrictBlock resolveDisplayNames(String boundaryCode, FieldPlannerServiceUtil util) {
        if (StringUtils.isBlank(boundaryCode) || util == null) {
            return DistrictBlock.empty();
        }
        String[] parts = boundaryCode.split("_");
        if (parts.length >= 4 && "India".equalsIgnoreCase(parts[0])) {
            return new DistrictBlock(util.boundaryCodeToName(parts[2]), util.boundaryCodeToName(parts[3]));
        }
        if (parts.length == 3 && "India".equalsIgnoreCase(parts[0])) {
            return new DistrictBlock(util.boundaryCodeToName(parts[2]), null);
        }
        if (parts.length >= 2) {
            return new DistrictBlock(
                    util.boundaryCodeToName(parts[parts.length - 2]),
                    util.boundaryCodeToName(parts[parts.length - 1])
            );
        }
        return DistrictBlock.empty();
    }

    public static List<String> expandDistrictFilterValues(List<String> values, FieldPlannerServiceUtil util) {
        return expandBoundaryFilterValues(values, util, true);
    }

    public static List<String> expandBlockFilterValues(List<String> values, FieldPlannerServiceUtil util) {
        return expandBoundaryFilterValues(values, util, false);
    }

    public static String toStateDisplayName(String storedValue, FieldPlannerServiceUtil util) {
        if (StringUtils.isBlank(storedValue) || util == null) {
            return storedValue;
        }
        if (isBoundaryCode(storedValue)) {
            String[] parts = storedValue.split("_");
            if (parts.length >= 2) {
                return util.boundaryCodeToName(parts[1]);
            }
        }
        return storedValue;
    }

    public static List<String> expandStateFilterValues(List<String> values, FieldPlannerServiceUtil util) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        Set<String> expanded = new LinkedHashSet<>();
        for (String value : values) {
            if (StringUtils.isBlank(value)) {
                continue;
            }
            expanded.add(value);
            if (!value.contains("_")) {
                expanded.add(util.boundaryCodeToName(value));
                continue;
            }
            String[] parts = value.split("_");
            if (parts.length >= 2 && "India".equalsIgnoreCase(parts[0])) {
                expanded.add(util.boundaryCodeToName(parts[1]));
            }
            expanded.add(util.boundaryCodeToName(parts[parts.length - 1]));
        }
        return new ArrayList<>(expanded);
    }

    private static List<String> expandBoundaryFilterValues(List<String> values,
                                                           FieldPlannerServiceUtil util,
                                                           boolean district) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        Set<String> expanded = new LinkedHashSet<>();
        for (String value : values) {
            if (StringUtils.isBlank(value)) {
                continue;
            }
            expanded.add(value);
            if (!value.contains("_")) {
                expanded.add(util.boundaryCodeToName(value));
                continue;
            }
            String[] parts = value.split("_");
            if (parts.length >= 3 && "India".equalsIgnoreCase(parts[0])) {
                if (district) {
                    expanded.add(util.boundaryCodeToName(parts[2]));
                } else if (parts.length >= 4) {
                    expanded.add(util.boundaryCodeToName(parts[3]));
                }
            }
            expanded.add(util.boundaryCodeToName(parts[parts.length - 1]));
        }
        return new ArrayList<>(expanded);
    }
}
