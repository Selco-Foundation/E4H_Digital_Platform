package org.egov.field_planner.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.models.project.Project;
import org.egov.common.models.project.ProjectRequest;
import org.egov.common.models.project.ProjectResponse;
import org.egov.field_planner.config.FieldPlannerConfiguration;
import org.egov.field_planner.util.AssessmentConstants;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssessmentProjectService {

    private final FieldPlannerConfiguration configuration;
    private final ServiceRequestRepository serviceRequestRepository;

    @Qualifier("objectMapper")
    private final ObjectMapper mapper;

    public Project getProjectById(RequestInfo requestInfo, String tenantId, String projectId) {
        Project project = Project.builder().id(projectId).tenantId(tenantId).build();
        ProjectRequest projectRequest = ProjectRequest.builder()
                .requestInfo(requestInfo)
                .projects(List.of(project))
                .build();
        String url = configuration.getProjectServiceHost() + configuration.getProjectServiceSearchUrl()
                + "?tenantId=" + tenantId + "&offset=0&limit=10";
        Object response = serviceRequestRepository.fetchResult(new StringBuilder(url), projectRequest);
        ProjectResponse projectResponse = mapper.convertValue(response, ProjectResponse.class);
        if (projectResponse != null && projectResponse.getProject() != null && !projectResponse.getProject().isEmpty()) {
            return projectResponse.getProject().get(0);
        }
        return null;
    }

    public Set<String> getProjectFacilityIds(RequestInfo requestInfo, String tenantId, String projectId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("RequestInfo", requestInfo);
        payload.put("ProjectFacility", Map.of("projectId", List.of(projectId)));

        String url = configuration.getProjectServiceHost() + configuration.getProjectFacilitySearchUrl()
                + "?tenantId=" + tenantId + "&offset=0&limit=1000&includeDeleted=false";

        Object response = serviceRequestRepository.fetchResult(new StringBuilder(url), payload);
        Map<String, Object> responseMap = mapper.convertValue(response, Map.class);
        List<Map<String, Object>> projectFacilities = (List<Map<String, Object>>) responseMap.get("ProjectFacilities");
        if (projectFacilities == null) {
            return Collections.emptySet();
        }
        return projectFacilities.stream()
                .map(pf -> (String) pf.get("facilityId"))
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
    }

    public void validateProjectExists(RequestInfo requestInfo, String tenantId, String projectId) {
        Project project = getProjectById(requestInfo, tenantId, projectId);
        if (project == null) {
            throw new CustomException(AssessmentConstants.ASSESSMENT_PROJECT_NOT_FOUND,
                    "Project not found: " + projectId);
        }
    }
}
