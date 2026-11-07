package org.egov.im.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.im.config.IMConfiguration;
import org.egov.im.repository.ServiceRequestRepository;
import org.egov.im.web.models.RequestInfoWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * The signed-in user's own jurisdictions, read from HRMS.
 *
 * <h3>Why v2</h3>
 * v1 and v2 differ in the one thing this is for: the roles attached to each jurisdiction. Without
 * them a grant says only "this person has some business in Karnataka", not "they are a Tech POC
 * there" - and the whole point of this check is that holding a role somewhere does not mean holding
 * it everywhere.
 *
 * <h3>It reports failure rather than hiding it</h3>
 * {@link Outcome} keeps "HRMS says you hold nothing" apart from "HRMS did not answer". They lead to
 * the same refusal, but only one of them is an outage, and the caller must be able to say which.
 * The inbox's equivalent deliberately flattens both to an empty list because it fails open; this
 * one guards a privilege boundary and fails closed, so the distinction has to survive.
 */
@Slf4j
@Component
public class HrmsJurisdictionUtil {

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private IMConfiguration config;

    @Autowired
    private ObjectMapper mapper;

    /** One grant: a boundary, and the roles it applies to. */
    public static class Grant {
        private final String boundary;
        private final Set<String> roleCodes;

        Grant(String boundary, Set<String> roleCodes) {
            this.boundary = boundary;
            this.roleCodes = roleCodes;
        }

        public String getBoundary() {
            return boundary;
        }

        /**
         * An empty set is not "no roles" - it is "every role the holder has", which is what every
         * grant made before role scoping looks like. Reading it the other way round would empty the
         * platform.
         */
        public boolean appliesTo(String roleCode, Set<String> heldRoles) {
            if (roleCodes.isEmpty()) {
                return heldRoles.contains(roleCode);
            }
            return roleCodes.contains(roleCode);
        }
    }

    /** The result of asking HRMS, with the failure modes kept distinct. */
    public static class Outcome {
        private final boolean resolved;
        private final List<Grant> grants;

        private Outcome(boolean resolved, List<Grant> grants) {
            this.resolved = resolved;
            this.grants = grants;
        }

        static Outcome of(List<Grant> grants) {
            return new Outcome(true, grants);
        }

        static Outcome unavailable() {
            return new Outcome(false, Collections.emptyList());
        }

        /** False means HRMS could not be asked - refuse, and alarm. Not the same as holding nothing. */
        public boolean isResolved() {
            return resolved;
        }

        public List<Grant> getGrants() {
            return grants;
        }
    }

    public Outcome getJurisdictionsForCaller(RequestInfo requestInfo) {
        if (requestInfo == null || requestInfo.getUserInfo() == null
                || !StringUtils.hasText(requestInfo.getUserInfo().getUuid())) {
            log.warn("No caller uuid on the request; cannot resolve jurisdictions");
            return Outcome.unavailable();
        }
        String uuid = requestInfo.getUserInfo().getUuid();
        String tenantId = requestInfo.getUserInfo().getTenantId();

        StringBuilder uri = new StringBuilder(config.getHrmsHost())
                .append(config.getHrmsV2SearchEndpoint())
                .append("?uuids=").append(uuid)
                .append("&isActive=true");
        if (StringUtils.hasText(tenantId)) {
            uri.append("&tenantId=").append(tenantId);
        }

        try {
            Object response = serviceRequestRepository.fetchResult(
                    uri, RequestInfoWrapper.builder().requestInfo(requestInfo).build());
            return parse(response, uuid);
        } catch (Exception e) {
            log.error("Failed to read jurisdictions from HRMS for caller {}", uuid, e);
            return Outcome.unavailable();
        }
    }

    @SuppressWarnings("unchecked")
    private Outcome parse(Object response, String uuid) {
        if (response == null) {
            log.error("HRMS returned no body for caller {}", uuid);
            return Outcome.unavailable();
        }
        Map<String, Object> body = mapper.convertValue(response, Map.class);
        List<Map<String, Object>> employees = (List<Map<String, Object>>) body.get("Employees");
        if (employees == null || employees.isEmpty()) {
            // A real answer about an unknown employee, not a transport failure - but it still means
            // we cannot say where they may act, so it refuses like one.
            log.warn("HRMS returned no employee for caller {}", uuid);
            return Outcome.of(Collections.emptyList());
        }

        List<Map<String, Object>> jurisdictions =
                (List<Map<String, Object>>) employees.get(0).get("jurisdictions");
        if (jurisdictions == null) {
            return Outcome.of(Collections.emptyList());
        }

        List<Grant> grants = new ArrayList<>();
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
                    Optional.ofNullable(role)
                            .map(r -> r.get("code"))
                            .ifPresent(code -> roleCodes.add(String.valueOf(code)));
                }
            }
            grants.add(new Grant(String.valueOf(boundary), roleCodes));
        }
        return Outcome.of(grants);
    }
}
