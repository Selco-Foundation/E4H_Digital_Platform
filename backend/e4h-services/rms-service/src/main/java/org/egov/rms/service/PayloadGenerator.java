package org.egov.rms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.rms.config.RMSConfiguration;
import org.egov.rms.model.Alert;
import org.egov.rms.model.FacilityDetails;
import org.egov.rms.model.IMServiceRequest;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayloadGenerator {

    private final RMSConfiguration config;
    private final FacilityServiceClient facilityServiceClient;
    private final ObjectMapper objectMapper;

    /**
     * Generates IM service ticket payload from alert
     */
    public IMServiceRequest generateTicketPayload(Alert alert, RequestInfo requestInfo) {
        log.debug("Generating ticket payload for alert: {}", alert.getId());

        try {
            // Fetch facility details
            FacilityDetails facilityDetails = facilityServiceClient.getFacilityByHfrId(
                    alert.getHfrId(), config.getDefaultTenantId());

            if (facilityDetails == null) {
                log.warn("Facility not found for hfrId: {}", alert.getHfrId());
                return null;
            }

            // Map alert type/subtype to IM service incident type/subtype
            String incidentType = mapAlertTypeToIncidentType(alert.getAlertType());
            String incidentSubType = mapAlertSubTypeToIncidentSubType(alert.getAlertSubType(), alert.getAlertType());

            // Build incident payload
            IMServiceRequest.Incident incident = IMServiceRequest.Incident.builder()
                    .incidentType(incidentType)
                    .incidentSubType(incidentSubType)
                    .tenantId(config.getDefaultTenantId())
                    .district(facilityDetails.getDistrict())
                    .block(facilityDetails.getBlock())
                    .phcType(facilityDetails.getPhcType())
                    .phcSubType(facilityDetails.getPhcSubType())
                    .comments(buildComments(alert, facilityDetails))
                    .systemFunctional("NON_FUNCTIONAL")
                    .applicationStatus("PENDINGFORASSIGNMENT")
                    .source("RMS")
                    .additionalDetail(buildAdditionalDetail(alert, facilityDetails))
                    .reporter(buildReporter(requestInfo))
                    .build();

            // Build workflow
            IMServiceRequest.Workflow workflow = IMServiceRequest.Workflow.builder()
                    .action("APPLY")
                    .comments("Auto-generated ticket from RMS service")
                    .build();

            return IMServiceRequest.builder()
                    .requestInfo(requestInfo)
                    .incident(incident)
                    .workflow(workflow)
                    .build();

        } catch (Exception e) {
            log.error("Error generating ticket payload for alert: {}", alert.getId(), e);
            return null;
        }
    }

    /**
     * Maps RMS alert type to IM service incident type
     */
    private String mapAlertTypeToIncidentType(Alert.AlertType alertType) {
        switch (alertType) {
            case PANEL:
                return "Panel";
            case INVERTER:
                return "Inverter";
            case BATTERY:
                return "Battery";
            case GRID:
                return "Grid";
            default:
                return "Solar System";
        }
    }

    /**
     * Maps RMS alert sub-type to IM service incident sub-type
     */
    private String mapAlertSubTypeToIncidentSubType(Alert.AlertSubType alertSubType, Alert.AlertType alertType) {
        switch (alertSubType) {
            case LOW_GENERATION:
                return "Low Generation (RMS data)";
            case SHUTDOWN:
                return "Shutdown (RMS data)";
            case HIGH_VOLTAGE:
                return "High Voltage (RMS data)";
            case BURNT_DISCONNECTED:
                return "Burnt/Disconnected (RMS data)";
            case DEEP_DISCHARGING:
                return "Deep Discharging / Overcharging (RMS data)";
            case OVERCHARGING:
                return "Deep Discharging / Overcharging (RMS data)";
            case VOLTAGE_VARIATION_LOW:
                return "Voltage Variation - Low (RMS data)";
            case VOLTAGE_VARIATION_HIGH:
                return "Voltage Variation - High (RMS data)";
            default:
                return "RMS Data Alert";
        }
    }

    /**
     * Builds comments for the ticket
     */
    private String buildComments(Alert alert, FacilityDetails facilityDetails) {
        StringBuilder comments = new StringBuilder();
        comments.append("RMS Alert: ").append(alert.getAlertType()).append(" - ")
                .append(alert.getAlertSubType()).append("\n");
        comments.append("Facility: ").append(facilityDetails.getFacilityName()).append("\n");
        comments.append("HFR ID: ").append(alert.getHfrId()).append("\n");
        comments.append("Detected at: ").append(alert.getDetectedAt()).append("\n");
        
        if (alert.getMetadata() != null && !alert.getMetadata().isEmpty()) {
            comments.append("Details: ").append(alert.getMetadata());
        }
        
        return comments.toString();
    }

    /**
     * Builds additional detail JSON
     */
    private Map<String, Object> buildAdditionalDetail(Alert alert, FacilityDetails facilityDetails) {
        Map<String, Object> additionalDetail = new HashMap<>();
        additionalDetail.put("fileStoreId", new java.util.ArrayList<>());
        additionalDetail.put("reopenreason", new java.util.ArrayList<>());
        additionalDetail.put("rejectReason", new java.util.ArrayList<>());
        additionalDetail.put("sendBackReason", new java.util.ArrayList<>());
        additionalDetail.put("sendBackSubReason", new java.util.ArrayList<>());
        additionalDetail.put("rmsAlertId", alert.getId());
        additionalDetail.put("rmsFacilityId", alert.getFacilityId());
        additionalDetail.put("rmsMetadata", alert.getMetadata());
        return additionalDetail;
    }

    /**
     * Builds reporter user from system user
     */
    private IMServiceRequest.User buildReporter(RequestInfo requestInfo) {
        return IMServiceRequest.User.builder()
                .uuid(config.getSystemUserUuid())
                .tenantId(config.getDefaultTenantId())
                .build();
    }
}

