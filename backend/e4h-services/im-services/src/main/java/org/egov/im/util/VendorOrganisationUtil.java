package org.egov.im.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.im.config.IMConfiguration;
import org.egov.im.repository.ServiceRequestRepository;
import org.egov.im.web.models.vendor.OrgUser;
import org.egov.im.web.models.vendor.OrgUserSearchCriteria;
import org.egov.im.web.models.vendor.OrgUserSearchRequest;
import org.egov.im.web.models.vendor.OrgUserSearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Resolves which vendor organisation a user belongs to, from vendor-registry.
 * <p>
 * Used to tell apart "reassigned to the same vendor" from "reassigned to a new vendor": the clients
 * only ever send a user uuid, and two different users can belong to the same vendor.
 */
@Component
@Slf4j
public class VendorOrganisationUtil {

    private final IMConfiguration imConfiguration;
    private final ServiceRequestRepository serviceRequestRepository;
    private final ObjectMapper mapper;

    @Autowired
    public VendorOrganisationUtil(IMConfiguration imConfiguration,
                                  ServiceRequestRepository serviceRequestRepository,
                                  ObjectMapper mapper) {
        this.imConfiguration = imConfiguration;
        this.serviceRequestRepository = serviceRequestRepository;
        this.mapper = mapper;
    }

    /**
     * user uuid -> organisation id, for the users that belong to one. Users with no organisation, and
     * memberships flagged deleted, are simply absent from the map.
     * <p>
     * Never throws: callers use this to enrich a decision they must take either way, so a
     * vendor-registry outage must not fail the operation under way. It returns an empty map instead,
     * which callers are expected to treat as "cannot tell".
     */
    public Map<String, String> getOrganisationIdsByUserUuids(Collection<String> userUuids, String tenantId,
                                                             RequestInfo requestInfo) {
        log.trace("VendorOrganisationUtil::getOrganisationIdsByUserUuids method invoked");
        if (CollectionUtils.isEmpty(userUuids)) {
            return Collections.emptyMap();
        }

        List<String> uuids = userUuids.stream()
                .filter(Objects::nonNull)
                .filter(uuid -> !uuid.isBlank())
                .distinct()
                .toList();
        if (uuids.isEmpty()) {
            return Collections.emptyMap();
        }

        StringBuilder url = new StringBuilder(imConfiguration.getVendorHost())
                .append(imConfiguration.getVendorOrganisationUserSearchPath())
                .append("?tenantId=").append(tenantId)
                .append("&offset=0&limit=").append(uuids.size());

        OrgUserSearchRequest searchRequest = OrgUserSearchRequest.builder()
                .requestInfo(requestInfo)
                .criteria(OrgUserSearchCriteria.builder()
                        .userId(uuids)
                        .tenantId(tenantId)
                        .build())
                .build();

        try {
            Object response = serviceRequestRepository.fetchResult(url, searchRequest);
            OrgUserSearchResponse searchResponse = mapper.convertValue(response, OrgUserSearchResponse.class);
            if (searchResponse == null || CollectionUtils.isEmpty(searchResponse.getOrgUsers())) {
                log.warn("No vendor organisation found for userUuids: {} in tenant: {}", uuids, tenantId);
                return Collections.emptyMap();
            }

            Map<String, String> organisationIdsByUserUuid = new LinkedHashMap<>();
            for (OrgUser orgUser : searchResponse.getOrgUsers()) {
                if (orgUser == null || Boolean.TRUE.equals(orgUser.getIsDeleted())
                        || orgUser.getUserId() == null || orgUser.getOrganizationId() == null) {
                    continue;
                }
                // A user is expected to belong to one organisation; should vendor-registry ever return
                // several, the first one wins rather than an arbitrary later one.
                organisationIdsByUserUuid.putIfAbsent(orgUser.getUserId(), orgUser.getOrganizationId());
            }
            log.debug("Resolved {} of {} user(s) to a vendor organisation", organisationIdsByUserUuid.size(), uuids.size());
            return organisationIdsByUserUuid;
        } catch (Exception e) {
            log.error("Error while fetching vendor organisations for userUuids: {} in tenant: {}", uuids, tenantId, e);
            return Collections.emptyMap();
        }
    }
}
