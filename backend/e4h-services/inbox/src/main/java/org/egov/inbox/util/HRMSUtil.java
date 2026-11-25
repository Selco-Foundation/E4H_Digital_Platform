package org.egov.inbox.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.inbox.web.model.RequestInfoWrapper;
import org.egov.inbox.repository.ServiceRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The caller's own jurisdictions, read from HRMS.
 *
 * The inbox had no HRMS client: the boundaries it filtered on arrived in the request body, put
 * there by the browser from session storage ({@code Jurisdiction.CurrentBoundary}, written at
 * login). Nothing checked them, so any caller could post any boundary set and read the whole
 * country. This is the server-side source of truth that replaces that trust.
 *
 * Read through the **v2** employee API, because v1 and v2 differ in exactly what matters here:
 * the roles a jurisdiction applies to. A jurisdiction with no roles applies to every role the
 * employee holds, which is what every row created before role scoping looks like.
 */
@Slf4j
@Component
public class HRMSUtil {

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${egov.hrms.host}")
    private String hrmsHost;

    @Value("${egov.hrms.search.endpoint}")
    private String hrmsSearchEndpoint;

    /**
     * One jurisdiction grant: a boundary, and the roles it applies to.
     *
     * An empty {@code roleCodes} is not "no roles" - it is "every role the holder has". The two
     * read alike in a list and mean opposite things, so the distinction is kept explicit here
     * rather than left to the caller to remember.
     */
    public static class JurisdictionGrant {
        private final String boundary;
        private final String boundaryType;
        private final Set<String> roleCodes;

        JurisdictionGrant(String boundary, String boundaryType, Set<String> roleCodes) {
            this.boundary = boundary;
            this.boundaryType = boundaryType;
            this.roleCodes = roleCodes;
        }

        public String getBoundary() {
            return boundary;
        }

        public String getBoundaryType() {
            return boundaryType;
        }

        public Set<String> getRoleCodes() {
            return roleCodes;
        }

        public boolean appliesToEveryRole() {
            return roleCodes.isEmpty();
        }
    }

    /**
     * The caller's active jurisdictions, or an empty list when HRMS cannot say.
     *
     * An empty list is **not** treated as "no access" by callers of this method - see
     * {@code RoleJurisdictionScope}, which fails open. An inbox that empties itself because a
     * lookup timed out is a worse failure than one that shows too much, and it matches what the
     * rest of this service already does with workflow lookups.
     */
    public List<JurisdictionGrant> getJurisdictionsForCaller(RequestInfo requestInfo) {
        if (requestInfo == null || requestInfo.getUserInfo() == null
                || StringUtils.isEmpty(requestInfo.getUserInfo().getUuid())) {
            log.warn("No caller uuid on the request; cannot resolve jurisdictions");
            return Collections.emptyList();
        }
        String uuid = requestInfo.getUserInfo().getUuid();
        String tenantId = requestInfo.getUserInfo().getTenantId();

        StringBuilder uri = new StringBuilder(hrmsHost).append(hrmsSearchEndpoint)
                .append("?uuids=").append(uuid)
                .append("&isActive=true");
        if (!StringUtils.isEmpty(tenantId)) {
            uri.append("&tenantId=").append(tenantId);
        }

        try {
            Object response = serviceRequestRepository.fetchResult(
                    uri, RequestInfoWrapper.builder().requestInfo(requestInfo).build());
            return parseJurisdictions(response);
        } catch (Exception e) {
            log.error("Failed to read jurisdictions from HRMS for caller {}", uuid, e);
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    private List<JurisdictionGrant> parseJurisdictions(Object response) {
        List<JurisdictionGrant> grants = new ArrayList<>();
        if (response == null) {
            return grants;
        }
        Map<String, Object> body = objectMapper.convertValue(response, HashMap.class);
        List<Map<String, Object>> employees = (List<Map<String, Object>>) body.get("Employees");
        if (employees == null || employees.isEmpty()) {
            log.warn("HRMS returned no employee for the caller; no jurisdictions to scope by");
            return grants;
        }
        List<Map<String, Object>> jurisdictions =
                (List<Map<String, Object>>) employees.get(0).get("jurisdictions");
        if (jurisdictions == null) {
            return grants;
        }
        for (Map<String, Object> jurisdiction : jurisdictions) {
            if (jurisdiction == null || Boolean.FALSE.equals(jurisdiction.get("isActive"))) {
                continue;
            }
            Object boundary = jurisdiction.get("boundary");
            if (boundary == null) {
                continue;
            }
            Set<String> roleCodes = new HashSet<>();
            List<Map<String, Object>> roles = (List<Map<String, Object>>) jurisdiction.get("roles");
            if (roles != null) {
                for (Map<String, Object> role : roles) {
                    if (role != null && role.get("code") != null) {
                        roleCodes.add(String.valueOf(role.get("code")));
                    }
                }
            }
            grants.add(new JurisdictionGrant(String.valueOf(boundary),
                    jurisdiction.get("boundaryType") == null ? null : String.valueOf(jurisdiction.get("boundaryType")),
                    roleCodes));
        }
        log.debug("Resolved {} active jurisdiction grants for the caller", grants.size());
        return grants;
    }
}
