package org.egov.amc.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.egov.amc.service.ServiceRequestRepository;
import org.egov.amc.web.models.AmcConfiguration;
import org.egov.amc.web.models.AssetAmc;
import org.egov.amc.web.models.ScheduledVisit;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.http.client.ServiceRequestClient;
import org.egov.common.models.project.*;
import org.egov.amc.config.AMCServiceConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static java.util.Objects.isNull;

@Slf4j
@Component
public class AmcConfigurationServiceUtil {
    @Autowired
    private ObjectMapper objectMapper;

    private final ServiceRequestRepository serviceRequestRepository;

    private final AMCServiceConfiguration amcConfigurationnerConfiguration;

    public AmcConfigurationServiceUtil(ServiceRequestRepository serviceRequestRepository, AMCServiceConfiguration amcConfigurationnerConfiguration) {
        this.serviceRequestRepository = serviceRequestRepository;
        this.amcConfigurationnerConfiguration = amcConfigurationnerConfiguration;
    }

    public AuditDetails getAuditDetails(String by, AuditDetails auditDetails, Boolean isCreate) {
        log.trace("Entering getAuditDetails method, isCreate: {}", isCreate);
        Long time = System.currentTimeMillis();
        if (isCreate) {
            log.debug("Creating new audit details for user: {}", by);
            return AuditDetails.builder().createdBy(by).lastModifiedBy(by).createdTime(time).lastModifiedTime(time).build();
        } else {
            log.debug("Updating audit details for user: {}", by);
            return AuditDetails.builder().createdBy(auditDetails.getCreatedBy()).lastModifiedBy(by)
                    .createdTime(auditDetails.getCreatedTime()).lastModifiedTime(time).build();
        }
    }

    // Generates AMC visits based on AMC Duration (in months) and AMC Frequency
    public List<Long> generateAmcVisits(long startDateMillis, long endDateMillis, int frequencyMonths) {
        log.trace("Entering generateAmcVisits method, startDate: {}, endDate: {}, frequencyMonths: {}", 
                startDateMillis, endDateMillis, frequencyMonths);
        if (startDateMillis <= 0) {
            log.error("Invalid startDateMillis: {}", startDateMillis);
            throw new IllegalArgumentException("startDateMillis must be a positive timestamp.");
        }
        if (endDateMillis <= 0) {
            log.error("Invalid endDateMillis: {}", endDateMillis);
            throw new IllegalArgumentException("endDateMillis must be a positive timestamp.");
        }
        if (endDateMillis <= startDateMillis) {
            log.error("endDateMillis {} must be greater than startDateMillis {}", endDateMillis, startDateMillis);
            throw new IllegalArgumentException("endDateMillis must be greater than startDateMillis.");
        }
        if (frequencyMonths <= 0) {
            log.error("Invalid frequencyMonths: {}", frequencyMonths);
            throw new IllegalArgumentException("frequencyMonths must be > 0.");
        }

        List<Long> visits = new ArrayList<>();
        // Convert startDate → LocalDate
        LocalDate startDate = Instant.ofEpochMilli(startDateMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        // Compute AMC end date = startDate + duration
//        LocalDate endDate = startDate.plusMonths(durationMonths);

        // Convert endDate → LocalDate
        LocalDate endDate = Instant.ofEpochMilli(endDateMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        // First visit = startDate + frequency
        LocalDate visitDate = startDate.plusMonths(frequencyMonths);

        // Generate all visits
        int safetyCounter = 0; // avoid infinite loops
        while (!visitDate.isAfter(endDate)) {
            long visitMillis = visitDate
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();

            visits.add(visitMillis);

            // Next visit
            visitDate = visitDate.plusMonths(frequencyMonths);

            // Safety (ex: bad data → infinite loop)
            if (++safetyCounter > 1000) {
                log.error("Infinite-loop protection triggered while generating AMC visits");
                throw new IllegalStateException("Infinite-loop protection triggered.");
            }
        }

        log.debug("Generated {} AMC visit(s) between startDate: {} and endDate: {}", visits.size(), startDateMillis, endDateMillis);
        log.info("Generated {} AMC visit(s) with frequency: {} months", visits.size(), frequencyMonths);
        return visits;
    }

    public ProjectStaff createProjectStaff(RequestInfo requestInfo, List<ProjectStaff> staffs) {
        log.trace("Entering createProjectStaff method, staff count: {}", staffs != null ? staffs.size() : 0);
        ProjectStaffBulkRequest request  = ProjectStaffBulkRequest.builder().requestInfo(requestInfo).projectStaff(staffs).build();
        String url = amcConfigurationnerConfiguration.getProjectServiceHost() + amcConfigurationnerConfiguration.getProjectStaffCreateUrl();
        log.debug("Calling project service to create project staff at URL: {}", url);
        Object response = serviceRequestRepository.fetchResult(new StringBuilder(url), request);
        ProjectStaffResponse projectResponse = objectMapper.convertValue(response, ProjectStaffResponse.class);
        if(projectResponse != null && projectResponse.getProjectStaff() !=null){
            log.info("Successfully created {} project staff assignment(s)", staffs != null ? staffs.size() : 0);
            return projectResponse.getProjectStaff();
        }
        log.warn("Project staff creation returned null or empty response");
        return null;
    }

    public void mergeAdditionalDetails(AmcConfiguration amcConfiguration, AmcConfiguration amcConfigurationFromDb) {
        JsonNode json = jsonMerge(objectMapper.valueToTree(amcConfigurationFromDb.getAdditionalDetails()),
                objectMapper.valueToTree(amcConfiguration.getAdditionalDetails()));
        amcConfiguration.setAdditionalDetails(objectMapper.convertValue(json, Map.class));
    }

    public void mergeAssetAmcAdditionalDetails(AssetAmc assetAmc, AssetAmc assetAmcFromDb) {
        JsonNode json = jsonMerge(objectMapper.valueToTree(assetAmcFromDb.getAdditionalDetails()),
                objectMapper.valueToTree(assetAmc.getAdditionalDetails()));
        assetAmc.setAdditionalDetails(objectMapper.convertValue(json, Map.class));
    }

    public void mergeScheduledVisitAdditionalDetails(ScheduledVisit assetAmc, ScheduledVisit assetAmcFromDb) {
        JsonNode json = jsonMerge(objectMapper.valueToTree(assetAmcFromDb.getAdditionalDetails()),
                objectMapper.valueToTree(assetAmc.getAdditionalDetails()));
        assetAmc.setAdditionalDetails(objectMapper.convertValue(json, Map.class));
    }

    /**
     * Method to merge additional details during update
     *
     * @param mainNode
     * @param updateNode
     * @return
     */
    public JsonNode jsonMerge(JsonNode mainNode, JsonNode updateNode) {

        if (isNull(mainNode) || mainNode.isNull())
            return updateNode;
        if (isNull(updateNode) || updateNode.isNull())
            return mainNode;

        Iterator<String> fieldNames = updateNode.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            JsonNode jsonNode = mainNode.get(fieldName);
            // if field exists and is an embedded object
            if (jsonNode != null && jsonNode.isObject()) {
                jsonMerge(jsonNode, updateNode.get(fieldName));
            } else {
                if (mainNode instanceof ObjectNode) {
                    // Overwrite field
                    JsonNode value = updateNode.get(fieldName);
                    ((ObjectNode) mainNode).set(fieldName, value);
                }
            }

        }
        return mainNode;
    }
}
