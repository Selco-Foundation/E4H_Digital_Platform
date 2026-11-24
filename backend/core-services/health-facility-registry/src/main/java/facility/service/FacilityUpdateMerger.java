package facility.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import facility.web.models.Facility;
import facility.web.models.FacilityAddress;
import facility.web.models.FacilityUpdateRequestFacilityUpdate;
import facility.web.models.HealthFacilityDetails;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Fills the fields an update request leaves out with the facility's stored values.
 *
 * <p>The deployed update-facility persister writes every column it maps straight from the request,
 * so a field the caller omits is written as NULL. Merging the stored row in before the push means a
 * partial payload changes only what it names.
 *
 * <p>Only {@code null} counts as omitted. A blank string is a value the caller sent, so it is kept.
 * {@code address} and {@code facility_details} are merged field by field, because callers send
 * them partially (the facility edit form sends only the coordinates and the solar design type).
 */
public final class FacilityUpdateMerger {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private FacilityUpdateMerger() {
    }

    /**
     * Copies every field {@code update} leaves null from {@code stored}. {@code stored} must already
     * hold the decrypted PoC phone, because the push encrypts whatever it is given.
     */
    public static void fillOmittedFromStored(FacilityUpdateRequestFacilityUpdate update, Facility stored) {
        if (update == null || stored == null) {
            return;
        }
        fill(update::getFacilityCategory, update::setFacilityCategory, stored.getFacilityCategory());
        fill(update::getFacilityType, update::setFacilityType, stored.getFacilityType());
        fill(update::getFacilitySubtype, update::setFacilitySubtype, stored.getFacilitySubtype());
        fill(update::getFacilityName, update::setFacilityName, stored.getFacilityName());
        fill(update::getPocName, update::setPocName, stored.getFacilityPocName());
        fill(update::getPocContact, update::setPocContact, stored.getFacilityPocPhone());
        fill(update::getPocEmail, update::setPocEmail, stored.getFacilityPocEmail());
        fill(update::getFacilityPocUsername, update::setFacilityPocUsername, stored.getFacilityPocUsername());
        fill(update::getHfrId, update::setHfrId, stored.getHfrId());
        fill(update::getNinId, update::setNinId, stored.getNinId());
        fill(update::getUserId, update::setUserId, stored.getUserId());
        fill(update::getStatus, update::setStatus, stored.getFacilityStatus());
        fill(update::getIsActive, update::setIsActive, stored.getIsActive());
        fill(update::getIsOnmReady, update::setIsOnmReady, stored.getIsOnmReady());
        fill(update::getSolarInstallationDate, update::setSolarInstallationDate, stored.getSolarInstallationDate());
        fill(update::getRmsInstallationDate, update::setRmsInstallationDate, stored.getRmsInstallationDate());
        fill(update::getSolarSystemCapacityKwp, update::setSolarSystemCapacityKwp, stored.getSolarSystemCapacityKwp());
        // A copy: the mapped-vendor merge writes into this map, and the stored one must stay as read.
        fill(update::getAdditionalDetails, update::setAdditionalDetails,
                stored.getAdditionalDetails() == null ? null : new HashMap<>(stored.getAdditionalDetails()));

        update.setAddress(mergeAddress(stored.getAddress(), update.getAddress()));
        update.setFacilityDetails(overlayNonNull(
                stored.getFacilityDetails(), update.getFacilityDetails(), HealthFacilityDetails.class));
    }

    private static <T> void fill(Supplier<T> current, Consumer<T> setter, T stored) {
        if (current.get() == null && stored != null) {
            setter.accept(stored);
        }
    }

    private static FacilityAddress mergeAddress(FacilityAddress stored, FacilityAddress incoming) {
        if (stored != null) {
            // The row mapper reads a NULL coordinate as 0.0. Carrying that over would write 0,0 onto
            // a facility that never had a location, so a stored 0.0 counts as absent.
            stored = copyOf(stored);
            if (isZero(stored.getLatitude())) {
                stored.setLatitude(null);
            }
            if (isZero(stored.getLongitude())) {
                stored.setLongitude(null);
            }
        }
        return overlayNonNull(stored, incoming, FacilityAddress.class);
    }

    private static boolean isZero(Double value) {
        return value != null && value == 0.0d;
    }

    private static FacilityAddress copyOf(FacilityAddress address) {
        return MAPPER.convertValue(MAPPER.convertValue(address, MAP_TYPE), FacilityAddress.class);
    }

    /**
     * {@code stored} with every non-null field of {@code incoming} laid over it. Returns whichever
     * side is present when the other is null.
     */
    static <T> T overlayNonNull(T stored, T incoming, Class<T> type) {
        if (incoming == null) {
            return stored;
        }
        if (stored == null) {
            return incoming;
        }
        Map<String, Object> merged = MAPPER.convertValue(stored, MAP_TYPE);
        Map<String, Object> overrides = MAPPER.convertValue(incoming, MAP_TYPE);
        overrides.forEach((key, value) -> {
            if (value != null) {
                merged.put(key, value);
            }
        });
        return MAPPER.convertValue(merged, type);
    }
}
