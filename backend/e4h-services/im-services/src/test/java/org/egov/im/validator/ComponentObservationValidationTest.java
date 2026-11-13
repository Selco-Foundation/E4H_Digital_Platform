package org.egov.im.validator;

import org.egov.im.util.HfServiceDefsFixture;
import org.egov.im.web.models.Document;
import org.egov.im.web.models.Incident;
import org.egov.im.web.models.IncidentRequest;
import org.egov.im.web.models.Workflow;
import org.egov.tracer.model.CustomException;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Covers the create-time checks on component and observations (HF Incident.ServiceDefs entries) and the
 * issue type / subtype pair check (SEM 4.0 Ticket Reporting).
 */
class ComponentObservationValidationTest {

    private static final Document FIR = Document.builder().documentType("FIR").fileStoreId("fs-fir").build();
    private static final Document PHOTO = Document.builder().documentType("PHOTO").fileStoreId("fs-photo").build();

    private final ServiceRequestValidator validator = new ServiceRequestValidator(null, null, null);

    private final Map<String, Object> mdmsData = HfServiceDefsFixture.mdmsData();

    @Test
    void theftWithoutObservationFails() {
        assertEquals("OBSERVATION_REQUIRED", singleError("THEFT", List.of(), List.of(FIR)));
        assertEquals("OBSERVATION_REQUIRED", singleError("THEFT", null, List.of(FIR)));
    }

    @Test
    void theftWithObservationAndFirPasses() {
        assertTrue(errors("THEFT", List.of("THEFT_BATTERY"), List.of(PHOTO, FIR)).isEmpty());
    }

    @Test
    void theftWithoutFirFails() {
        assertEquals("FIR_DOCUMENT_MANDATORY", singleError("THEFT", List.of("THEFT_BATTERY"), null));
        assertEquals("FIR_DOCUMENT_MANDATORY", singleError("THEFT", List.of("THEFT_BATTERY"), List.of(PHOTO)));
        assertEquals("FIR_DOCUMENT_MANDATORY", singleError("THEFT", List.of("THEFT_BATTERY"),
                List.of(Document.builder().documentType("FIR").fileStoreId(" ").build())));
    }

    @Test
    void theftWithoutObservationNorFirReportsBoth() {
        Map<String, String> errorMap = errors("THEFT", List.of(), List.of());
        assertTrue(errorMap.containsKey("OBSERVATION_REQUIRED") && errorMap.containsKey("FIR_DOCUMENT_MANDATORY"), errorMap.toString());
    }

    @Test
    void faultWithoutObservationFails() {
        assertEquals("OBSERVATION_REQUIRED", singleError("INVERTER", List.of()));
        assertEquals("OBSERVATION_REQUIRED", singleError("INVERTER", null));
    }

    @Test
    void faultWithSeveralObservationsPasses() {
        assertTrue(errors("INVERTER", List.of("INVERTER_NOT_WORKING", "INVERTER_OTHER_ISSUE")).isEmpty());
    }

    @Test
    void crmServiceDefIsNotAnObservation() {
        assertEquals("INVALID_OBSERVATIONS", singleError("INVERTER", List.of("ShutdownInverter")));
    }

    @Test
    void supportRequestTakesExactlyOneObservation() {
        assertTrue(errors("ADDITIONAL_SUPPORT", List.of("ADDITIONAL_SUPPORT_UNINSTALLATION_REQUIRED")).isEmpty());
        assertEquals("INVALID_OBSERVATIONS", singleError("ADDITIONAL_SUPPORT",
                List.of("ADDITIONAL_SUPPORT_SHIFTING_REQUIRED", "ADDITIONAL_SUPPORT_UNINSTALLATION_REQUIRED")));
    }

    @Test
    void subTypeMustBelongToTheIssueType() {
        assertDoesNotThrow(() -> validator.validateMDMS(typed("INVERTER", "Other"), mdmsData));
        assertDoesNotThrow(() -> validator.validateMDMS(typed("Theft", "THEFT_BATTERY"), mdmsData));
        assertDoesNotThrow(() -> validator.validateMDMS(typed("change over switch", "Other"), mdmsData));
        CustomException e = assertThrows(CustomException.class,
                () -> validator.validateMDMS(typed("BATTERY", "Other"), mdmsData));
        assertEquals("INVALID_SERVICECODE", e.getCode());
        assertThrows(CustomException.class, () -> validator.validateMDMS(typed("INVERTER", "Unknown"), mdmsData));
    }

    @Test
    void observationOfAnotherComponentFails() {
        assertEquals("INVALID_OBSERVATIONS", singleError("INVERTER", List.of("THEFT_BATTERY")));
    }

    @Test
    void unknownComponentFails() {
        assertEquals("INVALID_COMPONENT", singleError("GENERATOR", List.of()));
    }

    @Test
    void observationsWithoutComponentFail() {
        assertEquals("INVALID_OBSERVATIONS", singleError(null, List.of("INVERTER_NOT_WORKING")));
    }

    private static IncidentRequest typed(String incidentType, String incidentSubType) {
        IncidentRequest request = new IncidentRequest();
        request.setIncident(Incident.builder().incidentType(incidentType).incidentSubType(incidentSubType).build());
        return request;
    }

    private String singleError(String component, List<String> observations) {
        return singleError(component, observations, null);
    }

    private String singleError(String component, List<String> observations, List<Document> documents) {
        Map<String, String> errorMap = errors(component, observations, documents);
        assertEquals(1, errorMap.size(), errorMap.toString());
        return errorMap.keySet().iterator().next();
    }

    private Map<String, String> errors(String component, List<String> observations) {
        return errors(component, observations, null);
    }

    private Map<String, String> errors(String component, List<String> observations, List<Document> documents) {
        IncidentRequest request = new IncidentRequest();
        request.setIncident(Incident.builder().component(component).observations(observations).build());
        request.setWorkflow(Workflow.builder().verificationDocuments(documents).build());
        Map<String, String> errorMap = new HashMap<>();
        validator.validateComponentAndObservations(request, mdmsData, errorMap);
        return errorMap;
    }
}
