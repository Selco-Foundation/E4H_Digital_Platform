package facility.service;

import facility.web.models.Facility;
import facility.web.models.FacilityAddress;
import facility.web.models.FacilityUpdateRequestFacilityUpdate;
import facility.web.models.HealthFacilityDetails;
import facility.web.models.SolarSolutionDesignType;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

class FacilityUpdateMergerTest {

    private static Facility stored() {
        Map<String, Object> additional = new HashMap<>();
        additional.put("mappedVendorName", "Vendor A");
        return Facility.builder()
                .facilityId("F1").tenantId("in")
                .facilityCategory("HEALTH").facilityType("PHC").facilityName("Old Name")
                .facilityPocName("Asha").facilityPocPhone("9876543210").facilityPocEmail("asha@x.org")
                .facilityPocUsername("asha01").hfrId("HFR1").ninId("NIN1").userId("U1")
                .facilityStatus("ACTIVE").isActive(true).isOnmReady(true)
                .additionalDetails(additional)
                .address(FacilityAddress.builder().addressId("A1").addressLine1("Main Road")
                        .city("Hubli").latitude(15.3).longitude(75.1).build())
                .facilityDetails(HealthFacilityDetails.builder().pocDesignation("Doctor")
                        .solarSolutionDesignType(SolarSolutionDesignType.values()[0]).build())
                .build();
    }

    @Test
    void namePartialPayloadKeepsEveryStoredColumn() {
        FacilityUpdateRequestFacilityUpdate update = FacilityUpdateRequestFacilityUpdate.builder()
                .facilityId("F1").tenantId("in").facilityName("New Name").build();

        FacilityUpdateMerger.fillOmittedFromStored(update, stored());

        assertEquals("New Name", update.getFacilityName());
        assertEquals("HFR1", update.getHfrId());
        assertEquals("NIN1", update.getNinId());
        assertEquals("asha01", update.getFacilityPocUsername());
        assertEquals("Asha", update.getPocName());
        assertEquals("9876543210", update.getPocContact());
        assertEquals("asha@x.org", update.getPocEmail());
        assertEquals("ACTIVE", update.getStatus());
        assertEquals(Boolean.TRUE, update.getIsOnmReady());
        assertEquals("Main Road", update.getAddress().getAddressLine1());
        assertEquals("Doctor", update.getFacilityDetails().getPocDesignation());
    }

    @Test
    void blankStringIsASentValueNotAnOmission() {
        FacilityUpdateRequestFacilityUpdate update = FacilityUpdateRequestFacilityUpdate.builder()
                .ninId("").build();

        FacilityUpdateMerger.fillOmittedFromStored(update, stored());

        assertEquals("", update.getNinId());
    }

    @Test
    void partialAddressKeepsStoredLinesAndTakesNewCoordinates() {
        FacilityUpdateRequestFacilityUpdate update = FacilityUpdateRequestFacilityUpdate.builder()
                .address(FacilityAddress.builder().tenantId("in").latitude(12.9).longitude(77.6).build())
                .build();

        FacilityUpdateMerger.fillOmittedFromStored(update, stored());

        assertEquals("A1", update.getAddress().getAddressId());
        assertEquals("Main Road", update.getAddress().getAddressLine1());
        assertEquals("Hubli", update.getAddress().getCity());
        assertEquals(12.9, update.getAddress().getLatitude());
        assertEquals(77.6, update.getAddress().getLongitude());
    }

    @Test
    void storedZeroCoordinatesAreNotCarriedOver() {
        Facility stored = stored();
        stored.getAddress().setLatitude(0.0);
        stored.getAddress().setLongitude(0.0);
        FacilityUpdateRequestFacilityUpdate update = FacilityUpdateRequestFacilityUpdate.builder()
                .address(FacilityAddress.builder().tenantId("in").build()).build();

        FacilityUpdateMerger.fillOmittedFromStored(update, stored);

        assertNull(update.getAddress().getLatitude());
        assertNull(update.getAddress().getLongitude());
        assertEquals("Main Road", update.getAddress().getAddressLine1());
    }

    @Test
    void partialFacilityDetailsKeepsStoredKeys() {
        SolarSolutionDesignType other = SolarSolutionDesignType.values()[SolarSolutionDesignType.values().length - 1];
        FacilityUpdateRequestFacilityUpdate update = FacilityUpdateRequestFacilityUpdate.builder()
                .facilityDetails(HealthFacilityDetails.builder().solarSolutionDesignType(other).build())
                .build();

        FacilityUpdateMerger.fillOmittedFromStored(update, stored());

        assertEquals(other, update.getFacilityDetails().getSolarSolutionDesignType());
        assertEquals("Doctor", update.getFacilityDetails().getPocDesignation());
    }

    @Test
    void storedAdditionalDetailsAreCopiedNotShared() {
        Facility stored = stored();
        FacilityUpdateRequestFacilityUpdate update = new FacilityUpdateRequestFacilityUpdate();

        FacilityUpdateMerger.fillOmittedFromStored(update, stored);
        update.getAdditionalDetails().put("mappedVendorName", "Vendor B");

        assertEquals("Vendor A", stored.getAdditionalDetails().get("mappedVendorName"));
    }

    @Test
    void pocChangeCheckDoesNotThrowWhenOnmReadyIsAbsent() {
        FacilityService service = mock(FacilityService.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));
        Facility existing = stored();
        Facility requested = stored();
        requested.setFacilityPocName("Someone Else");
        requested.setIsOnmReady(null);

        assertFalse(service.checkPOCDetailsUpdated(existing, requested));
    }
}
