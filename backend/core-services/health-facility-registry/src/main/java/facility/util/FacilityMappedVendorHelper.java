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
        if (StringUtils.isBlank(facility.getMappedVendorName())
                && StringUtils.isBlank(facility.getMappedVendorUserName())) {
            return;
        }
        Map<String, Object> ad = facility.getAdditionalDetails();
        if (ad == null) {
            ad = new HashMap<>();
            facility.setAdditionalDetails(ad);
        }
        if (StringUtils.isNotBlank(facility.getMappedVendorName())) {
            ad.put(MAPPED_VENDOR_NAME_KEY, facility.getMappedVendorName());
        }
        if (StringUtils.isNotBlank(facility.getMappedVendorUserName())) {
            ad.put(MAPPED_VENDOR_USER_NAME_KEY, facility.getMappedVendorUserName());
        }
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

        String vendorName = firstNonBlankString(
                update.getMappedVendorName(),
                extractFromAdditionalDetails(update.getAdditionalDetails(), MAPPED_VENDOR_NAME_KEY, "mapped_vendor_name"));
        String vendorUserName = firstNonBlankString(
                update.getMappedVendorUserName(),
                extractFromAdditionalDetails(update.getAdditionalDetails(), MAPPED_VENDOR_USER_NAME_KEY,
                        "mapped_vendor_user_name", "mappedVendorUsername"));

        if (vendorName == null && vendorUserName == null && existingFacility != null) {
            hydrateFromAdditionalDetails(existingFacility);
            vendorName = existingFacility.getMappedVendorName();
            vendorUserName = existingFacility.getMappedVendorUserName();
        }

        if (vendorName != null || vendorUserName != null) {
            applyMappedVendor(facility, vendorName, vendorUserName);
        } else if (update.getAdditionalDetails() != null) {
            facility.setAdditionalDetails(update.getAdditionalDetails());
            hydrateFromAdditionalDetails(facility);
        }
    }

    public static boolean hasMappedVendor(Facility facility) {
        if (facility == null) {
            return false;
        }
        return StringUtils.isNotBlank(facility.getMappedVendorName())
                || StringUtils.isNotBlank(facility.getMappedVendorUserName());
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
