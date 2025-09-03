package org.egov.project.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.models.project.Project;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.models.project.ProjectRequest;
import org.egov.project.config.ProjectConfiguration;
import org.egov.project.repository.ProjectRepository;
import org.egov.project.util.BoundaryV2Util;
import org.egov.project.util.MDMSUtils;
import org.egov.project.web.models.ProjectNameResult;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProjectNameGenerationService {

    private final ProjectRepository projectRepository;
    private final BoundaryV2Util boundaryV2Util;
    private final ProjectConfiguration projectConfiguration;
    private final MDMSUtils mdmsUtils;


    /**
     * Checks if name generation should be skipped based on project type
     */
    private boolean shouldSkipNameGeneration(Project project) {
        String projectType = project.getProjectType();
        return "FieldPlan".equals(projectType) || "Facility".equals(projectType);
    }

    /**
     * Gets the project code from project details or uses default
     */
    private String getProjectCode(Project project, RequestInfo requestInfo) {
        try {
            // Try to get project code from MDMS based on project type
            if (project.getProjectType() != null && requestInfo != null) {
                String projectTypeName = project.getProjectType();
                String tenantId = project.getTenantId() != null ? project.getTenantId() : "in";
                
                log.info("Fetching project type code for project type: {} from tenant: {}", projectTypeName, tenantId);
                
                String projectCode = getCodeFromMDMS(project, requestInfo, tenantId, "ProjectType", projectTypeName);
                if (projectCode != null) {
                    log.info("Found project type code: {} for project type: {}", projectCode, projectTypeName);
                    return projectCode;
                }
                
                log.warn("Project type code not found in MDMS for: {}, using default: {}", 
                        projectTypeName, projectConfiguration.getProjectNameDefaultCode());
            }
            
            // Fallback to configured default
            log.info("Using default project code: {}", projectConfiguration.getProjectNameDefaultCode());
            return projectConfiguration.getProjectNameDefaultCode();
            
        } catch (Exception e) {
            log.error("Error getting project code for project: {}, using default", project.getId(), e);
            return projectConfiguration.getProjectNameDefaultCode();
        }
    }

    /**
     * Gets the state code from project boundary or MDMS
     */
    private String getStateCode(Project project, RequestInfo requestInfo) {
        try {
            // First try to get state code from MDMS based on boundary
            if (project.getAddress() != null && project.getAddress().getBoundary() != null) {
                String boundary = project.getAddress().getBoundary();
                String[] boundaryParts = boundary.split("_");
                if (boundaryParts.length >= 2) {
                    String stateName = boundaryParts[1]; // Second part is state
                    String stateCode = getCodeFromMDMS(project, requestInfo, project.getTenantId(), "State", stateName);
                    if (stateCode != null) {
                        log.info("Found state code from MDMS: {} for state: {}", stateCode, stateName);
                        return stateCode;
                    }
                }
            }
            
            // Fallback to tenant ID based mapping
            String tenantId = project.getTenantId();
            if (tenantId != null && tenantId.contains(".")) {
                String state = tenantId.split("\\.")[0];
                String stateCode = getCodeFromMDMS(project, requestInfo, tenantId, "State", state);
                if (stateCode != null) {
                    log.info("Found state code from MDMS using tenant: {} for state: {}", stateCode, state);
                    return stateCode;
                }
            }
            
            log.warn("State code not found in MDMS, using fallback mapping");
            return getStateCodeFromFallback(project);
            
        } catch (Exception e) {
            log.error("Error getting state code for project: {}, using fallback", project.getId(), e);
            return getStateCodeFromFallback(project);
        }
    }

    /**
     * Generic method to get code from MDMS
     */
    private String getCodeFromMDMS(Project project, RequestInfo requestInfo, String tenantId, String masterType, String searchName) {
        try {
            String rootTenantId = tenantId.split("\\.")[0];
            
            // Create a dummy project for MDMS call
            Project dummyProject = Project.builder()
                    .tenantId(tenantId)
                    .build();
            
            ProjectRequest projectRequest = ProjectRequest.builder()
                    .requestInfo(requestInfo)
                    .projects(List.of(dummyProject))
                    .build();
            
            // Call MDMS to get data
            Object mdmsResponse = mdmsUtils.mDMSCall(projectRequest, rootTenantId);
            
            return extractCodeFromMDMSResponse(mdmsResponse, masterType, searchName);
            
        } catch (Exception e) {
            log.error("Error getting {} code from MDMS for {}: {}", masterType, searchName, e.getMessage());
            return null;
        }
    }

    /**
     * Extract code from MDMS response
     */
    private String extractCodeFromMDMSResponse(Object mdmsResponse, String masterType, String searchName) {
        if (mdmsResponse instanceof LinkedHashMap) {
            LinkedHashMap<String, Object> responseMap = (LinkedHashMap<String, Object>) mdmsResponse;
            LinkedHashMap<String, Object> mdmsRes = (LinkedHashMap<String, Object>) responseMap.get("MdmsRes");
            
            if (mdmsRes != null) {
                LinkedHashMap<String, Object> commonMasters = (LinkedHashMap<String, Object>) mdmsRes.get("common-masters");
                
                if (commonMasters != null) {
                    // Handle different master types with their specific schema codes
                    String schemaKey = getSchemaKeyForMasterType(masterType);
                    List<LinkedHashMap<String, Object>> masterList = (List<LinkedHashMap<String, Object>>) commonMasters.get(schemaKey);
                    
                    if (masterList != null) {
                        // Find matching item by name
                        for (LinkedHashMap<String, Object> item : masterList) {
                            String name = (String) item.get("name");
                            Boolean active = (Boolean) item.get("active");
                            
                            if (searchName.equalsIgnoreCase(name) && Boolean.TRUE.equals(active)) {
                                String code = (String) item.get("code");
                                if (code != null && !code.trim().isEmpty()) {
                                    return code;
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return null;
    }

    /**
     * Get the correct schema key for different master types
     */
    private String getSchemaKeyForMasterType(String masterType) {
        switch (masterType) {
            case "State":
                return "StateInfo";
            case "ProjectType":
                return "ProjectType";
            // Add more mappings as needed
            default:
                return masterType;
        }
    }

    /**
     * Fallback method using simple mapping
     */
    private String getStateCodeFromFallback(Project project) {
        if (project.getAddress() != null && project.getAddress().getBoundary() != null) {
            String boundary = project.getAddress().getBoundary();
            String[] boundaryParts = boundary.split("_");
            if (boundaryParts.length >= 2) {
                String stateName = boundaryParts[1]; // Second part is state
                // Simple fallback: take first 2 characters of state name
                return stateName.toUpperCase().substring(0, Math.min(2, stateName.length()));
            }
        }
        
        // Fallback to tenant ID based mapping
        String tenantId = project.getTenantId();
        if (tenantId != null && tenantId.contains(".")) {
            String state = tenantId.split("\\.")[0];
            // Simple fallback: take first 2 characters of state name
            return state.toUpperCase().substring(0, Math.min(2, state.length()));
        }
        
        return "XX"; // Default fallback
    }

    /**
     * Gets the duration string from start and end dates
     * Format: YYYY-YY (e.g., 2023-25)
     */
    private String getDuration(Project project) {
        if (project.getStartDate() == null || project.getEndDate() == null) {
            throw new RuntimeException("Start date and end date are required for project name generation");
        }

        LocalDateTime startDate = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(project.getStartDate()), 
            ZoneId.systemDefault()
        );
        
        LocalDateTime endDate = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(project.getEndDate()), 
            ZoneId.systemDefault()
        );

        // Validate that start date is not greater than end date
        if (startDate.isAfter(endDate)) {
            throw new RuntimeException("Start date cannot be greater than end date. Start: " + startDate + ", End: " + endDate);
        }

        // Validate minimum project duration (at least 1 day)
        if (startDate.toLocalDate().equals(endDate.toLocalDate())) {
            throw new RuntimeException("Project must have a duration of at least 1 day. Start and end dates cannot be the same");
        }

        int startYear = startDate.getYear();
        int endYear = endDate.getYear();
        
        // Format as YYYY-YY
        return String.format("%d-%02d", startYear, endYear % 100);
    }

    /**
     * Generates a unique name by checking for duplicates and appending suffixes
     */
    private String generateUniqueName(String baseName, String tenantId) {
        // First check if base name exists
        if (!isProjectNameExists(baseName, tenantId)) {
            return baseName;
        }
        
        // If base name exists, find the highest suffix and increment
        String highestExistingName = findHighestExistingName(baseName, tenantId);
        int nextSuffix = extractAndIncrementSuffix(highestExistingName, baseName);
        
        // Validate that the next suffix is reasonable (prevent infinite loops)
        if (nextSuffix > 1000) {
            log.error("Generated suffix {} is too high for base name: {}. This might indicate a problem.", nextSuffix, baseName);
            throw new RuntimeException("Cannot generate unique project name. Too many duplicates exist for base: " + baseName);
        }
        
        String uniqueName = baseName + "-" + nextSuffix;
        log.info("Generated unique project name: {} (base: {}, suffix: {})", uniqueName, baseName, nextSuffix);
        
        return uniqueName;
    }

    /**
     * Finds the highest existing name with the given base name pattern
     */
    private String findHighestExistingName(String baseName, String tenantId) {
        return projectRepository.findHighestExistingProjectName(baseName, tenantId);
    }

    /**
     * Extracts the suffix from existing name and increments it
     */
    private int extractAndIncrementSuffix(String existingName, String baseName) {
        if (existingName == null || !existingName.startsWith(baseName)) {
            return 1;
        }
        
        // If it's exactly the base name (no suffix), return 1
        if (existingName.equals(baseName)) {
            return 1;
        }
        
        try {
            // Extract the part after base name
            String suffixPart = existingName.substring(baseName.length());
            
            // Remove leading dash if present
            if (suffixPart.startsWith("-")) {
                suffixPart = suffixPart.substring(1);
            }
            
            // Parse the suffix number
            int currentSuffix = Integer.parseInt(suffixPart);
            return currentSuffix + 1;
            
        } catch (NumberFormatException e) {
            log.warn("Could not parse suffix from existing name: {}", existingName);
            return 1;
        }
    }

    /**
     * Checks if a project name already exists in the system
     */
    private boolean isProjectNameExists(String projectName, String tenantId) {
        return projectRepository.isProjectNameExists(projectName, tenantId);
    }

    /**
     * Generates project name and checks for duplicates
     * Returns a result object with the generated name and duplicate status
     */
    public ProjectNameResult generateNameAndCheckDuplicate(Project project, RequestInfo requestInfo) {
        try {
            // Check if project type is FieldPlan or Facility - skip name generation
            if (shouldSkipNameGeneration(project)) {
                log.info("Skipping project name generation for project type: {}", project.getProjectType());
                return ProjectNameResult.builder()
                    .name(null)
                    .isDuplicateName(false)
                    .build();
            }
            
            // Generate the base name
            String projectCode = getProjectCode(project, requestInfo);
            String stateCode = getStateCode(project, requestInfo);
            String duration = getDuration(project);
            String baseName = String.format("%s-%s-%s", projectCode, stateCode, duration);
            
            // Check if base name exists
            boolean isDuplicate = isProjectNameExists(baseName, project.getTenantId());
            
            if (isDuplicate) {
                // Generate unique name with suffix
                String uniqueName = generateUniqueName(baseName, project.getTenantId());
                return ProjectNameResult.builder()
                    .name(uniqueName)
                    .isDuplicateName(true)
                    .build();
            } else {
                // Use base name as it's unique
                return ProjectNameResult.builder()
                    .name(baseName)
                    .isDuplicateName(false)
                    .build();
            }
            
        } catch (Exception e) {
            log.error("Error generating project name for project: {}", project.getId(), e);
            throw new RuntimeException("Failed to generate project name", e);
        }
    }
}
