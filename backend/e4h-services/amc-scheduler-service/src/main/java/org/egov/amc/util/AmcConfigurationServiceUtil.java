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
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    @Qualifier("objectMapper")
    ObjectMapper mapper;

    public AmcConfigurationServiceUtil(ServiceRequestRepository serviceRequestRepository, AMCServiceConfiguration amcConfigurationnerConfiguration, KafkaTemplate<String, Object> kafkaTemplate) {
        this.serviceRequestRepository = serviceRequestRepository;
        this.amcConfigurationnerConfiguration = amcConfigurationnerConfiguration;
        this.kafkaTemplate = kafkaTemplate;
    }

    public AuditDetails getAuditDetails(String by, AuditDetails auditDetails, Boolean isCreate) {
        Long time = System.currentTimeMillis();
        if (isCreate)
            return AuditDetails.builder().createdBy(by).lastModifiedBy(by).createdTime(time).lastModifiedTime(time).build();
        else
            return AuditDetails.builder().createdBy(auditDetails.getCreatedBy()).lastModifiedBy(by)
                    .createdTime(auditDetails.getCreatedTime()).lastModifiedTime(time).build();
    }

    // Generates AMC visits based on AMC Duration (in months) and AMC Frequency
    public List<Long> generateAmcVisits(long startDateMillis, long endDateMillis, int frequencyMonths) {
        if (startDateMillis <= 0) {
            throw new IllegalArgumentException("startDateMillis must be a positive timestamp.");
        }
        if (endDateMillis <= 0) {
            throw new IllegalArgumentException("endDateMillis must be a positive timestamp.");
        }
        if (endDateMillis <= startDateMillis) {
            throw new IllegalArgumentException("endDateMillis must be greater than startDateMillis.");
        }
        if (frequencyMonths <= 0) {
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
                throw new IllegalStateException("Infinite-loop protection triggered.");
            }
        }

        return visits;
    }

    public ProjectStaff createProjectStaff(RequestInfo requestInfo, List<ProjectStaff> staffs) {
        ProjectStaffBulkRequest request  = ProjectStaffBulkRequest.builder().requestInfo(requestInfo).projectStaff(staffs).build();
        String url = amcConfigurationnerConfiguration.getProjectServiceHost() + amcConfigurationnerConfiguration.getProjectStaffCreateUrl();
        Object response = serviceRequestRepository.fetchResult(new StringBuilder(url), request);
        ProjectStaffResponse projectResponse = mapper.convertValue(response, ProjectStaffResponse.class);
        if(projectResponse != null && projectResponse.getProjectStaff() !=null){
            return projectResponse.getProjectStaff();
        }
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
