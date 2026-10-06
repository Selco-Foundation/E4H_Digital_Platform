package facility.util;

import facility.web.models.Facility;
import facility.web.models.FacilityUpdateRequestFacilityUpdate;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Keeps {@link Facility#getMappedVendorName()} / {@link Facility#getMappedVendorUserName()}
 * in sync with {@link Facility#getAdditionalDetails()} for persistence and Kibana indexing.
 */
public final class FacilityMappedVendorHelper {

    public static final String MAPPED_VENDOR_NAME_KEY = "mappedVendorName";
    public static final String MAPPED_VENDOR_USER_NAME_KEY = "mappedVendorUserName";

    private FacilityMappedVendorHelper() {
    }

    public static void hydrateFromAdditionalDetails(Facility facility) {
        if (facility == null) {
            return;
        }
        Map<String, Object> ad = facility.getAdditionalDetails();
        if (ad == null || ad.isEmpty()) {
            return;
        }
        if (StringUtils.isBlank(facility.getMappedVendorName())) {
            facility.setMappedVendorName(firstNonBlankString(
                    ad.get(MAPPED_VENDOR_NAME_KEY),
                    ad.get("mapped_vendor_name")));
        }
        if (StringUtils.isBlank(facility.getMappedVendorUserName())) {
            facility.setMappedVendorUserName(firstNonBlankString(
                    ad.get(MAPPED_VENDOR_USER_NAME_KEY),
                    ad.get("mapped_vendor_user_name"),
                    ad.get("mappedVendorUsername")));
        }
    }

    public static void syncToAdditionalDetails(Facility facility) {
        if (facility == null) {
            return;
        }
        Map<String, Object> ad = facility.getAdditionalDetails();
        if (ad == null) {
            ad = new HashMap<>();
            facility.setAdditionalDetails(ad);
        }
        ad.put(MAPPED_VENDOR_NAME_KEY, facility.getMappedVendorName());
        ad.put(MAPPED_VENDOR_USER_NAME_KEY, facility.getMappedVendorUserName());
    }

    public static void applyMappedVendor(Facility facility, String vendorName, String vendorUserName) {
        if (facility == null) {
            return;
        }
        if (StringUtils.isNotBlank(vendorName)) {
            facility.setMappedVendorName(vendorName);
        }
        if (StringUtils.isNotBlank(vendorUserName)) {
            facility.setMappedVendorUserName(vendorUserName);
        }
        syncToAdditionalDetails(facility);
    }

    public static void mergeMappedVendorFromUpdate(Facility facility,
                                                   FacilityUpdateRequestFacilityUpdate update,
                                                   Facility existingFacility) {
        if (facility == null || update == null) {
            return;
        }

        String existingVendorName = existingFacility != null ? existingFacility.getMappedVendorName() : null;
        String existingVendorUserName = existingFacility != null ? existingFacility.getMappedVendorUserName() : null;
        if (existingFacility != null
                && (StringUtils.isBlank(existingVendorName) || StringUtils.isBlank(existingVendorUserName))) {
            hydrateFromAdditionalDetails(existingFacility);
            existingVendorName = existingFacility.getMappedVendorName();
            existingVendorUserName = existingFacility.getMappedVendorUserName();
        }

        String vendorName = resolveMappedVendorValue(
                update.getMappedVendorName(),
                update.getAdditionalDetails(),
                existingVendorName,
                MAPPED_VENDOR_NAME_KEY,
                "mapped_vendor_name");
        String vendorUserName = resolveMappedVendorValue(
                update.getMappedVendorUserName(),
                update.getAdditionalDetails(),
                existingVendorUserName,
                MAPPED_VENDOR_USER_NAME_KEY,
                "mapped_vendor_user_name",
                "mappedVendorUsername");

        facility.setMappedVendorName(vendorName);
        facility.setMappedVendorUserName(vendorUserName);
    }

    public static boolean hasMappedVendorUpdateInPayload(FacilityUpdateRequestFacilityUpdate update) {
        if (update == null) {
            return false;
        }
        if (update.getMappedVendorName() != null || update.getMappedVendorUserName() != null) {
            return true;
        }
        Map<String, Object> additionalDetails = update.getAdditionalDetails();
        if (additionalDetails == null || additionalDetails.isEmpty()) {
            return false;
        }
        return additionalDetails.containsKey(MAPPED_VENDOR_NAME_KEY)
                || additionalDetails.containsKey(MAPPED_VENDOR_USER_NAME_KEY)
                || additionalDetails.containsKey("mapped_vendor_name")
                || additionalDetails.containsKey("mapped_vendor_user_name")
                || additionalDetails.containsKey("mappedVendorUsername");
    }

    public static boolean hasMappedVendor(Facility facility) {
        if (facility == null) {
            return false;
        }
        return StringUtils.isNotBlank(facility.getMappedVendorName())
                || StringUtils.isNotBlank(facility.getMappedVendorUserName());
    }

    private static String resolveMappedVendorValue(String topLevelValue,
                                                   Map<String, Object> additionalDetails,
                                                   String existingValue,
                                                   String... additionalKeys) {
        if (StringUtils.isNotBlank(topLevelValue)) {
            return topLevelValue.trim();
        }
        if (topLevelValue != null && topLevelValue.trim().isEmpty()) {
            return null;
        }
        if (hasAdditionalDetailKey(additionalDetails, additionalKeys)) {
            Object fromAdditionalDetails = getAdditionalDetailRaw(additionalDetails, additionalKeys);
            if (fromAdditionalDetails == null) {
                return null;
            }
            String normalized = fromAdditionalDetails.toString().trim();
            return normalized.isEmpty() ? null : normalized;
        }
        return existingValue;
    }

    private static boolean hasAdditionalDetailKey(Map<String, Object> additionalDetails, String... keys) {
        if (additionalDetails == null || additionalDetails.isEmpty() || keys == null) {
            return false;
        }
        for (String key : keys) {
            if (key != null && additionalDetails.containsKey(key)) {
                return true;
            }
        }
        return false;
    }

    private static Object getAdditionalDetailRaw(Map<String, Object> additionalDetails, String... keys) {
        if (additionalDetails == null || additionalDetails.isEmpty() || keys == null) {
            return null;
        }
        for (String key : keys) {
            if (key != null && additionalDetails.containsKey(key)) {
                return additionalDetails.get(key);
            }
        }
        return null;
    }

    private static String extractFromAdditionalDetails(Map<String, Object> ad, String... keys) {
        if (ad == null || ad.isEmpty()) {
            return null;
        }
        return firstNonBlankString(
                java.util.Arrays.stream(keys).map(ad::get).toArray());
    }

    private static String firstNonBlankString(Object... values) {
        if (values == null) {
            return null;
        }
        for (Object value : values) {
            if (value == null) {
                continue;
            }
            String s = value.toString().trim();
            if (!s.isEmpty()) {
                return s;
            }
        }
        return null;
    }
}
