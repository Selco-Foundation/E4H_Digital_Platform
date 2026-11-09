package org.egov.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;
import org.egov.config.Configuration;
import org.egov.repository.ServiceRequestRepository;
import org.egov.web.models.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Slf4j
public class FacilityUtil {

    private static final String FACILITY_BOUNDARY_TYPE = "Facility";
    private static final String EMPLOYEE_ROLE_CODE = "EMPLOYEE";
    private static final String SYSTEM_USER_ROLE_CODE = "SYSTEM_USER";
    // System RequestInfo for facility-service update (docs/cron/run_visit_scheduling.py)
    private static final String INTERNAL_REQUEST_API_ID = "Rainmaker";
    private static final String INTERNAL_REQUEST_AUTH_TOKEN = "cronjob-token";
    private static final String INTERNAL_REQUEST_DID = "vendor-registry-facility-sync";
    private static final String INTERNAL_REQUEST_KEY = "cronjob-key";
    private static final String INTERNAL_SYSTEM_USER_UUID = "c8ed7e51-c0e5-4552-a420-76eeeee1e1dc";
    private static final String INTERNAL_SYSTEM_USER_NAME = "CRONJOB_VISIT_SCHEDULING";
    private static final String INTERNAL_SYSTEM_USER_DISPLAY_NAME = "Cron Job - Visit Scheduling";
    private static final String INTERNAL_SYSTEM_USER_MOBILE = "0000000000";
    private static final String INTERNAL_SYSTEM_USER_EMAIL = "cronjob@e4h.com";
    private static final String INTERNAL_SYSTEM_USER_TYPE = "SYSTEM";

    private final Configuration configuration;
    private final ServiceRequestRepository serviceRequestRepository;

    public FacilityUtil(Configuration configuration,
                        ServiceRequestRepository serviceRequestRepository) {
        this.configuration = configuration;
        this.serviceRequestRepository = serviceRequestRepository;
    }

    @Async("mappedVendorExecutor")
    public void syncMappedVendorToFacilities(OrgUserRequest request,
                                             List<Jurisdiction> jurisdictions,
                                             String vendorName,
                                             String vendorUserName) {
        List<String> facilityBoundaryCodes = extractActiveFacilityBoundaryCodes(jurisdictions);
        if (facilityBoundaryCodes.isEmpty()) {
            log.debug("No facility jurisdictions in request; skipping mapped-vendor sync");
            return;
        }
        String tenantId = configuration.getGlobalTenantId();
        if (request.getUser() != null && StringUtils.isNotBlank(request.getUser().getTenantId())) {
            tenantId = request.getUser().getTenantId();
        }
        log.info("Syncing mapped vendor name='{}' userName='{}' to {} facility boundaries",
                vendorName, vendorUserName, facilityBoundaryCodes.size());
        updateMappedVendorForFacilityBoundaries(
                request.getRequestInfo(),
                tenantId,
                facilityBoundaryCodes,
                vendorName,
                vendorUserName
        );
    }

    @Async("mappedVendorExecutor")
    public void clearMappedVendorForRemovedFacilityJurisdictions(OrgUserRequest request,
                                                                 List<Jurisdiction> previousJurisdictions,
                                                                 List<Jurisdiction> currentJurisdictions) {
        List<String> removedFacilityBoundaryCodes =
                extractRemovedActiveFacilityBoundaryCodes(previousJurisdictions, currentJurisdictions);
        if (removedFacilityBoundaryCodes.isEmpty()) {
            log.debug("No removed facility jurisdictions; skipping mapped-vendor clear");
            return;
        }
        String tenantId = configuration.getGlobalTenantId();
        if (request.getUser() != null && StringUtils.isNotBlank(request.getUser().getTenantId())) {
            tenantId = request.getUser().getTenantId();
        }
        log.info("Clearing mapped vendor on {} removed facility boundaries", removedFacilityBoundaryCodes.size());
        clearMappedVendorForFacilityBoundaries(
                request.getRequestInfo(),
                tenantId,
                removedFacilityBoundaryCodes
        );
    }

