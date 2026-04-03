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
import org.egov.tracer.model.CustomException;
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
        log.trace("Entering shouldSkipNameGeneration for project: {}", project.getId());
        String projectType = project.getProjectType();
        return "FieldPlan".equals(projectType) || "Facility".equals(projectType);
    }

    /**
     * Gets the project code from project details or uses default
     */
    private String getProjectCode(Project project, RequestInfo requestInfo) {
        log.trace("Entering getProjectCode for project: {}", project.getId());
        try {
            // Try to get project code from MDMS based on project type
            if (project.getProjectType() != null && requestInfo != null) {
                String projectTypeName = project.getProjectType();
                String tenantId = project.getTenantId() != null ? project.getTenantId() : "in";
                
                log.info("Fetching project type code for project type: {} from tenant: {}", projectTypeName, tenantId);
                log.debug("Calling MDMS to get project type code");
                
                String projectCode = getCodeFromMDMS(project, requestInfo, tenantId, "ProjectType", projectTypeName);
                if (projectCode != null) {
                    log.info("Found project type code: {} for project type: {}", projectCode, projectTypeName);
                    log.trace("Exiting getProjectCode");
                    return projectCode;
                }
                
                log.warn("Project type code not found in MDMS for: {}, using default: {}", 
                        projectTypeName, projectConfiguration.getProjectNameDefaultCode());
            }
            
            // Fallback to configured default
            log.info("Using default project code: {}", projectConfiguration.getProjectNameDefaultCode());
            log.trace("Exiting getProjectCode");
            return projectConfiguration.getProjectNameDefaultCode();
            
        } catch (Exception e) {
            log.error("Error getting project code for project: {}, using default", project.getId(), e);
            log.trace("Exiting getProjectCode");
            return projectConfiguration.getProjectNameDefaultCode();
        }
    }

    /**
     * Helper method to extract state name from boundary string
     * Handles both formats: "India_<StateName>" and direct "<StateName>"
     */
    private String extractStateNameFromBoundary(String boundary) {
        log.trace("Entering extractStateNameFromBoundary with boundary: {}", boundary);
        if (boundary == null || boundary.trim().isEmpty()) {
            log.debug("Boundary is null or empty");
            log.trace("Exiting extractStateNameFromBoundary");
            return null;
        }
        
        String[] boundaryParts = boundary.split("_");
        String stateName = null;
        
        if (boundaryParts.length >= 2 && "India".equalsIgnoreCase(boundaryParts[0])) {
            stateName = boundaryParts[1];
        } else if (boundaryParts.length >= 1) {
            stateName = boundaryParts[0];
        }
        log.debug("Extracted state name: {} from boundary", stateName);
        
        // Validate state name is not placeholder/invalid
        if (stateName != null && !stateName.equalsIgnoreCase("nan") && 
            !stateName.equalsIgnoreCase("XYZ") && stateName.trim().length() > 0) {
            log.trace("Exiting extractStateNameFromBoundary");
            return stateName.trim();
        }
        
        log.warn("Invalid state name found in boundary: {}, returning null", stateName);
        log.trace("Exiting extractStateNameFromBoundary");
        return null;
    }

    /**
     * Gets the state code from project boundary or MDMS
     */
    private String getStateCode(Project project, RequestInfo requestInfo) {
        log.trace("Entering getStateCode for project: {}", project.getId());
        try {
            // First try to get state code from MDMS based on boundary
            if (project.getAddress() != null && project.getAddress().getBoundary() != null) {
                log.debug("Attempting to get state code from boundary");
                String boundary = project.getAddress().getBoundary();
                String stateName = extractStateNameFromBoundary(boundary);
                
                if (stateName != null) {
                    log.debug("Extracted state name: {}, fetching code from MDMS", stateName);
                    String stateCode = getCodeFromMDMS(project, requestInfo, project.getTenantId(), "State", stateName);
                    if (stateCode != null) {
                        log.info("Found state code from MDMS: {} for state: {}", stateCode, stateName);
                        log.trace("Exiting getStateCode");
                        return stateCode;
                    }
                }
            }
            
            // Fallback to tenant ID based mapping
            String tenantId = project.getTenantId();
            if (tenantId != null && tenantId.contains(".")) {
                log.debug("Attempting to get state code from tenant ID");
                String state = tenantId.split("\\.")[0];
                String stateCode = getCodeFromMDMS(project, requestInfo, tenantId, "State", state);
                if (stateCode != null) {
                    log.info("Found state code from MDMS using tenant: {} for state: {}", stateCode, state);
                    log.trace("Exiting getStateCode");
                    return stateCode;
                }
            }
            
            log.warn("State code not found in MDMS, using fallback mapping");
            String fallbackCode = getStateCodeFromFallback(project);
            log.trace("Exiting getStateCode");
            return fallbackCode;
            
        } catch (Exception e) {
            log.error("Error getting state code for project: {}, using fallback", project.getId(), e);
            String fallbackCode = getStateCodeFromFallback(project);
            log.trace("Exiting getStateCode");
            return fallbackCode;
        }
    }

    /**
     * Generic method to get code from MDMS
     */
    private String getCodeFromMDMS(Project project, RequestInfo requestInfo, String tenantId, String masterType, String searchName) {
        log.trace("Entering getCodeFromMDMS for masterType: {}, searchName: {}", masterType, searchName);
        try {
            String rootTenantId = tenantId.split("\\.")[0];
            log.debug("Calling MDMS for masterType: {}, searchName: {}, rootTenantId: {}", masterType, searchName, rootTenantId);
            
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
            log.debug("Received MDMS response, extracting code");
            
            return extractCodeFromMDMSResponse(mdmsResponse, masterType, searchName);
            
        } catch (Exception e) {
            log.error("Error getting {} code from MDMS for {}: {}", masterType, searchName, e.getMessage(), e);
            log.trace("Exiting getCodeFromMDMS");
            return null;
        }
    }

    /**
     * Extract code from MDMS response
     */
    private String extractCodeFromMDMSResponse(Object mdmsResponse, String masterType, String searchName) {
        log.trace("Entering extractCodeFromMDMSResponse for masterType: {}, searchName: {}", masterType, searchName);
        if (mdmsResponse instanceof LinkedHashMap) {
            log.debug("Processing MDMS response as LinkedHashMap");
            LinkedHashMap<String, Object> responseMap = (LinkedHashMap<String, Object>) mdmsResponse;
            LinkedHashMap<String, Object> mdmsRes = (LinkedHashMap<String, Object>) responseMap.get("MdmsRes");
            
            if (mdmsRes != null) {
                LinkedHashMap<String, Object> commonMasters = (LinkedHashMap<String, Object>) mdmsRes.get("common-masters");
                
                if (commonMasters != null) {
                    // Handle different master types with their specific schema codes
                    String schemaKey = getSchemaKeyForMasterType(masterType);
                    log.debug("Using schema key: {} for masterType: {}", schemaKey, masterType);
                    List<LinkedHashMap<String, Object>> masterList = (List<LinkedHashMap<String, Object>>) commonMasters.get(schemaKey);
                    
                    if (masterList != null) {
                        log.debug("Found {} items in master list", masterList.size());
                        // Find matching item by name
                        for (LinkedHashMap<String, Object> item : masterList) {
                            String name = (String) item.get("name");
                            Boolean active = (Boolean) item.get("active");
                            
                            if (searchName.equalsIgnoreCase(name) && Boolean.TRUE.equals(active)) {
                                String code = (String) item.get("code");
                                if (code != null && !code.trim().isEmpty()) {
                                    log.debug("Found matching code: {} for name: {}", code, name);
                                    log.trace("Exiting extractCodeFromMDMSResponse");
                                    return code;
                                }
                            }
                        }
                        log.debug("No matching active item found for searchName: {}", searchName);
                    } else {
                        log.debug("Master list is null for schema key: {}", schemaKey);
                    }
                } else {
                    log.debug("Common masters is null in MDMS response");
                }
            } else {
                log.debug("MdmsRes is null in response");
            }
        } else {
            log.debug("MDMS response is not a LinkedHashMap");
        }
        
        log.trace("Exiting extractCodeFromMDMSResponse - no code found");
        return null;
    }

    /**
     * Get the correct schema key for different master types
     */
    private String getSchemaKeyForMasterType(String masterType) {
        log.trace("Entering getSchemaKeyForMasterType for masterType: {}", masterType);
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
        log.trace("Entering getStateCodeFromFallback for project: {}", project.getId());
        log.debug("Using fallback method to get state code");
        if (project.getAddress() != null && project.getAddress().getBoundary() != null) {
            String boundary = project.getAddress().getBoundary();
            String stateName = extractStateNameFromBoundary(boundary);
            
            if (stateName != null) {
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
        
        log.warn("Using default fallback state code: XX");
        log.trace("Exiting getStateCodeFromFallback");
        return "XX"; // Default fallback
    }

    /**
     * Gets the duration string from start and end dates
     * Format: YYYY-YY (e.g., 2023-25)
     */
    private String getDuration(Project project) {
        log.trace("Entering getDuration for project: {}", project.getId());
        if (project.getStartDate() == null || project.getEndDate() == null) {
            log.error("Start date or end date is null for project: {}", project.getId());
            throw new CustomException("INVALID_PROJECT_DATES", "Start date and end date are required for project name generation");
        }

        LocalDateTime startDate = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(project.getStartDate()), 
            ZoneId.systemDefault()
        );
        
        LocalDateTime endDate = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(project.getEndDate()), 
            ZoneId.systemDefault()
        );
        log.debug("Parsed dates - start: {}, end: {}", startDate, endDate);

        // Validate that start date is not greater than end date
        if (startDate.isAfter(endDate)) {
            log.error("Start date is after end date for project: {}. Start: {}, End: {}", project.getId(), startDate, endDate);
            throw new CustomException("INVALID_PROJECT_DATES", "Start date cannot be greater than end date. Start: " + startDate + ", End: " + endDate);
        }

        // Validate minimum project duration (at least 1 day)
        if (startDate.toLocalDate().equals(endDate.toLocalDate())) {
            log.error("Start and end dates are the same for project: {}", project.getId());
            throw new CustomException("INVALID_PROJECT_DURATION", "Project must have a duration of at least 1 day. Start and end dates cannot be the same");
        }

        int startYear = startDate.getYear();
        int endYear = endDate.getYear();
        log.debug("Extracted years - start: {}, end: {}", startYear, endYear);
        
        // Format as YYYY-YY
        return String.format("%d-%02d", startYear, endYear % 100);
    }

    /**
     * Generates a unique name by checking for duplicates and appending suffixes
     */
    private String generateUniqueName(String baseName, String tenantId) {
        log.trace("Entering generateUniqueName for baseName: {}, tenantId: {}", baseName, tenantId);
        // First check if base name exists
        log.debug("Checking if base name exists: {}", baseName);
        if (!isProjectNameExists(baseName, tenantId)) {
            log.debug("Base name is unique, returning as-is");
            log.trace("Exiting generateUniqueName");
            return baseName;
        }
        
        // If base name exists, find the highest suffix and increment
        log.debug("Base name exists, finding highest existing name");
        String highestExistingName = findHighestExistingName(baseName, tenantId);
        log.debug("Highest existing name: {}", highestExistingName);
        int nextSuffix = extractAndIncrementSuffix(highestExistingName, baseName);
        log.debug("Calculated next suffix: {}", nextSuffix);
        
        // Validate that the next suffix is reasonable (prevent infinite loops)
        if (nextSuffix > 1000) {
            log.error("Generated suffix {} is too high for base name: {}. This might indicate a problem.", nextSuffix, baseName);
            throw new CustomException("PROJECT_NAME_GENERATION_FAILED", "Cannot generate unique project name. Too many duplicates exist for base: " + baseName);
        }
        
        String uniqueName = baseName + "-" + nextSuffix;
        log.info("Generated unique project name: {} (base: {}, suffix: {})", uniqueName, baseName, nextSuffix);
        log.trace("Exiting generateUniqueName");
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
        log.trace("Entering extractAndIncrementSuffix for existingName: {}, baseName: {}", existingName, baseName);
        if (existingName == null || !existingName.startsWith(baseName)) {
            log.debug("Existing name is null or doesn't start with base name, returning suffix 1");
            log.trace("Exiting extractAndIncrementSuffix");
            return 1;
        }
        
        // If it's exactly the base name (no suffix), return 1
        if (existingName.equals(baseName)) {
            log.debug("Existing name equals base name, returning suffix 1");
            log.trace("Exiting extractAndIncrementSuffix");
            return 1;
        }
        
        try {
            // Extract the part after base name
            String suffixPart = existingName.substring(baseName.length());
            log.debug("Extracted suffix part: {}", suffixPart);
            
            // Remove leading dash if present
            if (suffixPart.startsWith("-")) {
                suffixPart = suffixPart.substring(1);
            }
            
            // Parse the suffix number
            int currentSuffix = Integer.parseInt(suffixPart);
            return currentSuffix + 1;
            
        } catch (NumberFormatException e) {
            log.warn("Could not parse suffix from existing name: {}", existingName);
            log.trace("Exiting extractAndIncrementSuffix");
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
     * Generates project name and checks for duplicates (for creation)
     * Returns a result object with the generated name and duplicate status
     */
    public ProjectNameResult generateNameAndCheckDuplicate(Project project, RequestInfo requestInfo) {
        return generateNameAndCheckDuplicate(project, requestInfo, null);
    }

    /**
     * Generates project name and checks for duplicates
     * Returns a result object with the generated name and duplicate status
     * @param project The project to generate name for
     * @param requestInfo Request information
     * @param excludeProjectId Project ID to exclude from duplicate check (for updates)
     */
    public ProjectNameResult generateNameAndCheckDuplicate(Project project, RequestInfo requestInfo, String excludeProjectId) {
        log.trace("Entering generateNameAndCheckDuplicate for project: {}, excludeProjectId: {}", project.getId(), excludeProjectId);
        log.info("Generating project name for project: {}", project.getId());
        try {
            // Check if project type is FieldPlan or Facility - skip name generation
            if (shouldSkipNameGeneration(project)) {
                log.info("Skipping project name generation for project type: {}", project.getProjectType());
                log.trace("Exiting generateNameAndCheckDuplicate");
                return ProjectNameResult.builder()
                    .name(null)
                    .isDuplicateName(false)
                    .build();
            }
            
            // Generate the base name
            log.debug("Fetching project code, state code, and duration");
            String projectCode = getProjectCode(project, requestInfo);
            String stateCode = getStateCode(project, requestInfo);
            String duration = getDuration(project);
            String baseName = String.format("%s-%s-%s", projectCode, stateCode, duration);
            log.debug("Generated base name: {}", baseName);
            
            // Check if base name exists (with optional exclusion for updates)
            boolean isDuplicate;
            if (excludeProjectId != null) {
                // For updates: exclude current project from duplicate check
                log.debug("Checking for duplicate name excluding project: {}", excludeProjectId);
                isDuplicate = projectRepository.isProjectNameExistsExcludingProject(baseName, project.getTenantId(), excludeProjectId);
            } else {
                // For creation: check all projects
                log.debug("Checking for duplicate name across all projects");
                isDuplicate = isProjectNameExists(baseName, project.getTenantId());
            }
            log.debug("Duplicate check result: {}", isDuplicate);
            
            if (isDuplicate) {
                // Generate unique name with suffix
                log.info("Base name is duplicate, generating unique name with suffix");
                String uniqueName = generateUniqueName(baseName, project.getTenantId());
                log.info("Generated unique project name: {}", uniqueName);
                log.trace("Exiting generateNameAndCheckDuplicate");
                return ProjectNameResult.builder()
                    .name(uniqueName)
                    .isDuplicateName(true)
                    .build();
            } else {
                // Use base name as it's unique
                log.info("Base name is unique: {}", baseName);
                log.trace("Exiting generateNameAndCheckDuplicate");
                return ProjectNameResult.builder()
                    .name(baseName)
                    .isDuplicateName(false)
                    .build();
            }
            
        } catch (Exception e) {
            log.error("Error generating project name for project: {}", project.getId(), e);
            throw new CustomException("PROJECT_NAME_GENERATION_FAILED", "Failed to generate project name: " + e);
        }
    }
}
