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
     * Each of the given boundaries mapped to itself plus every boundary above it.
     *
     * This is the direction that scales. The question being answered is "is this user's
     * jurisdiction inside the administrator's area?", and it can be asked either way round - but
     * expanding the *administrator* downwards means listing every descendant of, say, a whole
     * state, which runs to thousands of facility-level codes. Asking instead for the ancestors of
     * the handful of boundaries actually being tested keeps the request small no matter how senior
     * the administrator is.
     *
     * Resolved through boundary-service rather than by comparing codes as strings: they look
     * hierarchical (`India_Telangana_Hyderabad`), but a prefix match confuses
     * `India_Telangana_Hyderabad` with `India_Telangana_Hyderabad2`, and the level separator is a
     * literal underscore that pattern matching treats as a wildcard.
     *
     * A boundary the service knows nothing about maps to just itself, so an exact match still
     * works and nothing is granted by accident.
     */
    public Map<String, Set<String>> ancestorsOf(RequestInfo requestInfo, String tenantId, Collection<String> codes) {
        Set<String> wanted = new LinkedHashSet<>();
        if (codes != null) {
            codes.stream().filter(StringUtils::isNotBlank).forEach(wanted::add);
        }

        Map<String, Set<String>> chains = new LinkedHashMap<>();
        wanted.forEach(code -> chains.put(code, new LinkedHashSet<>(List.of(code))));
        if (wanted.isEmpty()) {
            return chains;
        }

        StringBuilder uri = new StringBuilder(config.getLocationHost())
                .append(config.getLocationContextPath())
                .append(config.getLocationBoundaryRelationshipEndpoint())
                .append("?tenantId=").append(tenantId)
                .append("&hierarchyType=").append(config.getLocationJurisdictionHierarchyType())
                .append("&includeParents=true")
                .append("&codes=").append(StringUtils.join(wanted, ','));

        Object response;
        try {
            response = serviceRequestRepository.fetchResult(uri,
                    RequestInfoWrapper.builder().requestInfo(requestInfo).build());
        } catch (Exception exception) {
            log.error("Boundary service call failed while resolving ancestors of {}", wanted, exception);
            throw new CustomException("BOUNDARY_ANCESTOR_LOOKUP_FAILED",
                    "Could not resolve the boundaries above " + wanted);
        }

        collectChains(response, new ArrayList<>(), wanted, chains);
        return chains;
    }

    /**
     * Walks the returned tree, remembering the path taken, and records that path for every
     * boundary that was asked about.
     *
     * The path is what carries the answer: a node's ancestors are exactly the codes above it in
     * the walk, so no second lookup is needed and the shape of the wrapper does not matter.
     */
    private void collectChains(Object node, List<String> path, Set<String> wanted,
                               Map<String, Set<String>> chains) {
        if (node instanceof Map<?, ?> map) {
            Object rawCode = map.get("code");
            boolean isBoundary = rawCode instanceof String value && StringUtils.isNotBlank(value);

            if (isBoundary) {
                String code = (String) rawCode;
                path.add(code);
                if (wanted.contains(code)) {
                    chains.get(code).addAll(path);
                }
            }

            map.forEach((key, child) -> {
                // Only descend through nesting, never back through the node's own scalar fields.
                if (child instanceof Map || child instanceof Iterable) {
                    collectChains(child, path, wanted, chains);
                }
            });

            if (isBoundary) {
                path.remove(path.size() - 1);
            }
        } else if (node instanceof Iterable<?> iterable) {
            iterable.forEach(child -> collectChains(child, path, wanted, chains));
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