    private List<String> extractActiveFacilityBoundaryCodes(List<Jurisdiction> jurisdictions) {
        if (jurisdictions == null || jurisdictions.isEmpty()) {
            return List.of();
        }
        return jurisdictions.stream()
                .filter(j -> j != null && FacilityUtil.isFacilityBoundaryType(j.getBoundaryType()))
                .filter(j -> j.getIsActive() == null || Boolean.TRUE.equals(j.getIsActive()))
                .map(Jurisdiction::getBoundary)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<String> extractRemovedActiveFacilityBoundaryCodes(List<Jurisdiction> previousJurisdictions,
                                                                   List<Jurisdiction> currentJurisdictions) {
        Set<String> currentActiveBoundaries = extractActiveFacilityBoundaryCodes(currentJurisdictions).stream()
                .map(String::toLowerCase)
                .collect(Collectors.toCollection(HashSet::new));
        return extractActiveFacilityBoundaryCodes(previousJurisdictions).stream()
                .filter(boundary -> !currentActiveBoundaries.contains(boundary.toLowerCase()))
                .distinct()
                .collect(Collectors.toList());
    }

    public void clearMappedVendorForFacilityBoundaries(RequestInfo requestInfo, String tenantId,
                                                       List<String> boundaryCodes) {
        if (boundaryCodes == null || boundaryCodes.isEmpty()) {
            return;
        }
        String effectiveTenantId = StringUtils.isNotBlank(tenantId) ? tenantId : configuration.getGlobalTenantId();
        for (String boundaryCode : boundaryCodes) {
            if (StringUtils.isBlank(boundaryCode)) {
                continue;
            }
            try {
                updateFacilityMappedVendor(effectiveTenantId, boundaryCode, null, null);
            } catch (Exception e) {
                log.error("Failed to clear mapped vendor for boundaryCode: {}", boundaryCode, e);
            }
        }
    }

    public void updateMappedVendorForFacilityBoundaries(RequestInfo requestInfo, String tenantId,
                                                        List<String> boundaryCodes, String vendorName,
                                                        String vendorUserName) {
        if (boundaryCodes == null || boundaryCodes.isEmpty()) {
            return;
        }
        if (StringUtils.isBlank(vendorName)) {
            log.warn("Skipping facility mapped-vendor sync: vendor name is blank");
            return;
        }
        String effectiveTenantId = StringUtils.isNotBlank(tenantId) ? tenantId : configuration.getGlobalTenantId();
        for (String boundaryCode : boundaryCodes) {
            if (StringUtils.isBlank(boundaryCode)) {
                continue;
            }
            try {
                updateFacilityMappedVendor(effectiveTenantId, boundaryCode, vendorName, vendorUserName);
            } catch (Exception e) {
                log.error("Failed to sync mapped vendor for boundaryCode: {}", boundaryCode, e);
            }
        }
    }

    /**
     * Writes one facility's mapped vendor through facility-service's dedicated endpoint.
     *
     * <p>Addresses the facility by the boundary code this vendor's jurisdiction already names, so
     * no search is needed first. This used to search the facility and then echo its entire record
     * back through {@code /v2/facility/update}, which made a vendor mapping run the ONM-ready
     * branching - POC user creation, password resets, index deletion - and rewrite every field it
     * had just read.
     *
     * <p>A null name and username unmap the facility.
     */
    public void updateFacilityMappedVendor(String tenantId, String boundaryCode,
                                           String vendorName, String vendorUserName) {
        if (StringUtils.isBlank(boundaryCode)) {
            return;
        }
        String effectiveTenantId = StringUtils.isNotBlank(tenantId) ? tenantId : configuration.getGlobalTenantId();

        FacilityMappedVendorUpdateRequest updateRequest = FacilityMappedVendorUpdateRequest.builder()
                .requestInfo(buildFacilityServiceSystemRequestInfo(effectiveTenantId))
                .tenantId(effectiveTenantId)
                .boundaryCode(boundaryCode.trim())
                .mappedVendorName(vendorName)
                .mappedVendorUserName(vendorUserName)
                .build();

        String url = configuration.getFacilityHost() + configuration.getFacilityMappedVendorUpdatePath();
        serviceRequestRepository.fetchResult(new StringBuilder(url), updateRequest);
        log.info("Requested mapped vendor update for boundaryCode {} (userName={})",
                boundaryCode, vendorUserName);
    }

    public static boolean isFacilityBoundaryType(String boundaryType) {
        return FACILITY_BOUNDARY_TYPE.equalsIgnoreCase(StringUtils.trimToEmpty(boundaryType));
    }

    /**
     * Builds a dedicated system RequestInfo for facility-service update calls.
     * Pattern aligned with docs/cron/run_visit_scheduling.py (SYSTEM_USER + EMPLOYEE roles).
     */
    private RequestInfo buildFacilityServiceSystemRequestInfo(String tenantId) {
        String effectiveTenantId = StringUtils.isNotBlank(tenantId) ? tenantId : configuration.getGlobalTenantId();
        List<Role> roles = List.of(
                Role.builder().name("Employee").code(EMPLOYEE_ROLE_CODE).tenantId(effectiveTenantId).build(),
                Role.builder().name("System User").code(SYSTEM_USER_ROLE_CODE).tenantId(effectiveTenantId).build()
        );
        User systemUser = User.builder()
                .uuid(INTERNAL_SYSTEM_USER_UUID)
                .userName(INTERNAL_SYSTEM_USER_NAME)
                .name(INTERNAL_SYSTEM_USER_DISPLAY_NAME)
                .mobileNumber(INTERNAL_SYSTEM_USER_MOBILE)
                .emailId(INTERNAL_SYSTEM_USER_EMAIL)
                .type(INTERNAL_SYSTEM_USER_TYPE)
                .roles(roles)
                .tenantId(effectiveTenantId)
                .build();
        return RequestInfo.builder()
                .apiId(INTERNAL_REQUEST_API_ID)
                .ver("1.0")
                .ts(System.currentTimeMillis())
                .action("_update")
                .did(INTERNAL_REQUEST_DID)
                .key(INTERNAL_REQUEST_KEY)
                .msgId(UUID.randomUUID().toString())
                .authToken(INTERNAL_REQUEST_AUTH_TOKEN)
                .userInfo(systemUser)
                .build();
    }
}
