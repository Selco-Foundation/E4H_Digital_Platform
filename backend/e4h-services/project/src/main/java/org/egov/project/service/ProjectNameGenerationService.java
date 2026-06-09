package org.egov.project.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.models.project.Project;
import org.egov.common.models.project.ProjectRequest;
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
 * Revised project ID (name) format: [STATE]-[FYTY]-[HF]-[JUST]
 * Example: KA-2627-190-00120-1
 */
@Service
@Slf4j
public class ProjectNameGenerationService {

    private static final Pattern REVISED_PROJECT_ID_PATTERN =
            Pattern.compile("^([A-Z]{2})-(\\d{4})-(\\d+)-([0-9]+(-[0-9]+)*)$");
    private static final Pattern JUSTIFICATION_CODE_PATTERN =
            Pattern.compile("^JUS-[0-9]+(-[0-9]+)*$", Pattern.CASE_INSENSITIVE);
    private static final String JUS_PREFIX = "JUS-";
    private static final String SCHEDULED_STATUS = "SCHEDULED";
    public static final String JUSTIFICATION_CODE_MESSAGE =
            "Justification code is required and must follow the format JUS-{numbers} (e.g., JUS-393, JUS-8080-89).";

    private final ProjectRepository projectRepository;
    private final MDMSUtils mdmsUtils;
    private final ObjectMapper objectMapper;

    public ProjectNameGenerationService(
            ProjectRepository projectRepository,
            MDMSUtils mdmsUtils,
            @Qualifier("objectMapper") ObjectMapper objectMapper) {
        this.projectRepository = projectRepository;
        this.mdmsUtils = mdmsUtils;
        this.objectMapper = objectMapper;
    }

    public static String getScheduledStatus() {
        return SCHEDULED_STATUS;
    }

    /**
     * Resolves 2-letter state code from address / MDMS (e.g. KA).
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
                        return stateCode.toUpperCase();
                    }
                }
                if (boundary.length() == 2) {
                    return boundary.toUpperCase();
                }
            }

            String tenantId = project.getTenantId();
            if (tenantId != null && tenantId.contains(".")) {
                String state = tenantId.split("\\.")[1];
                if (StringUtils.isNotBlank(state) && state.length() >= 2) {
                    String stateCode = getCodeFromMDMS(project, requestInfo, tenantId, "State", state);
                    if (stateCode != null) {
                        return stateCode.toUpperCase();
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
     * Builds revised project ID. Draft uses HF=0; scheduled uses live facility count.
     */
    public ProjectNameResult generateProjectName(Project project, RequestInfo requestInfo, boolean draft) {
        return generateProjectName(project, requestInfo, draft, null);
    }

    /**
     * @param facilityCountOverride when non-null, used instead of DB count (e.g. before persister flush)
     */
    public ProjectNameResult generateProjectName(Project project, RequestInfo requestInfo, boolean draft,
                                               Integer facilityCountOverride) {
        log.info("Generating project ID for project: {}, draft: {}", project.getId(), draft);
        try {
            int healthFacilityCount = draft ? 0
                    : (facilityCountOverride != null
                    ? facilityCountOverride
                    : countLinkedHealthFacilities(project.getId(), project.getTenantId()));
            String name = buildProjectName(project, requestInfo, healthFacilityCount);
            log.info("Generated project ID: {}", name);
            return ProjectNameResult.builder()
                    .name(name)
                    .isDuplicateName(false)
                    .build();
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error generating project ID for project: {}", project.getId(), e);
            throw new CustomException("PROJECT_NAME_GENERATION_FAILED", "Failed to generate project ID: " + e.getMessage());
        }
    }

    public String buildProjectName(Project project, RequestInfo requestInfo, int healthFacilityCount) {
        String stateCode = resolveStateCode(project, requestInfo);
        String fyty = getFyty(project);
        String justNumeric = getJustificationNumeric(project);
        return String.format("%s-%s-%d-%s", stateCode, fyty, healthFacilityCount, justNumeric);
    }

    public int parseHealthFacilityCountFromName(String name) {
        if (StringUtils.isBlank(name)) {
            return -1;
        }
        Matcher matcher = REVISED_PROJECT_ID_PATTERN.matcher(name.trim().toUpperCase());
        if (matcher.matches()) {
            return Integer.parseInt(matcher.group(3));
        }
        return -1;
    }

    public boolean isRevisedProjectIdFormat(String name) {
        return StringUtils.isNotBlank(name) && REVISED_PROJECT_ID_PATTERN.matcher(name.trim().toUpperCase()).matches();
    }

    public boolean isValidJustificationCodeFormat(String justificationCode) {
        return StringUtils.isNotBlank(justificationCode)
                && JUSTIFICATION_CODE_PATTERN.matcher(justificationCode.trim()).matches();
    }

    public void validateJustificationCodeFormat(String justificationCode) {
        if (!isValidJustificationCodeFormat(justificationCode)) {
            throw new CustomException("INVALID_JUSTIFICATION_CODE", JUSTIFICATION_CODE_MESSAGE);
        }
    }

    private String getJustificationNumeric(Project project) {
        String justificationCode = extractJustificationCode(project.getAdditionalDetails());
        if (StringUtils.isBlank(justificationCode)) {
            throw new CustomException("JUSTIFICATION_CODE_REQUIRED", JUSTIFICATION_CODE_MESSAGE);
        }
        validateJustificationCodeFormat(justificationCode);
        String trimmed = justificationCode.trim().toUpperCase().substring(JUS_PREFIX.length());
        if (trimmed.startsWith("-")) {
            trimmed = trimmed.substring(1);
        }
        return trimmed;
    }

    public String extractJustificationCode(Object additionalDetails) {
        if (additionalDetails == null) {
            return null;
        }
        try {
            JsonNode node = additionalDetails instanceof JsonNode
                    ? (JsonNode) additionalDetails
                    : objectMapper.valueToTree(additionalDetails);
            node = normalizeAdditionalDetailsNode(node);
            if (node != null && node.isObject()
                    && node.has("justificationCode") && !node.get("justificationCode").isNull()) {
                String value = node.get("justificationCode").asText();
                return value == null || value.isBlank() ? null : value.trim();
            }
        } catch (Exception e) {
            log.error("Error reading justificationCode from additionalDetails", e);
        }
        return null;
    }

    private JsonNode normalizeAdditionalDetailsNode(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isTextual()) {
            String text = node.asText();
            if (StringUtils.isBlank(text)) {
                return null;
            }
            try {
                JsonNode parsed = objectMapper.readTree(text);
                return parsed.isObject() ? parsed : null;
            } catch (Exception e) {
                log.debug("additionalDetails is a scalar string, not JSON object: {}", text);
                return null;
            }
        }
        return node;
    }

    /**
     * FYTY: last two digits of start and end years (e.g. 2026-2027 -> 2627).
     */
    private String getFyty(Project project) {
        if (project.getStartDate() == null || project.getEndDate() == null) {
            throw new CustomException("INVALID_PROJECT_DATES", "Start date and end date are required for project ID generation");
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
