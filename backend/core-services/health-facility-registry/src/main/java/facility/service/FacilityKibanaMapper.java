package facility.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import facility.config.Configuration;
import facility.repository.ServiceRequestRepository;
import facility.web.models.BoundaryInfo;
import facility.web.models.Facility;
import facility.util.FacilityAmcFieldsHelper;
import facility.util.FacilityMappedVendorHelper;
import facility.web.models.FacilityKibanaIndex;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import static facility.service.FacilityService.CATEGORY_ANGANWADI;

/**
 * Mapper service to transform Facility objects to Kibana index format
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class FacilityKibanaMapper {

    private final ServiceRequestRepository serviceRequestRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final Configuration configs;
    private final IncidentStatusDao incidentStatusDao;
    private final FacilityProjectClient facilityProjectClient;

    private static final String LOCALIZATION_MODULE = "rainmaker-in";
    private static final String LOCALIZATION_LOCALE = "en_IN";
    /** Boundary localizations are stored at national tenant (see ingestion-service / FacilityService). */
    private static final String LOCALIZATION_TENANT_ID = "in";

    @Value("${egov.boundary.host}")
    private String boundaryHost;

    @Value("${egov.boundary.relationship.search.path}")
    private String boundaryRelationshipSearchPath;

    @Value("${egov.indexer.es.host.name}")
    private String esHost;

    @Value("${egov.indexer.es.port.no}")
    private String esPort;

    private static final String INDEX_NAME = "health-facility-index-v0001";
    private static final String SEARCH_PATH = "_search";

    // Elasticsearch authentication
    @Value("${egov.indexer.es.username}")
    private String esUsername;

    @Value("${egov.indexer.es.password}")
    private String esPassword;

    /**
     * Transforms a Facility object to the format expected by Kibana indexer
     * 
     * @param facility The facility to transform
     * @param requestInfo RequestInfo for boundary service calls
     * @return FacilityKibanaIndex object ready for indexing
     */
    public FacilityKibanaIndex toKibanaIndex(Facility facility, RequestInfo requestInfo) {
        if (facility == null) {
            return null;
        }

        FacilityKibanaIndex.FacilityKibanaIndexBuilder builder = FacilityKibanaIndex.builder()
                .facilityId(facility.getFacilityId())
                .name(facility.getFacilityName())
                // Match existing index docs: null when unknown (not an empty object)
                .phcName(null)
                .phcType(facility.getFacilityType())
                .tenantId(facility.getTenantId())
                .facilityCategory(facility.getFacilityCategory())
                // Used downstream as the health facility display name (see im-services-analytics)
                .tenantIdLocalized(resolveTenantIdLocalized(facility))
                .code(resolveFacilityCodeForIndex(facility))
                .type(facility.getFacilityType())
                .isLive(facility.getIsActive())
                .synced(false)
                .totalTickets(0)
                .openTickets(0)
                .closedTickets(0)
                .lastModifiedTime(System.currentTimeMillis());

        // Set geoPoint from address if available
        if (facility.getAddress() != null && 
            facility.getAddress().getLatitude() != null && 
            facility.getAddress().getLongitude() != null) {
            String geoPoint = facility.getAddress().getLatitude() + "," + facility.getAddress().getLongitude();
            builder.geoPoint(geoPoint);
        }

        applySolarPanelStatus(facility, builder);

        applyMappedVendorFields(facility, builder);
        if (facility.getFacilityDetails() != null && facility.getFacilityDetails().getSolarSolutionDesignType() != null) {
            builder.solutionDesignType(facility.getFacilityDetails().getSolarSolutionDesignType().name());
        }

        // Fetch boundary hierarchy and extract codes
        BoundaryCodes boundaryCodes = fetchBoundaryHierarchy(facility, requestInfo);
        
        // Set top-level fields from boundary hierarchy
        String blockCode = null;
        String districtCode = null;
        String stateCode = null;
        String countryCode = null;
        if (boundaryCodes != null) {
            blockCode = boundaryCodes.getBlockCode();
            districtCode = boundaryCodes.getDistrictCode();
            stateCode = boundaryCodes.getStateCode();
            countryCode = boundaryCodes.getCountryCode();
            // Top-level state/district/block are human-readable labels from localization (spaces preserved)
            Map<String, String> boundaryLabels = fetchBoundaryDisplayLabels(requestInfo, stateCode, districtCode, blockCode);
            builder.block(resolveBoundaryDisplayLabel(blockCode, boundaryLabels))
                   .district(resolveBoundaryDisplayLabel(districtCode, boundaryLabels))
                   .state(resolveBoundaryDisplayLabel(stateCode, boundaryLabels));
        }
        
        // Build boundary info from fetched hierarchy (use extracted values)
        BoundaryInfo boundaryInfo = buildBoundaryInfo(facility, boundaryCodes, blockCode, districtCode, stateCode, countryCode);
        builder.boundary(boundaryInfo);
        
        // Log boundary object for debugging
        if (boundaryInfo != null) {
            log.info("Boundary object built for facility {}: facilityCode={}, blockCode={}, districtCode={}, stateCode={}, countryCode={}",
                    facility.getFacilityId(), boundaryInfo.getFacilityCode(), boundaryInfo.getBlockCode(), 
                    boundaryInfo.getDistrictCode(), boundaryInfo.getStateCode(), boundaryInfo.getCountryCode());
        } else {
            log.warn("Boundary object is null for facility {}", facility.getFacilityId());
        }

        FacilityKibanaIndex result = builder.build();
        // Populate the project name mapped to this facility so it lands in the index.
        result.setProjectName(resolveProjectName(facility, requestInfo, null));
        // AMC fields live only on the index (amc-scheduler-service owns them and they are never
        // persisted here), so this freshly-built document has no source to rebuild them from - carry
        // whatever is already indexed forward, otherwise an operator re-index would wipe them.
        FacilityAmcFieldsHelper.copyAmcFields(
                fetchExistingKibanaIndex(facility.getFacilityId(), facility.getTenantId()), result);
        // Log the full boundary object in the result
        if (result.getBoundary() != null) {
            log.info("Boundary in FacilityKibanaIndex: {}", result.getBoundary());
        } else {
            log.warn("Boundary is null in FacilityKibanaIndex for facility {}", facility.getFacilityId());
        }
        return result;
    }

    /**
     * Derives solar panel status and the time the facility went non-functional, and sets both on
     * {@code builder}.
     *
     * <p>Status is computed the same way the im-services-analytics Kafka listener does: NON_FUNCTIONAL
     * when any open incident for this boundary code is non-functional, else FUNCTIONAL. An explicit
     * value in {@code additionalDetails} still takes precedence over the incident-derived status.
     */
    private void applySolarPanelStatus(Facility facility, FacilityKibanaIndex.FacilityKibanaIndexBuilder builder) {
        IncidentStatusDao.SolarPanelState solarPanelState =
                incidentStatusDao.resolveSolarPanelState(facility.getBoundaryCode());
        String solarPanelStatus = solarPanelState.status();
        Long nonFunctionalTimestamp = solarPanelState.nonFunctionalSince();

        Object override = facility.getAdditionalDetails() != null
                ? facility.getAdditionalDetails().get("solarPanelStatus")
                : null;
        if (override != null && !override.toString().isBlank()) {
            solarPanelStatus = override.toString();
            // The override replaces the incident-derived status, so the incident-derived timestamp no
            // longer describes it. Keep it only while the override still says non-functional.
            if (!IncidentStatusDao.NON_FUNCTIONAL.equals(solarPanelStatus)) {
                nonFunctionalTimestamp = null;
            }
        }

        builder.solarPanelStatus(solarPanelStatus);
        builder.nonFunctionalTimestamp(nonFunctionalTimestamp);
    }

    /**
     * Updates only mutable "display" fields for an existing Kibana index document.
     * Falls back to full mapping when no existing document can be found.
     */
    public FacilityKibanaIndex toKibanaIndexForFacilityUpdate(Facility facility, RequestInfo requestInfo) {
        if (facility == null) {
            log.info("Skipping Kibana index update mapping: facility is null");
            return null;
        }

        log.info("Preparing Kibana index update mapping for facilityId={} tenantId={}",
                facility.getFacilityId(), facility.getTenantId());
        FacilityKibanaIndex existingDoc = fetchExistingKibanaIndex(facility.getFacilityId(), facility.getTenantId());
        log.info("Existing Kibana document found for facilityId={} existingDoc={}",
                facility.getFacilityId(), existingDoc);
        if (existingDoc == null) {
            log.info("No existing Kibana document found for facilityId={} tenantId={}; falling back to full mapping",
                    facility.getFacilityId(), facility.getTenantId());
            return toKibanaIndex(facility, requestInfo);
        }

        if (facility.getFacilityName() != null && !facility.getFacilityName().isBlank()) {
            existingDoc.setName(facility.getFacilityName());
            existingDoc.setTenantIdLocalized(facility.getFacilityName());
            log.info("Updated Kibana field name for facilityId={}", facility.getFacilityId());
        }
        if (facility.getFacilityType() != null && !facility.getFacilityType().isBlank()) {
            existingDoc.setType(facility.getFacilityType());
            existingDoc.setPhcType(facility.getFacilityType());
            log.info("Updated Kibana fields type/phcType for facilityId={}", facility.getFacilityId());
        }
        if (facility.getFacilityCategory() != null && !facility.getFacilityCategory().isBlank()) {
            existingDoc.setFacilityCategory(facility.getFacilityCategory());
            log.info("Updated Kibana field facilityCategory={} for facilityId={}",
                    facility.getFacilityCategory(), facility.getFacilityId());
        }
        if (facility.getIsActive() != null) {
            existingDoc.setIsLive(facility.getIsActive());
            log.info("Updated Kibana field isLive={} for facilityId={}",
                    facility.getIsActive(), facility.getFacilityId());
        }
        existingDoc.setMappedVendorName(facility.getMappedVendorName());
        existingDoc.setMappedVendorUserName(facility.getMappedVendorUserName());
        log.info("Updated Kibana mapped vendor fields for facilityId={} (name={}, userName={})",
                facility.getFacilityId(), facility.getMappedVendorName(), facility.getMappedVendorUserName());
        if (facility.getFacilityDetails() != null && facility.getFacilityDetails().getSolarSolutionDesignType() != null) {
            existingDoc.setSolutionDesignType(facility.getFacilityDetails().getSolarSolutionDesignType().name());
        }
        // AMC fields need no handling here: existingDoc was deserialized from the indexed document, so
        // it already carries them and they round-trip untouched.
        // Refresh projectName from the project service; keep the already-indexed value when the
        // lookup yields nothing so it is never lost on a re-index.
        existingDoc.setProjectName(resolveProjectName(facility, requestInfo, existingDoc.getProjectName()));
        existingDoc.setLastModifiedTime(System.currentTimeMillis());
        log.info("Completed Kibana index update mapping for facilityId={} tenantId={}",
                facility.getFacilityId(), facility.getTenantId());

        return existingDoc;
    }

    /**
     * Sets indexer {@link FacilityKibanaIndex#getCode()} to {@code code} (trimmed, non-blank).
     * When an Elasticsearch document exists, only {@code code} and {@code lastModifiedTime} are changed.
     * When no document exists, performs a full index mapping ({@link #toKibanaIndex(Facility, RequestInfo)}).
     */
    public FacilityKibanaIndex toKibanaIndexPatchCode(Facility facility, String code, RequestInfo requestInfo) {
        if (facility == null || code == null || code.trim().isEmpty()) {
            log.warn("Skipping Kibana code patch: facility null or code blank");
            return null;
        }
        RequestInfo effectiveInfo = requestInfo != null ? requestInfo : new RequestInfo();
        String trimmedCode = code.trim();

        FacilityKibanaIndex existingDoc = fetchExistingKibanaIndex(facility.getFacilityId(), null);
        if (existingDoc == null) {
            log.info("No ES doc for facilityId={}; full index mapping with code={}", facility.getFacilityId(), trimmedCode);
            FacilityKibanaIndex fullIndex = toKibanaIndex(facility, effectiveInfo);
            if (fullIndex != null) {
                fullIndex.setCode(trimmedCode);
            }
            return fullIndex;
        }

        existingDoc.setCode(trimmedCode);
        existingDoc.setProjectName(resolveProjectName(facility, effectiveInfo, existingDoc.getProjectName()));
        existingDoc.setLastModifiedTime(System.currentTimeMillis());
        log.info("Patched Kibana code for facilityId={} tenantId={}", facility.getFacilityId(), facility.getTenantId());
        return existingDoc;
    }

    /**
     * Resolves the project name for a facility from the project service. Falls back to
     * {@code existingProjectName} (the value already in the index) when the lookup yields
     * nothing, so an existing projectName is never lost on a full-document re-index.
     */
    private String resolveProjectName(Facility facility, RequestInfo requestInfo, String existingProjectName) {
        if (facility == null || facility.getFacilityId() == null || facility.getFacilityId().isBlank()) {
            return existingProjectName;
        }
        String fetched = facilityProjectClient.fetchProjectName(
                requestInfo, facility.getTenantId(), facility.getFacilityId());
        if (fetched != null && !fetched.isBlank()) {
            return fetched;
        }
        return existingProjectName;
    }

    /**
     * Prefer HFR / official facility code for the index {@code code} field; fall back to boundary code.
     */
    private static String resolveTenantIdLocalized(Facility facility) {
        String name = facility.getFacilityName();
        if (name != null && !name.isBlank()) {
            return name;
        }
        return facility.getTenantId();
    }

    private static String resolveFacilityCodeForIndex(Facility facility) {
        String normalizedCategory = facility.getFacilityCategory() == null
                ? ""
                : facility.getFacilityCategory().trim().toUpperCase(Locale.ROOT);
        boolean isAnganwadi = CATEGORY_ANGANWADI.equals(normalizedCategory);

        String code = "";
        if (isAnganwadi) {
            String username = facility.getFacilityPocUsername();
            if (username != null && !username.isBlank()) {
                code = username.trim();
            }
            else
                code = facility.getBoundaryCode();
        }
        else{
            String username = facility.getHfrId() != null && !facility.getHfrId().trim().isBlank() ? facility.getHfrId().trim() : facility.getNinId();
            if (username != null && !username.isBlank()) {
                code = username.trim();
            }
            else
                code = facility.getBoundaryCode();
        }
        return code;
    }

    /**
     * Resolves a human-readable boundary label via egov-localization ({@code Boundary_{code}}).
     * Falls back to the last hierarchy segment when localization is missing or unavailable.
     */
    private String resolveBoundaryDisplayLabel(String boundaryCode, Map<String, String> labels) {
        if (boundaryCode == null || boundaryCode.isBlank()) {
            return null;
        }
        String localizationCode = toLocalizationCode(boundaryCode);
        if (labels != null) {
            String localized = labels.get(localizationCode);
            if (localized != null && !localized.isBlank()) {
                return localized;
            }
        }
        return boundaryHierarchyCodeToDisplayLabel(boundaryCode);
    }

    private Map<String, String> fetchBoundaryDisplayLabels(RequestInfo requestInfo, String... boundaryCodes) {
        List<String> localizationCodes = new ArrayList<>();
        for (String code : boundaryCodes) {
            if (code != null && !code.isBlank()) {
                localizationCodes.add(toLocalizationCode(code));
            }
        }
        if (localizationCodes.isEmpty()) {
            return Map.of();
        }

        String searchUrl = buildLocalizationSearchUrl();
        if (searchUrl == null || searchUrl.isBlank()) {
            log.warn("Localization search URL not configured; using code fragment fallback for boundaries");
            return Map.of();
        }

        String url = UriComponentsBuilder.fromHttpUrl(searchUrl)
                .queryParam("tenantId", LOCALIZATION_TENANT_ID)
                .queryParam("module", LOCALIZATION_MODULE)
                .queryParam("locale", LOCALIZATION_LOCALE)
                .queryParam("codes", String.join(",", localizationCodes))
                .build()
                .toUriString();

        Map<String, Object> body = new HashMap<>();
        if (requestInfo != null) {
            body.put("RequestInfo", requestInfo);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, new HttpEntity<>(body.isEmpty() ? null : body, headers), String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("Localization search returned no labels for codes={}", localizationCodes);
                return Map.of();
            }
            return parseLocalizationMessages(response.getBody());
        } catch (Exception e) {
            log.warn("Localization search failed for boundary codes {}: {}", localizationCodes, e.getMessage());
            return Map.of();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> parseLocalizationMessages(String responseBody) {
        Map<String, String> result = new HashMap<>();
        try {
            Map<String, Object> root = mapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
            Object messagesObj = root.get("messages");
            if (!(messagesObj instanceof List)) {
                return result;
            }
            for (Object messageObj : (List<?>) messagesObj) {
                if (!(messageObj instanceof Map)) {
                    continue;
                }
                Map<String, Object> message = (Map<String, Object>) messageObj;
                String code = (String) message.get("code");
                String text = (String) message.get("message");
                if (code != null && !code.isBlank() && text != null && !text.isBlank()) {
                    result.put(code, text);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse localization response: {}", e.getMessage());
        }
        return result;
    }

    private String buildLocalizationSearchUrl() {
        String host = configs.getLocalizationHost();
        String contextPath = configs.getLocalizationContextPath();
        String searchEndpoint = configs.getLocalizationSearchEndpoint();
        if (host == null || host.isBlank() || contextPath == null || contextPath.isBlank()
                || searchEndpoint == null || searchEndpoint.isBlank()) {
            return null;
        }
        return host + contextPath + searchEndpoint;
    }

    private static String toLocalizationCode(String boundaryCode) {
        if (boundaryCode.startsWith("Boundary_")) {
            return boundaryCode;
        }
        return "Boundary_" + boundaryCode;
    }

    /**
     * Fallback when localization is unavailable: last segment of private static String boundaryHierarchyCodeToDisplayLabel(String boundaryCode) {
     if (boundaryCode == null || boundaryCode.isBlank()) {
     return null;
     }
     int i = boundaryCode.lastIndexOf('_');
     if (i < 0) {
     return boundaryCode;
     }
     return boundaryCode.substring(i + 1);
     }the hierarchy code (spaces not restored).
     */
    private static String boundaryHierarchyCodeToDisplayLabel(String boundaryCode) {
        if (boundaryCode == null || boundaryCode.isBlank()) {
            return null;
        }
        int i = boundaryCode.lastIndexOf('_');
        if (i < 0) {
            return boundaryCode;
        }
        return boundaryCode.substring(i + 1);
    }

    @SuppressWarnings("unchecked")
    private void applyMappedVendorFields(Facility facility, FacilityKibanaIndex.FacilityKibanaIndexBuilder builder) {
        FacilityMappedVendorHelper.hydrateFromAdditionalDetails(facility);
        String user = firstNonBlankString(facility.getMappedVendorUserName());
        String name = firstNonBlankString(facility.getMappedVendorName());
        Map<String, Object> ad = facility.getAdditionalDetails();
        if (ad != null && !ad.isEmpty()) {
            if (user == null) {
                user = firstNonBlankString(
                        ad.get(FacilityMappedVendorHelper.MAPPED_VENDOR_USER_NAME_KEY),
                        ad.get("mapped_vendor_user_name"),
                        ad.get("mappedVendorUsername"));
            }
            if (name == null) {
                name = firstNonBlankString(
                        ad.get(FacilityMappedVendorHelper.MAPPED_VENDOR_NAME_KEY),
                        ad.get("mapped_vendor_name"));
            }
            if (user == null || name == null) {
                Object nested = ad.get("vendor");
                if (nested instanceof Map) {
                    Map<String, Object> v = (Map<String, Object>) nested;
                    if (user == null) {
                        user = firstNonBlankString(v.get("userName"), v.get("mappedVendorUserName"), v.get("username"));
                    }
                    if (name == null) {
                        name = firstNonBlankString(v.get("name"), v.get("vendorName"));
                    }
                }
            }
        }
        if (user == null && name == null) {
            return;
        }
        if (user != null) {
            builder.mappedVendorUserName(user);
        }
        if (name != null) {
            builder.mappedVendorName(name);
        }
    }

    private static String firstNonBlankString(Object... values) {
        if (values == null) {
            return null;
        }
        for (Object o : values) {
            if (o == null) {
                continue;
            }
            String s = o.toString();
            if (!s.isBlank()) {
                return s;
            }
        }
        return null;
    }

    /**
     * Fetches boundary hierarchy from boundary service and extracts codes by boundary type.
     * When the Facility relationship is not yet persisted (async Kafka create), falls back to the
     * parent block code which is already present in boundary_relationship.
     */
    private BoundaryCodes fetchBoundaryHierarchy(Facility facility, RequestInfo requestInfo) {
        if (facility.getBoundaryCode() == null || facility.getTenantId() == null) {
            log.warn("Cannot fetch boundary hierarchy: boundaryCode or tenantId is null");
            return null;
        }

        try {
            BoundaryCodes codes = fetchBoundaryHierarchyForCode(
                    facility.getTenantId(),
                    facility.getBoundaryCode(),
                    "Facility",
                    requestInfo
            );

            if (!hasParentHierarchy(codes)) {
                String blockBoundaryCode = deriveBlockBoundaryCode(
                        facility.getBoundaryCode(), facility.getFacilityId());
                if (blockBoundaryCode != null) {
                    log.info(
                            "Parent hierarchy missing for facility {}; resolving via block boundary {}",
                            facility.getFacilityId(), blockBoundaryCode
                    );
                    BoundaryCodes blockHierarchy = fetchBoundaryHierarchyForCode(
                            facility.getTenantId(),
                            blockBoundaryCode,
                            "Block",
                            requestInfo
                    );
                    codes = mergeBoundaryCodes(blockHierarchy, codes, facility);
                }
            } else if (codes.getFacilityCode() == null) {
                codes.setFacilityCode(facility.getBoundaryCode());
            }

            return codes;
        } catch (Exception e) {
            log.error("Error fetching boundary hierarchy for facility {}: {}",
                    facility.getFacilityId(), e.getMessage(), e);
            return null;
        }
    }

    private BoundaryCodes fetchBoundaryHierarchyForCode(
            String tenantId,
            String boundaryCode,
            String boundaryType,
            RequestInfo requestInfo
    ) {
        Map<String, Object> requestBody =
                requestInfo != null ? Map.of("RequestInfo", requestInfo) : Map.of();

        String uri = UriComponentsBuilder.fromUriString(boundaryHost)
                .path(boundaryRelationshipSearchPath)
                .queryParam("tenantId", tenantId)
                .queryParam("hierarchyType", configs.getBoundaryHierarchyType())
                .queryParam("boundaryType", boundaryType)
                .queryParam("includeParents", true)
                .queryParam("includeChildren", false)
                .queryParam("codes", boundaryCode)
                .toUriString();

        Object rawResponse = serviceRequestRepository.fetchResult(new StringBuilder(uri), requestBody);
        Map<String, Object> response = mapper.convertValue(rawResponse, new TypeReference<Map<String, Object>>() {});
        return parseBoundaryHierarchy(response);
    }

    private static boolean hasParentHierarchy(BoundaryCodes codes) {
        return codes != null && codes.getBlockCode() != null && !codes.getBlockCode().isBlank();
    }

    private static String deriveBlockBoundaryCode(String facilityBoundaryCode, String facilityId) {
        if (facilityBoundaryCode == null || facilityId == null) {
            return null;
        }
        String suffix = "_" + facilityId;
        if (!facilityBoundaryCode.endsWith(suffix)) {
            return null;
        }
        return facilityBoundaryCode.substring(0, facilityBoundaryCode.length() - suffix.length());
    }

    private static BoundaryCodes mergeBoundaryCodes(
            BoundaryCodes fromBlock,
            BoundaryCodes fromFacility,
            Facility facility
    ) {
        BoundaryCodes merged = new BoundaryCodes();
        if (fromBlock != null) {
            merged.setCountryCode(fromBlock.getCountryCode());
            merged.setStateCode(fromBlock.getStateCode());
            merged.setDistrictCode(fromBlock.getDistrictCode());
            merged.setBlockCode(fromBlock.getBlockCode());
        }
        if (fromFacility != null && fromFacility.getFacilityCode() != null) {
            merged.setFacilityCode(fromFacility.getFacilityCode());
        } else if (facility.getBoundaryCode() != null) {
            merged.setFacilityCode(facility.getBoundaryCode());
        }
        return merged;
    }

    /**
     * Parses the boundary hierarchy response to extract codes by boundary type
     */
    @SuppressWarnings("unchecked")
    private BoundaryCodes parseBoundaryHierarchy(Map<String, Object> response) {
        BoundaryCodes codes = new BoundaryCodes();

        try {
            Object tenantBoundaryObj = response.get("TenantBoundary");
            if (!(tenantBoundaryObj instanceof List) || ((List<?>) tenantBoundaryObj).isEmpty()) {
                return codes;
            }

            List<Map<String, Object>> tenantBoundaryList = (List<Map<String, Object>>) tenantBoundaryObj;
            Map<String, Object> tenantBoundary = tenantBoundaryList.get(0);
            
            Object boundaryObj = tenantBoundary.get("boundary");
            if (!(boundaryObj instanceof List) || ((List<?>) boundaryObj).isEmpty()) {
                return codes;
            }

            List<Map<String, Object>> boundaryList = (List<Map<String, Object>>) boundaryObj;
            
            // Traverse the nested hierarchy to extract codes
            for (Map<String, Object> boundary : boundaryList) {
                extractCodesFromHierarchy(boundary, codes);
            }

        } catch (Exception e) {
            log.error("Error parsing boundary hierarchy response: {}", e.getMessage(), e);
        }

        return codes;
    }

    /**
     * Recursively traverses boundary hierarchy to extract codes by boundary type
     */
    @SuppressWarnings("unchecked")
    private void extractCodesFromHierarchy(Map<String, Object> boundaryNode, BoundaryCodes codes) {
        if (boundaryNode == null) {
            return;
        }

        String code = (String) boundaryNode.get("code");
        String boundaryType = (String) boundaryNode.get("boundaryType");

        // Extract code based on boundary type
        if (code != null && boundaryType != null) {
            switch (boundaryType) {
                case "Country":
                    codes.setCountryCode(code);
                    break;
                case "State":
                    codes.setStateCode(code);
                    break;
                case "District":
                    codes.setDistrictCode(code);
                    break;
                case "Block":
                    codes.setBlockCode(code);
                    break;
                case "Facility":
                    codes.setFacilityCode(code);
                    break;
            }
        }

        // Recursively process children
        Object childrenObj = boundaryNode.get("children");
        if (childrenObj instanceof List) {
            List<Map<String, Object>> children = (List<Map<String, Object>>) childrenObj;
            for (Map<String, Object> child : children) {
                extractCodesFromHierarchy(child, codes);
            }
        }
    }

    /**
     * Builds BoundaryInfo object from extracted codes
     * Uses provided block/district/state values to ensure boundary object is populated
     */
    private BoundaryInfo buildBoundaryInfo(Facility facility, BoundaryCodes codes,
                                          String blockCode, String districtCode, String stateCode, String countryCode) {
        BoundaryInfo.BoundaryInfoBuilder builder = BoundaryInfo.builder();
        
        // Set facilityCode (always available)
        String facilityCodeValue = codes != null && codes.getFacilityCode() != null 
            ? codes.getFacilityCode() : facility.getBoundaryCode();
        builder.facilityCode(facilityCodeValue);

        // Use codes from boundary hierarchy if available, otherwise use provided values
        // This ensures boundary object always has values when top-level fields are populated
        String finalBlockCode = codes != null && codes.getBlockCode() != null ? codes.getBlockCode() : blockCode;
        String finalDistrictCode = codes != null && codes.getDistrictCode() != null ? codes.getDistrictCode() : districtCode;
        String finalStateCode = codes != null && codes.getStateCode() != null ? codes.getStateCode() : stateCode;
        String finalCountryCode = codes != null && codes.getCountryCode() != null ? codes.getCountryCode() : countryCode;
        
        builder.blockCode(finalBlockCode)
               .districtCode(finalDistrictCode)
               .stateCode(finalStateCode)
               .countryCode(finalCountryCode);

        BoundaryInfo result = builder.build();
        log.debug("Built BoundaryInfo: {}", result);
        return result;
    }

    /**
     * Helper class to hold extracted boundary codes
     */
    private static class BoundaryCodes {
        private String countryCode;
        private String stateCode;
        private String districtCode;
        private String blockCode;
        private String facilityCode;

        public String getCountryCode() { return countryCode; }
        public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
        public String getStateCode() { return stateCode; }
        public void setStateCode(String stateCode) { this.stateCode = stateCode; }
        public String getDistrictCode() { return districtCode; }
        public void setDistrictCode(String districtCode) { this.districtCode = districtCode; }
        public String getBlockCode() { return blockCode; }
        public void setBlockCode(String blockCode) { this.blockCode = blockCode; }
        public String getFacilityCode() { return facilityCode; }
        public void setFacilityCode(String facilityCode) { this.facilityCode = facilityCode; }
    }

    /**
     * Checks if a facility with the given facilityId exists in Kibana index
     * 
     * @param facilityId The facility ID to check
     * @param tenantId The tenant ID
     * @param requestInfo RequestInfo for API calls
     * @return true if facility exists in Kibana, false otherwise
     */
    public boolean existsInKibana(String facilityId, String tenantId, RequestInfo requestInfo) {
        if (facilityId == null || tenantId == null) {
            return false;
        }

        try {
            // Build Elasticsearch search query
            Map<String, Object> searchQuery = Map.of(
                "query", Map.of(
                    "bool", Map.of(
                        "must", List.of(
                            Map.of("term", Map.of("Data.facilityId.keyword", facilityId)),
                            Map.of("term", Map.of("Data.tenantId.keyword", tenantId))
                        )
                    )
                ),
                "size", 0
            );

            // Build URI: {host}:{port}/{INDEX_NAME}/{SEARCH_PATH}
            // Example: https://localhost:9200/health-facility-index-v0001/_search
            String uri = getBaseUrl() + "/" + INDEX_NAME + "/" + SEARCH_PATH;

            // Build headers with authentication
            HttpEntity<Object> entity = new HttpEntity<>(searchQuery, buildHeaders());

            log.info("Executing Elasticsearch query to check facility existence: {}", searchQuery);
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(uri, entity, Map.class);

            // Parse response to check if document exists
            return parseSearchResponse(response);

        } catch (Exception e) {
            log.warn("Error checking if facility {} exists in Kibana: {}. Assuming not present.", 
                    facilityId, e.getMessage(), e);
            // If check fails, return false to allow push (fail open approach)
            return false;
        }
    }

    /**
     * Builds the base URL for Elasticsearch API
     * Direct Elasticsearch access: {host}:{port}
     */
    private String getBaseUrl() {
        String host = esHost.endsWith("/") ? esHost.substring(0, esHost.length() - 1) : esHost;
        String port = esPort.startsWith(":") ? esPort : ":" + esPort;
        return host + port;
    }

    /**
     * Builds HTTP headers with Elasticsearch authentication
     */
    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        // Add Basic Authentication if credentials are provided
        if (esUsername != null && !esUsername.isEmpty() && esPassword != null && !esPassword.isEmpty()) {
            String auth = esUsername + ":" + esPassword;
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
            String authHeader = "Basic " + new String(encodedAuth);
            headers.set("Authorization", authHeader);
        }

        return headers;
    }

    /**
     * Parses the search response to determine if any documents were found
     */
    @SuppressWarnings("unchecked")
    private boolean parseSearchResponse(Map<String, Object> response) {
        try {
            // Check for hits in response
            Object hitsObj = response.get("hits");
            if (hitsObj instanceof Map) {
                Map<String, Object> hits = (Map<String, Object>) hitsObj;
                Object totalObj = hits.get("total");
                
                if (totalObj instanceof Map) {
                    // ES 7.x+ format: {"total": {"value": 1}}
                    Map<String, Object> totalMap = (Map<String, Object>) totalObj;
                    Object value = totalMap.get("value");
                    if (value instanceof Number) {
                        return ((Number) value).intValue() > 0;
                    }
                } else if (totalObj instanceof Number) {
                    // ES 6.x format: {"total": 1}
                    return ((Number) totalObj).intValue() > 0;
                }
            }
            
            return false;
        } catch (Exception e) {
            log.error("Error parsing search response: {}", e.getMessage(), e);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private FacilityKibanaIndex fetchExistingKibanaIndex(String facilityId, String tenantId) {
        if (facilityId == null) {
            log.info("Skipping Kibana lookup: facilityId or tenantId is null (facilityId={}, tenantId={})",
                    facilityId, tenantId);
            return null;
        }

        log.info("Fetching existing Kibana document for facilityId={} tenantId={}", facilityId, tenantId);
        try {
            List<Map<String, Object>> mustClauses = new ArrayList<>();

            mustClauses.add(
                    Map.of("term", Map.of("Data.facilityId.keyword", facilityId))
            );

            if (tenantId != null) {
                mustClauses.add(
                        Map.of("term", Map.of("Data.tenantId.keyword", tenantId))
                );
            }

            Map<String, Object> searchQuery = Map.of(
                    "query", Map.of(
                            "bool", Map.of(
                                    "must", mustClauses
                            )
                    ),
                    "size", 1
            );

            String uri = getBaseUrl() + "/" + INDEX_NAME + "/" + SEARCH_PATH;
            HttpEntity<Object> entity = new HttpEntity<>(searchQuery, buildHeaders());
            log.info("Executing Kibana lookup query for facilityId={} tenantId={}", facilityId, tenantId);
            Map<String, Object> response = restTemplate.postForObject(uri, entity, Map.class);
            if (response == null) {
                log.info("No Kibana response received for facilityId={} tenantId={}", facilityId, tenantId);
                return null;
            }

            Object hitsObj = response.get("hits");
            if (!(hitsObj instanceof Map)) {
                log.info("Kibana response missing hits object for facilityId={} tenantId={}", facilityId, tenantId);
                return null;
            }

            Object hitListObj = ((Map<String, Object>) hitsObj).get("hits");
            if (!(hitListObj instanceof List) || ((List<?>) hitListObj).isEmpty()) {
                log.info("No existing Kibana document found for facilityId={} tenantId={}", facilityId, tenantId);
                return null;
            }

            Object firstHitObj = ((List<?>) hitListObj).get(0);
            if (!(firstHitObj instanceof Map)) {
                log.info("Kibana first hit has unexpected format for facilityId={} tenantId={}", facilityId, tenantId);
                return null;
            }

            Object sourceObj = ((Map<String, Object>) firstHitObj).get("_source");
            if (!(sourceObj instanceof Map)) {
                log.info("Kibana hit missing _source for facilityId={} tenantId={}", facilityId, tenantId);
                return null;
            }

            Object dataObj = ((Map<String, Object>) sourceObj).get("Data");
            if (!(dataObj instanceof Map)) {
                log.info("Kibana _source missing Data payload for facilityId={} tenantId={}", facilityId, tenantId);
                return null;
            }

            FacilityKibanaIndex existingDoc = mapper.convertValue(dataObj, FacilityKibanaIndex.class);
            String tenantIdLocalized = (String)((Map<String, Object>) dataObj).get("tenantId_localized");
//            List<Integer> geoPoint = (List<Integer>)((Map<String, Object>) dataObj).get("geo-point");
            Object geoPointObj = ((Map<String, Object>) dataObj).get("geo-point");
            Integer total_tickets = (Integer)((Map<String, Object>) dataObj).get("total_tickets");
            Integer open_tickets = (Integer)((Map<String, Object>) dataObj).get("open_tickets");
            Integer closed_tickets = (Integer)((Map<String, Object>) dataObj).get("closed_tickets");
            String solar_panel_status = (String)((Map<String, Object>) dataObj).get("solar_panel_status");
            existingDoc.setTenantIdLocalized(tenantIdLocalized);
//            String result = geoPoint!= null ? geoPoint.stream().map(String::valueOf).collect(Collectors.joining(", ")) : null;
//            existingDoc.setGeoPoint(result);
            String geoPointStr = null;
            if (geoPointObj instanceof List) {
                geoPointStr = ((List<?>) geoPointObj).stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(", "));
            } else if (geoPointObj != null) {
                geoPointStr = geoPointObj.toString();
            }
            existingDoc.setGeoPoint(geoPointStr);
            existingDoc.setTotalTickets(total_tickets);
            existingDoc.setOpenTickets(open_tickets);
            existingDoc.setClosedTickets(closed_tickets);
            existingDoc.setSolarPanelStatus(solar_panel_status);
            log.info("Successfully fetched existing Kibana document for facilityId={} tenantId={}", facilityId, tenantId);
            return existingDoc;
        } catch (Exception e) {
            log.warn("Unable to fetch existing Kibana document for facility {} and tenant {}: {}",
                    facilityId, tenantId, e.getMessage());
            return null;
        }
    }

    /**
     * Deletes the Elasticsearch document(s) for a facility, matched by {@code Data.facilityId.keyword}
     * (optionally scoped by tenant). Uses {@code _delete_by_query} so a missing document is a no-op.
     *
     * @return number of documents deleted, or {@code -1} when the delete call failed
     */
    @SuppressWarnings("unchecked")
    public int deleteKibanaIndexByFacilityId(String facilityId, String tenantId) {
        if (facilityId == null || facilityId.isBlank()) {
            log.warn("Skipping Kibana delete: facilityId is null or blank");
            return -1;
        }

        log.info("Deleting Kibana document(s) for facilityId={} tenantId={}", facilityId, tenantId);
        try {
            List<Map<String, Object>> mustClauses = new ArrayList<>();
            mustClauses.add(Map.of("term", Map.of("Data.facilityId.keyword", facilityId)));
            if (tenantId != null && !tenantId.isBlank()) {
                mustClauses.add(Map.of("term", Map.of("Data.tenantId.keyword", tenantId)));
            }

            Map<String, Object> deleteQuery = Map.of(
                    "query", Map.of("bool", Map.of("must", mustClauses))
            );

            String uri = getBaseUrl() + "/" + INDEX_NAME + "/_delete_by_query?refresh=true";
            HttpEntity<Object> entity = new HttpEntity<>(deleteQuery, buildHeaders());
            ResponseEntity<Map> response = restTemplate.exchange(uri, HttpMethod.POST, entity, Map.class);

            Map<String, Object> body = response != null ? response.getBody() : null;
            int deleted = 0;
            if (body != null && body.get("deleted") instanceof Number) {
                deleted = ((Number) body.get("deleted")).intValue();
            }
            log.info("Deleted {} Kibana document(s) for facilityId={} tenantId={}", deleted, facilityId, tenantId);
            return deleted;
        } catch (Exception e) {
            log.error("Unable to delete Kibana document for facility {} and tenant {}: {}",
                    facilityId, tenantId, e.getMessage(), e);
            return -1;
        }
    }

    /** Lightweight view of an indexed facility, used by the projectName backfill. */
    public static class IndexedFacilityRef {
        private final String facilityId;
        private final String tenantId;
        private final String projectName;

        public IndexedFacilityRef(String facilityId, String tenantId, String projectName) {
            this.facilityId = facilityId;
            this.tenantId = tenantId;
            this.projectName = projectName;
        }

        public String getFacilityId() { return facilityId; }
        public String getTenantId() { return tenantId; }
        public String getProjectName() { return projectName; }
    }

    /**
     * Scans every document in the health facility index (paginated via {@code search_after}) and
     * returns a {@link IndexedFacilityRef} per document. Optionally filtered by tenant.
     *
     * @param tenantId when non-blank, only documents for this tenant are returned
     * @param pageSize ES page size; defaults to 500 when non-positive
     */
    @SuppressWarnings("unchecked")
    public List<IndexedFacilityRef> fetchAllIndexedFacilities(String tenantId, int pageSize) {
        List<IndexedFacilityRef> all = new ArrayList<>();
        int size = pageSize > 0 ? pageSize : 500;
        String uri = getBaseUrl() + "/" + INDEX_NAME + "/" + SEARCH_PATH;
        List<Object> searchAfter = null;

        try {
            while (true) {
                Map<String, Object> query = new LinkedHashMap<>();
                query.put("size", size);
                if (tenantId != null && !tenantId.isBlank()) {
                    query.put("query", Map.of("bool", Map.of("must", List.of(
                            Map.of("term", Map.of("Data.tenantId.keyword", tenantId))))));
                } else {
                    query.put("query", Map.of("match_all", Map.of()));
                }
                query.put("sort", List.of(Map.of("Data.facilityId.keyword", Map.of("order", "asc"))));
                if (searchAfter != null) {
                    query.put("search_after", searchAfter);
                }

                HttpEntity<Object> entity = new HttpEntity<>(query, buildHeaders());
                Map<String, Object> response = restTemplate.postForObject(uri, entity, Map.class);
                if (response == null) {
                    break;
                }
                Object hitsObj = response.get("hits");
                if (!(hitsObj instanceof Map)) {
                    break;
                }
                Object hitListObj = ((Map<String, Object>) hitsObj).get("hits");
                if (!(hitListObj instanceof List) || ((List<?>) hitListObj).isEmpty()) {
                    break;
                }

                List<?> hitList = (List<?>) hitListObj;
                for (Object hitObj : hitList) {
                    if (!(hitObj instanceof Map)) {
                        continue;
                    }
                    Map<String, Object> hit = (Map<String, Object>) hitObj;
                    Object sortObj = hit.get("sort");
                    if (sortObj instanceof List) {
                        searchAfter = new ArrayList<>((List<Object>) sortObj);
                    }
                    Object sourceObj = hit.get("_source");
                    if (!(sourceObj instanceof Map)) {
                        continue;
                    }
                    Object dataObj = ((Map<String, Object>) sourceObj).get("Data");
                    if (!(dataObj instanceof Map)) {
                        continue;
                    }
                    Map<String, Object> data = (Map<String, Object>) dataObj;
                    String facilityId = (String) data.get("facilityId");
                    if (facilityId == null || facilityId.isBlank()) {
                        continue;
                    }
                    all.add(new IndexedFacilityRef(
                            facilityId,
                            (String) data.get("tenantId"),
                            (String) data.get("projectName")));
                }

                if (hitList.size() < size) {
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Failed while scanning health facility index for projectName backfill: {}", e.getMessage(), e);
        }
        log.info("Scanned {} indexed facilities (tenantId={})", all.size(), tenantId);
        return all;
    }

    /**
     * Sets {@code Data.projectName} on the indexed document(s) for a facility via
     * {@code _update_by_query} (partial update — all other fields are left untouched).
     *
     * @return number of documents updated, or {@code -1} when the update call failed
     */
    @SuppressWarnings("unchecked")
    public int updateProjectNameByFacilityId(String facilityId, String tenantId, String projectName) {
        if (facilityId == null || facilityId.isBlank()) {
            return -1;
        }
        try {
            List<Map<String, Object>> mustClauses = new ArrayList<>();
            mustClauses.add(Map.of("term", Map.of("Data.facilityId.keyword", facilityId)));
            if (tenantId != null && !tenantId.isBlank()) {
                mustClauses.add(Map.of("term", Map.of("Data.tenantId.keyword", tenantId)));
            }

            Map<String, Object> body = Map.of(
                    "query", Map.of("bool", Map.of("must", mustClauses)),
                    "script", Map.of(
                            "source", "ctx._source.Data.projectName = params.projectName",
                            "lang", "painless",
                            "params", Collections.singletonMap("projectName", projectName))
            );

            String uri = getBaseUrl() + "/" + INDEX_NAME + "/_update_by_query?refresh=true";
            HttpEntity<Object> entity = new HttpEntity<>(body, buildHeaders());
            ResponseEntity<Map> response = restTemplate.exchange(uri, HttpMethod.POST, entity, Map.class);

            Map<String, Object> respBody = response != null ? response.getBody() : null;
            int updated = 0;
            if (respBody != null && respBody.get("updated") instanceof Number) {
                updated = ((Number) respBody.get("updated")).intValue();
            }
            return updated;
        } catch (Exception e) {
            log.error("Unable to update projectName for facilityId={} tenantId={}: {}",
                    facilityId, tenantId, e.getMessage(), e);
            return -1;
        }
    }

    /**
     * Writes AMC fields onto the indexed document(s) for a facility via {@code _update_by_query},
     * touching nothing else. This is deliberately index-only: AMC data belongs to
     * amc-scheduler-service and is <em>not</em> persisted in the facility table, so it never goes
     * through the facility {@code _update} API (whose payload the persister would write to
     * {@code additional_details}) nor through the indexer Kafka topic.
     *
     * <p>Keys mapped to {@code null} are written as null, which is how a cleared AMC or a shortened
     * cadence clears the previously indexed value instead of leaving an orphan behind.
     *
     * <p>{@code _update_by_query} only touches documents that already exist: a facility that is not
     * indexed yet (e.g. not ONM-ready) is a no-op, reported as 0 updated.
     *
     * @return number of documents updated, or {@code -1} when the update call failed
     */
    public int updateAmcFieldsByFacilityId(String facilityId, String tenantId, Map<String, Object> amcFields) {
        if (facilityId == null || facilityId.isBlank()) {
            log.warn("Skipping AMC index update: facilityId is null or blank");
            return -1;
        }
        if (amcFields == null || amcFields.isEmpty()) {
            log.debug("Skipping AMC index update for facilityId={}: no fields supplied", facilityId);
            return 0;
        }
        try {
            List<Map<String, Object>> mustClauses = new ArrayList<>();
            mustClauses.add(Map.of("term", Map.of("Data.facilityId.keyword", facilityId)));
            if (tenantId != null && !tenantId.isBlank()) {
                mustClauses.add(Map.of("term", Map.of("Data.tenantId.keyword", tenantId)));
            }

            // HashMap, not Map.of: the values are intentionally nullable.
            Map<String, Object> params = new HashMap<>();
            params.put("amcFields", amcFields);

            Map<String, Object> body = Map.of(
                    "query", Map.of("bool", Map.of("must", mustClauses)),
                    "script", Map.of(
                            "source", "if (ctx._source.Data == null) { ctx._source.Data = [:]; } "
                                    + "for (entry in params.amcFields.entrySet()) { "
                                    + "ctx._source.Data[entry.getKey()] = entry.getValue(); }",
                            "lang", "painless",
                            "params", params)
            );

            String uri = getBaseUrl() + "/" + INDEX_NAME + "/_update_by_query?refresh=true";
            HttpEntity<Object> entity = new HttpEntity<>(body, buildHeaders());
            ResponseEntity<Map> response = restTemplate.exchange(uri, HttpMethod.POST, entity, Map.class);

            Map<String, Object> respBody = response != null ? response.getBody() : null;
            int updated = 0;
            if (respBody != null && respBody.get("updated") instanceof Number) {
                updated = ((Number) respBody.get("updated")).intValue();
            }
            if (updated == 0) {
                log.info("AMC index update matched no document for facilityId={} tenantId={} "
                        + "(facility not indexed yet?)", facilityId, tenantId);
            } else {
                log.info("Updated AMC fields on {} indexed document(s) for facilityId={}", updated, facilityId);
            }
            return updated;
        } catch (Exception e) {
            log.error("Unable to update AMC fields for facilityId={} tenantId={}: {}",
                    facilityId, tenantId, e.getMessage(), e);
            return -1;
        }
    }
}

