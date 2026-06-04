package org.egov.project.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.models.project.Project;
import org.egov.common.models.project.ProjectRequest;
import org.egov.project.config.ProjectConfiguration;
import org.egov.project.repository.ProjectRepository;
import org.egov.project.util.MDMSUtils;
import org.egov.project.web.models.ProjectNameResult;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Option 4 project name: [STATE][P#]-[FYTY]-[HF]-[JUST]
 * Example: KA23-2627-190-00120-1
 */
@Service
@Slf4j
public class ProjectNameGenerationService {

    private static final Pattern OPTION4_NAME_PATTERN = Pattern.compile("^(.+)-(\\d{4})-(\\d+)-(.+)$");
    private static final String JUS_PREFIX = "JUS-";
    private static final String SCHEDULED_STATUS = "SCHEDULED";

    private final ProjectRepository projectRepository;
    private final ProjectConfiguration projectConfiguration;
    private final MDMSUtils mdmsUtils;
    private final ObjectMapper objectMapper;

    public ProjectNameGenerationService(
            ProjectRepository projectRepository,
            ProjectConfiguration projectConfiguration,
            MDMSUtils mdmsUtils,
            @Qualifier("objectMapper") ObjectMapper objectMapper) {
        this.projectRepository = projectRepository;
        this.projectConfiguration = projectConfiguration;
        this.mdmsUtils = mdmsUtils;
        this.objectMapper = objectMapper;
    }

    public static String getScheduledStatus() {
        return SCHEDULED_STATUS;
    }

    /**
     * Resolves 2-letter state code for IdGen prefix (e.g. KA).
     */
    public String resolveStateCode(Project project, RequestInfo requestInfo) {
        log.trace("Entering resolveStateCode for project: {}", project.getId());
        try {
            if (project.getAddress() != null && StringUtils.isNotBlank(project.getAddress().getBoundary())) {
                String boundary = project.getAddress().getBoundary();
                String stateName = extractStateNameFromBoundary(boundary);
                if (stateName != null) {
                    String stateCode = getCodeFromMDMS(project, requestInfo, project.getTenantId(), "State", stateName);
                    if (stateCode != null) {
                        return stateCode;
                    }
                }
            }

            String tenantId = project.getTenantId();
            if (tenantId != null && tenantId.contains(".")) {
                String state = tenantId.split("\\.")[1];
                if (StringUtils.isNotBlank(state) && state.length() >= 2) {
                    String stateCode = getCodeFromMDMS(project, requestInfo, tenantId, "State", state);
                    if (stateCode != null) {
                        return stateCode;
                    }
                    return state.toUpperCase().substring(0, Math.min(2, state.length()));
                }
            }
        } catch (Exception e) {
            log.error("Error resolving state code for project: {}", project.getId(), e);
        }
        log.warn("Using fallback state code XX for project: {}", project.getId());
        return "XX";
    }

    /**
     * IdGen format for state-scoped project numbers (KA1, KA2, ...).
     */
    public String getProjectNumberIdFormat(String stateCode) {
        return stateCode + "[SEQ_NUMBER]";
    }

    /**
     * IdGen id name for state-scoped sequences.
     */
    public String getProjectNumberIdName(String stateCode) {
        return projectConfiguration.getIdgenProjectNumberName() + "." + stateCode;
    }

    /**
     * Builds Option 4 name. Draft uses HF=0; scheduled uses live facility count.
     */
    public ProjectNameResult generateProjectName(Project project, RequestInfo requestInfo, boolean draft) {
        log.info("Generating Option 4 project name for project: {}, draft: {}", project.getId(), draft);
        try {
            int healthFacilityCount = draft ? 0 : countLinkedHealthFacilities(project.getId(), project.getTenantId());
            String name = buildProjectName(project, healthFacilityCount);
            log.info("Generated project name: {}", name);
            return ProjectNameResult.builder()
                    .name(name)
                    .isDuplicateName(false)
                    .build();
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error generating project name for project: {}", project.getId(), e);
            throw new CustomException("PROJECT_NAME_GENERATION_FAILED", "Failed to generate project name: " + e.getMessage());
        }
    }

    public String buildProjectName(Project project, int healthFacilityCount) {
        String projectNumber = project.getProjectNumber();
        if (StringUtils.isBlank(projectNumber)) {
            throw new CustomException("PROJECT_NUMBER_REQUIRED", "Project number is required before generating project name");
        }
        String fyty = getFyty(project);
        String justNumeric = getJustificationNumeric(project);
        return String.format("%s-%s-%d-%s", projectNumber.trim(), fyty, healthFacilityCount, justNumeric);
    }

    public int parseHealthFacilityCountFromName(String name) {
        if (StringUtils.isBlank(name)) {
            return -1;
        }
        Matcher matcher = OPTION4_NAME_PATTERN.matcher(name.trim());
        if (matcher.matches()) {
            return Integer.parseInt(matcher.group(3));
        }
        return -1;
    }

    public boolean isOption4NameFormat(String name) {
        return StringUtils.isNotBlank(name) && OPTION4_NAME_PATTERN.matcher(name.trim()).matches();
    }

    private String getJustificationNumeric(Project project) {
        String justificationCode = extractJustificationCode(project.getAdditionalDetails());
        if (StringUtils.isBlank(justificationCode)) {
            throw new CustomException("JUSTIFICATION_CODE_REQUIRED", "Justification code is required for project name generation");
        }
        String trimmed = justificationCode.trim().toUpperCase();
        if (trimmed.startsWith(JUS_PREFIX)) {
            trimmed = trimmed.substring(JUS_PREFIX.length());
        }
        if (trimmed.startsWith("-")) {
            trimmed = trimmed.substring(1);
        }
        if (!trimmed.matches("[0-9]+(-[0-9]+)*")) {
            throw new CustomException("INVALID_JUSTIFICATION_CODE",
                    "Justification code must contain numeric values only after JUS- prefix: " + justificationCode);
        }
        return trimmed;
    }

    private String extractJustificationCode(Object additionalDetails) {
        if (additionalDetails == null) {
            return null;
        }
        try {
            JsonNode node = objectMapper.valueToTree(additionalDetails);
            if (node != null && node.has("justificationCode") && !node.get("justificationCode").isNull()) {
                return node.get("justificationCode").asText();
            }
        } catch (Exception e) {
            log.error("Error reading justificationCode from additionalDetails", e);
        }
        return null;
    }

    /**
     * FYTY: last two digits of start and end years (e.g. 2026-2027 -> 2627).
     */
    private String getFyty(Project project) {
        if (project.getStartDate() == null || project.getEndDate() == null) {
            throw new CustomException("INVALID_PROJECT_DATES", "Start date and end date are required for project name generation");
        }

        LocalDateTime startDate = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(project.getStartDate()), ZoneId.systemDefault());
        LocalDateTime endDate = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(project.getEndDate()), ZoneId.systemDefault());

        if (startDate.isAfter(endDate)) {
            throw new CustomException("INVALID_PROJECT_DATES", "Start date cannot be greater than end date");
        }
        if (startDate.toLocalDate().equals(endDate.toLocalDate())) {
            throw new CustomException("INVALID_PROJECT_DURATION", "Project must have a duration of at least 1 day");
        }

        int startYy = startDate.getYear() % 100;
        int endYy = endDate.getYear() % 100;
        return String.format("%02d%02d", startYy, endYy);
    }

    private int countLinkedHealthFacilities(String projectId, String tenantId) {
        return projectRepository.countProjectFacilitiesByProjectId(projectId, tenantId);
    }

    private String extractStateNameFromBoundary(String boundary) {
        if (StringUtils.isBlank(boundary)) {
            return null;
        }
        String[] boundaryParts = boundary.split("_");
        String stateName = null;
        if (boundaryParts.length >= 2 && "India".equalsIgnoreCase(boundaryParts[0])) {
            stateName = boundaryParts[1];
        } else if (boundaryParts.length >= 1) {
            stateName = boundaryParts[0];
        }
        if (stateName != null && !stateName.equalsIgnoreCase("nan")
                && !stateName.equalsIgnoreCase("XYZ") && stateName.trim().length() > 0) {
            return stateName.trim();
        }
        return null;
    }

    private String getCodeFromMDMS(Project project, RequestInfo requestInfo, String tenantId, String masterType, String searchName) {
        try {
            String rootTenantId = tenantId.split("\\.")[0];
            Project dummyProject = Project.builder().tenantId(tenantId).build();
            ProjectRequest projectRequest = ProjectRequest.builder()
                    .requestInfo(requestInfo)
                    .projects(List.of(dummyProject))
                    .build();
            Object mdmsResponse = mdmsUtils.mDMSCall(projectRequest, rootTenantId);
            return extractCodeFromMDMSResponse(mdmsResponse, masterType, searchName);
        } catch (Exception e) {
            log.error("Error getting {} code from MDMS for {}: {}", masterType, searchName, e.getMessage());
            return null;
        }
    }

    private String extractCodeFromMDMSResponse(Object mdmsResponse, String masterType, String searchName) {
        if (!(mdmsResponse instanceof LinkedHashMap)) {
            return null;
        }
        LinkedHashMap<String, Object> responseMap = (LinkedHashMap<String, Object>) mdmsResponse;
        LinkedHashMap<String, Object> mdmsRes = (LinkedHashMap<String, Object>) responseMap.get("MdmsRes");
        if (mdmsRes == null) {
            return null;
        }
        LinkedHashMap<String, Object> commonMasters = (LinkedHashMap<String, Object>) mdmsRes.get("common-masters");
        if (commonMasters == null) {
            return null;
        }
        String schemaKey = "State".equals(masterType) ? "StateInfo" : masterType;
        List<LinkedHashMap<String, Object>> masterList = (List<LinkedHashMap<String, Object>>) commonMasters.get(schemaKey);
        if (masterList == null) {
            return null;
        }
        for (LinkedHashMap<String, Object> item : masterList) {
            String name = (String) item.get("name");
            Boolean active = (Boolean) item.get("active");
            if (searchName.equalsIgnoreCase(name) && Boolean.TRUE.equals(active)) {
                String code = (String) item.get("code");
                if (StringUtils.isNotBlank(code)) {
                    return code;
                }
            }
        }
        return null;
    }
}
