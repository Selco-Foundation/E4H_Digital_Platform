package org.egov.util;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.models.RequestInfoWrapper;
import org.egov.common.contract.request.RequestInfo;
import org.egov.config.Configuration;
import org.egov.repository.ServiceRequestRepository;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;


@Component
@Slf4j
public class BoundaryUtil {

    private final ServiceRequestRepository serviceRequestRepository;

    private final Configuration config;

    @Autowired
    public BoundaryUtil(ServiceRequestRepository serviceRequestRepository, Configuration config) {
        this.serviceRequestRepository = serviceRequestRepository;
        this.config = config;
    }

    /**
     * Takes map of locations with boundaryType as key and list of boundaries
     * as its values, tenantId, requestInfo and hierarchyType.
     * For each boundaryType, egov-location service is called with hierarchyType,tenantId and codes as parameters.
     * The boundaries in request are validated against the egov-location response
     *
     * @param locations
     * @param tenantId
     * @param requestInfo
     * @param hierarchyTypeCode
     */
    public void validateBoundaryDetails(Map<String, List<String>> locations, String tenantId, RequestInfo requestInfo, String hierarchyTypeCode) {
        log.trace("BoundaryUtil::validateBoundaryDetails entry");
        log.info("Starting boundary validation for tenant: {}, boundary types: {}", tenantId, locations.size());
        for (Map.Entry<String, List<String>> entry : locations.entrySet()) {
            String boundaryType = entry.getKey();
            List<String> boundaries = entry.getValue();

            log.debug("Validating {} boundaries for boundary type: {} with hierarchyType: {}", boundaries.size(), boundaryType, hierarchyTypeCode);
            StringBuilder uri = getUri(tenantId, hierarchyTypeCode, boundaryType, boundaries);

            Optional<Object> response = Optional.ofNullable(serviceRequestRepository.fetchResult(uri, RequestInfoWrapper.builder().requestInfo(requestInfo).build()));

            if (response.isPresent()) {
                LinkedHashMap responseMap = (LinkedHashMap) response.get();
                if (CollectionUtils.isEmpty(responseMap)) {
                    log.error("Location service returned empty response for boundary validation");
                    throw new CustomException("BOUNDARY ERROR", "The response from location service is empty or null");
                }
                String jsonString = new JSONObject(responseMap).toString();

                for (String boundary : boundaries) {
                    String boundaryCodePath = "$.Boundary[?(@.code==\"{}\")]";
                    String jsonpath = boundaryCodePath.replace("{}", boundary);
                    DocumentContext context = JsonPath.parse(jsonString);
                    Object boundaryObject = context.read(jsonpath);

                    if (!(boundaryObject instanceof ArrayList) || CollectionUtils.isEmpty((ArrayList) boundaryObject)) {
                        log.error("Boundary data not available for code: {}", boundary);
                        throw new CustomException("INVALID_BOUNDARY_DATA", "The boundary data for the code "
                                + boundary + " is not available");
                    }
                }
                log.debug("Successfully validated {} boundaries for boundary type: {}", boundaries.size(), boundaryType);
            } else {
                log.error("Error in fetching data from egov-location service for boundary validation, tenant: {}", tenantId);
                throw new CustomException("EGOV_LOCATION_SERVICE_FAILED", "Error in fetching data from egov-location");
            }
        }
        log.info("Boundary validation completed successfully for tenant: {}", tenantId);
    }

    /**
     * The given boundaries and everything beneath them, as a flat set.
     *
     * This is what turns "is this jurisdiction inside the admin's area?" into a set membership
     * test. Resolved through boundary-service rather than by comparing boundary codes as strings:
     * the codes look hierarchical (`India_Telangana_Hyderabad`), but a prefix match confuses
     * `India_Telangana_Hyderabad` with `India_Telangana_Hyderabad2`, and the level separator is a
     * literal underscore that pattern matching treats as a wildcard.
     *
     * Returns an empty set when the boundary service answers nothing. The caller decides what that
     * means; for authorisation it has to mean "refuse", never "allow everything".
     */
    public Set<String> codesBeneath(RequestInfo requestInfo, String tenantId, Collection<String> codes) {
        Set<String> wanted = new LinkedHashSet<>();
        if (codes != null) {
            codes.stream().filter(StringUtils::isNotBlank).forEach(wanted::add);
        }
        if (wanted.isEmpty()) {
            return Collections.emptySet();
        }

        StringBuilder uri = new StringBuilder(config.getLocationHost())
                .append(config.getLocationContextPath())
                .append(config.getLocationBoundaryRelationshipEndpoint())
                .append("?tenantId=").append(tenantId)
                .append("&hierarchyType=").append(config.getLocationJurisdictionHierarchyType())
                .append("&includeChildren=true")
                .append("&codes=").append(StringUtils.join(wanted, ','));

        Object response;
        try {
            response = serviceRequestRepository.fetchResult(uri,
                    RequestInfoWrapper.builder().requestInfo(requestInfo).build());
        } catch (Exception exception) {
            log.error("Boundary service call failed while expanding {}", wanted, exception);
            throw new CustomException("BOUNDARY_EXPANSION_FAILED",
                    "Could not resolve the boundaries beneath " + wanted);
        }

        Set<String> found = new LinkedHashSet<>(wanted);
        collectCodes(response, found);
        log.debug("Expanded {} boundaries to {} codes", wanted.size(), found.size());
        return found;
    }

    /**
     * Walks whatever shape the relationship search returned, collecting every `code`.
     *
     * Deliberately structure-agnostic: the response nests boundaries under `TenantBoundary[].boundary[]`
     * and then under `children[]`, and asking for codes wherever they appear costs nothing while
     * surviving a change in how deeply they are wrapped.
     */
    private void collectCodes(Object node, Set<String> found) {
        if (node instanceof Map<?, ?> map) {
            Object code = map.get("code");
            if (code instanceof String value && StringUtils.isNotBlank(value)) {
                found.add(value);
            }
            map.values().forEach(child -> collectCodes(child, found));
        } else if (node instanceof Iterable<?> iterable) {
            iterable.forEach(child -> collectCodes(child, found));
        }
    }

    private StringBuilder getUri(String tenantId, String hierarchyTypeCode, String boundaryType, List<String> boundaries) {
        StringBuilder uri = new StringBuilder(config.getLocationHost());
        uri.append(config.getLocationContextPath()).append(config.getLocationEndpoint());
        uri.append("?").append("tenantId=").append(tenantId);

        uri.append("&").append("codes=").append(StringUtils.join(boundaries, ','));
        return uri;
    }

}
