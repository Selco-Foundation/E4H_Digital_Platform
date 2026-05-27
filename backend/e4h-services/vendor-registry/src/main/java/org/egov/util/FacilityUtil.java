package org.egov.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;
import org.egov.config.Configuration;
import org.egov.repository.ServiceRequestRepository;
import org.egov.web.models.Facility;
import org.egov.web.models.FacilitySearchResponse;
import org.egov.web.models.FacilityUpdatePayload;
import org.egov.web.models.FacilityUpdateRequest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class FacilityUtil {

    private static final String FACILITY_BOUNDARY_TYPE = "Facility";
    private static final String EMPLOYEE_ROLE_CODE = "EMPLOYEE";
    private static final String SYSTEM_USER_ROLE_CODE = "SYSTEM_USER";
    private static final String MAPPED_VENDOR_NAME_KEY = "mappedVendorName";
    private static final String MAPPED_VENDOR_USER_NAME_KEY = "mappedVendorUserName";

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

    private final RestTemplate restTemplate;
    private final Configuration configuration;
    private final ServiceRequestRepository serviceRequestRepository;

    public FacilityUtil(RestTemplate restTemplate, Configuration configuration,
                        ServiceRequestRepository serviceRequestRepository) {
        this.restTemplate = restTemplate;
        this.configuration = configuration;
        this.serviceRequestRepository = serviceRequestRepository;
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
                Facility facility = searchFacilityByBoundaryCode(requestInfo, effectiveTenantId, boundaryCode);
                if (facility == null) {
                    log.warn("No facility found for boundaryCode: {}", boundaryCode);
                    continue;
                }
                updateFacilityMappedVendor(facility, vendorName, vendorUserName);
            } catch (Exception e) {
                log.error("Failed to sync mapped vendor for boundaryCode: {}", boundaryCode, e);
            }
        }
    }

    public Facility searchFacilityByBoundaryCode(RequestInfo requestInfo, String tenantId, String boundaryCode) {
        if (StringUtils.isBlank(boundaryCode)) {
            return null;
        }
        String url = UriComponentsBuilder
                .fromHttpUrl(configuration.getFacilityHost() + configuration.getFacilitySearchPath())
                .queryParam("tenantId", tenantId)
                .queryParam("boundaryCode", boundaryCode.trim())
                .queryParam("limit", 1)
                .queryParam("offset", 0)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        if (requestInfo != null && StringUtils.isNotBlank(requestInfo.getAuthToken())) {
            headers.set("auth-token", requestInfo.getAuthToken());
        }
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<FacilitySearchResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        FacilitySearchResponse body = response.getBody();
        if (body == null || body.getFacilities() == null || body.getFacilities().isEmpty()) {
            return null;
        }
        return body.getFacilities().get(0);
    }

    public void updateFacilityMappedVendor(Facility existingFacility, String vendorName, String vendorUserName) {
        if (existingFacility == null || StringUtils.isBlank(existingFacility.getFacilityId())) {
            return;
        }

        String facilityTenantId = StringUtils.isNotBlank(existingFacility.getTenantId())
                ? existingFacility.getTenantId()
                : configuration.getGlobalTenantId();
        RequestInfo systemRequestInfo = buildFacilityServiceSystemRequestInfo(facilityTenantId);

        Map<String, Object> additionalDetails = new HashMap<>();
        if (existingFacility.getAdditionalDetails() != null) {
            additionalDetails.putAll(existingFacility.getAdditionalDetails());
        }
        additionalDetails.put(MAPPED_VENDOR_NAME_KEY, vendorName);
        if (StringUtils.isNotBlank(vendorUserName)) {
            additionalDetails.put(MAPPED_VENDOR_USER_NAME_KEY, vendorUserName);
        }

        FacilityUpdatePayload payload = FacilityUpdatePayload.builder()
                .tenantId(existingFacility.getTenantId())
                .facilityId(existingFacility.getFacilityId())
                .facilityCategory(existingFacility.getFacilityCategory())
                .facilityType(existingFacility.getFacilityType())
                .facilitySubtype(existingFacility.getFacilitySubtype())
                .facilityName(existingFacility.getFacilityName())
                .boundaryCode(existingFacility.getBoundaryCode())
                .address(existingFacility.getAddress())
                .isOnmReady(existingFacility.getIsOnmReady())
                .mappedVendorName(vendorName)
                .mappedVendorUserName(vendorUserName)
                .additionalDetails(additionalDetails)
                .build();

        FacilityUpdateRequest updateRequest = FacilityUpdateRequest.builder()
                .requestInfo(systemRequestInfo)
                .facilityUpdate(payload)
                .build();

        String url = configuration.getFacilityHost() + configuration.getFacilityUpdatePath();
        serviceRequestRepository.fetchResult(new StringBuilder(url), updateRequest);
        log.info("Updated mapped vendor on facility {} for boundaryCode {}",
                existingFacility.getFacilityId(), existingFacility.getBoundaryCode());
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
