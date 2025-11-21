package org.egov.im.service;

import org.egov.common.contract.request.RequestInfo;
import org.egov.im.config.IMConfiguration;
import org.egov.im.repository.IdGenRepository;
import org.egov.im.repository.ServiceRequestRepository;
import org.egov.im.util.IMUtils;
import org.egov.im.web.models.*;
import org.egov.im.web.models.Idgen.IdResponse;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.stream.Collectors;

import static org.egov.im.util.IMConstants.USERTYPE_CITIZEN;

@Slf4j
@org.springframework.stereotype.Service
public class EnrichmentService {


    private IMUtils utils;

    private IdGenRepository idGenRepository;

    private IMConfiguration config;

    private UserService userService;

    private LocalizationService localizationService;

    private NotificationService notificationService;

    private WorkflowService workflowService;

    private SLAService slaService;

    private RestTemplate restTemplate;

    @Autowired
    public EnrichmentService(IMUtils utils, IdGenRepository idGenRepository, IMConfiguration config, UserService userService, LocalizationService localizationService, NotificationService notificationService, @Lazy WorkflowService workflowService, SLAService slaService, RestTemplate restTemplate) {
        this.utils = utils;
        this.idGenRepository = idGenRepository;
        this.config = config;
        this.userService = userService;
        this.localizationService = localizationService;
        this.notificationService = notificationService;
        this.workflowService = workflowService;
        this.slaService = slaService;
        this.restTemplate = restTemplate;
    }


    /**
     * Enriches the create request with auditDetails. uuids and custom ids from idGen service
     *
     * @param serviceRequest The create request
     */
    public void enrichCreateRequest(IncidentRequest incidentRequest) {
        log.info("EnrichmentService::Enriching create request");

        RequestInfo requestInfo = incidentRequest.getRequestInfo();
        Incident incident = incidentRequest.getIncident();
        Workflow workflow = incidentRequest.getWorkflow();
        String tenantId = incident.getTenantId();

        incident.setAccountId(incidentRequest.getIncident().getReporter().getUuid());
        incident.setReporterTenant(incidentRequest.getIncident().getReporter().getTenantId());

        incident.setBlock(toCamelCase(incident.getBlock()));
        incident.setDistrict(toCamelCase(incident.getDistrict()));

        userService.callUserService(incidentRequest);

        if (incident.getReporterTenant().equalsIgnoreCase(incident.getTenantId().split("\\.")[0]))
            incident.setReporterType("CRM");
        else
            incident.setReporterType("HCR");

        AuditDetails auditDetails = utils.getAuditDetails(requestInfo.getUserInfo().getUuid(), incident, true);

        incident.setAuditDetails(auditDetails);
        incident.setId(UUID.randomUUID().toString());

        if (workflow.getVerificationDocuments() != null) {
            workflow.getVerificationDocuments().forEach(document -> {
                document.setId(UUID.randomUUID().toString());
            });
        }

        List<String> customIds = getIdList(requestInfo, tenantId, config.getServiceRequestIdGenName(), config.getServiceRequestIdGenFormat(), 1);

        incident.setIncidentId(customIds.get(0));

        // Enrich facilityId from facility registry using boundaryCode from request (only if not already set)
        if (incident.getFacilityId() == null && incident.getBoundaryCode() != null) {
            enrichFacilityDetailsFromBoundaryCode(incidentRequest);
        }

    }


    /**
     * Enriches the update request (updates the lastModifiedTime in auditDetails0
     *
     * @param serviceRequest The update request
     */
    public void enrichUpdateRequest(IncidentRequest incidentRequest) {
        log.info("EnrichmentService::Enriching incident update request");

        RequestInfo requestInfo = incidentRequest.getRequestInfo();
        Incident incident = incidentRequest.getIncident();
        AuditDetails auditDetails = utils.getAuditDetails(requestInfo.getUserInfo().getUuid(), incident, false);
        incident.setBlock(toCamelCase(incident.getBlock()));
        incident.setDistrict(toCamelCase(incident.getDistrict()));
        incident.setAuditDetails(auditDetails);

        userService.callUserService(incidentRequest);

        // Enrich facilityId from facility registry using boundaryCode from request (only if not already set)
        if (incident.getFacilityId() == null && incident.getBoundaryCode() != null) {
            enrichFacilityDetailsFromBoundaryCode(incidentRequest);
        }
    }

    /**
     * Enriches the search criteria in case of default search and enriches the userIds from mobileNumber in case of seach based on mobileNumber.
     * Also sets the default limit and offset if none is provided
     *
     * @param requestInfo
     * @param criteria
     */
    public void enrichSearchRequest(RequestInfo requestInfo, RequestSearchCriteria criteria) {
        log.info("EnrichmentService::Enriching incident search request");

        if (criteria.isEmpty() && requestInfo.getUserInfo().getType().equalsIgnoreCase(USERTYPE_CITIZEN)) {
            String citizenMobileNumber = requestInfo.getUserInfo().getUserName();
            criteria.setMobileNumber(citizenMobileNumber);
        }

        criteria.setAccountId(requestInfo.getUserInfo().getUuid());

        String tenantId = (criteria.getTenantId() != null) ? criteria.getTenantId() : requestInfo.getUserInfo().getTenantId();

        if (criteria.getMobileNumber() != null) {
            userService.enrichUserIds(tenantId, criteria);
        }

        if (criteria.getLimit() == null)
            criteria.setLimit(config.getDefaultLimit());

        if (criteria.getOffset() == null)
            criteria.setOffset(config.getDefaultOffset());

        if (criteria.getLimit() != null && criteria.getLimit() > config.getMaxLimit())
            criteria.setLimit(config.getMaxLimit());

    }

    public void enrichFieldsForIndexing(IncidentRequestWrapper wrapper) {
        log.info("EnrichmentService::Enriching incident fields for indexing");
        IncidentRequest incidentRequest = wrapper.getIncidentRequest();

        // Enrich localized fields first (will populate IndexView inside the wrapper)
        localizationService.enrichLocalizedFieldsForIndexing(wrapper);

        // Ensure IndexView is initialized and reused (not replaced)
        IndexView indexView = wrapper.getIndexView();
        if (indexView == null) {
            indexView = new IndexView();
            wrapper.setIndexView(indexView);
        }

        // Fetch HCR and Vendor details
        Map<String, String> hcrDetails = notificationService.getHRMSEmployeeForIndexing(incidentRequest, null, "COMPLAINANT");
        Map<String, String> vendorDetails = notificationService.getHRMSEmployeeForIndexing(incidentRequest, null, "COMPLAINT_RESOLVER");

        // Get details of the user who last modified (last action)
        String lastActionTakenByUser = wrapper.getIncidentRequest().getRequestInfo().getUserInfo().getName();

        // Set fields in IndexView if values exist
        Optional.ofNullable(hcrDetails.get("employeeUserName")).ifPresent(indexView::setNinHfrId);
        Optional.ofNullable(vendorDetails.get("employeeUserName")).ifPresent(indexView::setMappedVendorUserName);
        Optional.ofNullable(vendorDetails.get("employeeName")).ifPresent(indexView::setMappedVendorName);
        indexView.setLastActionTakenBy(lastActionTakenByUser);
        indexView.setComments(
                (wrapper.getIncidentRequest().getWorkflow().getComments() != null &&
                        !wrapper.getIncidentRequest().getWorkflow().getComments().isEmpty())
                        ? wrapper.getIncidentRequest().getWorkflow().getComments()
                        : wrapper.getIncidentRequest().getIncident().getComments()
        );

        if (wrapper.getIncidentRequest().getWorkflow().getSendBackReason() != null) {
            SendBackReason reason = wrapper.getIncidentRequest().getWorkflow().getSendBackReason();
            indexView.setSendBackReason(reason.getReason());
            indexView.setSendBackSubReason(reason.getSubReason());
        }

        Object additionalDetailObj = wrapper.getIncidentRequest().getIncident().getAdditionalDetail();

        if (additionalDetailObj instanceof Map) {
            Map<String, Object> additionalDetail = (Map<String, Object>) additionalDetailObj;

            Object rejectReasonObj = additionalDetail.get("rejectReason");

            if (rejectReasonObj instanceof List) {
                List<?> rejectReasons = (List<?>) rejectReasonObj;

                if (!rejectReasons.isEmpty()) {
                    indexView.setLatestRejectReason(rejectReasons.get(rejectReasons.size() - 1).toString());
                }
            }
        }

        // Enrich boundary object for indexing only (not persisted to database)
        if (incidentRequest.getIncident().getBoundaryCode() != null) {
            Boundary boundary = enrichBoundaryFromBoundaryCode(incidentRequest);
            indexView.setBoundary(boundary);
        }

    }

    /**
     * Returns a list of numbers generated from idgen
     *
     * @param requestInfo RequestInfo from the request
     * @param tenantId    tenantId of the city
     * @param idKey       code of the field defined in application properties for which ids are generated for
     * @param idformat    format in which ids are to be generated
     * @param count       Number of ids to be generated
     * @return List of ids generated using idGen service
     */
    private List<String> getIdList(RequestInfo requestInfo, String tenantId, String idKey,
                                   String idformat, int count) {
        List<IdResponse> idResponses = idGenRepository.getId(requestInfo, tenantId, idKey, idformat, count).getIdResponses();

        if (CollectionUtils.isEmpty(idResponses))
            throw new CustomException("IDGEN ERROR", "No ids returned from idgen Service");

        return idResponses.stream()
                .map(IdResponse::getId).collect(Collectors.toList());
    }

    public static String toCamelCase(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        str = new String(str.trim());
        StringBuilder converted = new StringBuilder();

        boolean convertNext = true;

        for (char ch : str.toCharArray()) {
            if (Character.isSpaceChar(ch)) {
                convertNext = true;
            } else if (convertNext) {
                ch = Character.toTitleCase(ch);
                convertNext = false;
            } else {
                ch = Character.toLowerCase(ch);
            }
            converted.append(ch);
        }
        return converted.toString();
    }

    /**
     * Enriches boundary object from boundary service using boundaryCode from incident request
     * @param incidentRequest The incident request containing boundaryCode
     */
    private Boundary enrichBoundaryFromBoundaryCode(IncidentRequest incidentRequest) {
        Incident incident = incidentRequest.getIncident();
        RequestInfo requestInfo = incidentRequest.getRequestInfo();
        String boundaryCode = incident.getBoundaryCode();
        String tenantId = incident.getTenantId();

        if (boundaryCode == null || boundaryCode.isEmpty()) {
            log.debug("No boundaryCode provided in incident request, skipping boundary enrichment");
            return null;
        }

        try {
            String url = UriComponentsBuilder.fromHttpUrl(config.getBoundaryHost() + config.getBoundarySearchPath())
                    .queryParam("tenantId", tenantId != null ? tenantId.split("\\.")[0] : "")
                    .queryParam("codes", boundaryCode)
                    .queryParam("includeParents", "true")
                    .queryParam("boundaryType", "Facility")
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("RequestInfo", requestInfo);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> responseMap = responseEntity.getBody();

            if (responseMap != null) {
                List<Map<String, Object>> boundaryRelationships = (List<Map<String, Object>>) responseMap.get("TenantBoundary");

                if (boundaryRelationships != null && !boundaryRelationships.isEmpty()) {
                    // Get the first tenant boundary entry
                    Map<String, Object> tenantBoundary = boundaryRelationships.get(0);
                    List<Map<String, Object>> boundaries = (List<Map<String, Object>>) tenantBoundary.get("boundary");

                    if (boundaries != null && !boundaries.isEmpty()) {
                        // Build boundary hierarchy
                        Boundary boundary = buildBoundaryHierarchy(boundaries);
                        
                        if (boundary != null) {
                            log.debug("Enriched boundary for indexing for boundaryCode: {}", boundaryCode);
                            return boundary;
                        }
                    } else {
                        log.warn("No boundaries found in response for boundaryCode: {}", boundaryCode);
                    }
                } else {
                    log.warn("No boundary relationships found for boundaryCode: {}", boundaryCode);
                }
            }
        } catch (Exception e) {
            log.error("Error enriching boundary details for boundaryCode: {}", boundaryCode, e);
        }
        
        return null;
    }

    /**
     * Builds boundary hierarchy from boundary service response
     * @param boundaries List of boundary objects from boundary service
     * @return Boundary object with hierarchy codes
     */
    private Boundary buildBoundaryHierarchy(List<Map<String, Object>> boundaries) {
        Boundary boundary = new Boundary();
        
        for (Map<String, Object> boundaryItem : boundaries) {
            String code = (String) boundaryItem.get("code");
            String boundaryType = (String) boundaryItem.get("boundaryType");
            
            if (code != null && boundaryType != null) {
                switch (boundaryType.toLowerCase()) {
                    case "country":
                        boundary.setCountryCode(code);
                        break;
                    case "state":
                        boundary.setStateCode(code);
                        break;
                    case "district":
                        boundary.setDistrictCode(code);
                        break;
                    case "block":
                        boundary.setBlockCode(code);
                        break;
                    case "facility":
                        boundary.setFacilityCode(code);
                        break;
                    default:
                        log.debug("Unknown boundaryType: {}", boundaryType);
                }
            }
            
            // Recursively process children if present
            List<Map<String, Object>> children = (List<Map<String, Object>>) boundaryItem.get("children");
            if (children != null && !children.isEmpty()) {
                Boundary childBoundary = buildBoundaryHierarchy(children);
                // Merge child boundary codes into parent
                if (childBoundary.getCountryCode() != null) boundary.setCountryCode(childBoundary.getCountryCode());
                if (childBoundary.getStateCode() != null) boundary.setStateCode(childBoundary.getStateCode());
                if (childBoundary.getDistrictCode() != null) boundary.setDistrictCode(childBoundary.getDistrictCode());
                if (childBoundary.getBlockCode() != null) boundary.setBlockCode(childBoundary.getBlockCode());
                if (childBoundary.getFacilityCode() != null) boundary.setFacilityCode(childBoundary.getFacilityCode());
            }
        }
        
        return boundary;
    }

    /**
     * Enriches facilityId from facility registry search API using boundaryCode from incident request
     * @param incidentRequest The incident request containing boundaryCode
     */
    private void enrichFacilityDetailsFromBoundaryCode(IncidentRequest incidentRequest) {
        Incident incident = incidentRequest.getIncident();
        String boundaryCode = incident.getBoundaryCode();
        String tenantId = incident.getTenantId();

        if (boundaryCode == null || boundaryCode.isEmpty()) {
            log.debug("No boundaryCode provided in incident request, skipping facility enrichment");
            return;
        }

        try {
            String url = UriComponentsBuilder.fromHttpUrl(config.getFacilityHost() + config.getFacilitySearchPath())
                    .queryParam("tenantId", tenantId != null ? tenantId : "")
                    .queryParam("boundaryCode", boundaryCode)
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            HttpEntity<Object> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> responseMap = responseEntity.getBody();

            if (responseMap != null) {
                List<Map<String, Object>> facilities = (List<Map<String, Object>>) responseMap.get("facilities");

                if (facilities != null && !facilities.isEmpty()) {
                    Map<String, Object> facility = facilities.get(0);
                    String facilityId = (String) facility.get("facility_id");

                    if (facilityId != null) {
                        incident.setFacilityId(facilityId);
                        log.debug("Enriched facilityId: {} for boundaryCode: {}", facilityId, boundaryCode);
                    } else {
                        log.warn("Facility found but facility_id is null for boundaryCode: {}", boundaryCode);
                    }
                } else {
                    log.warn("No facility found for boundaryCode: {}", boundaryCode);
                }
            }
        } catch (Exception e) {
            log.error("Error enriching facility details for boundaryCode: {}", boundaryCode, e);
        }
    }

    public void enrichFieldsForAuditIndexing(IncidentRequestWrapper wrapper, String startingStatus) {
        log.info("EnrichmentService::Enriching incident fields for audit indexing");
        // Ensure IndexView is initialized
        IndexView indexView = wrapper.getIndexView();
        if (indexView == null) {
            indexView = new IndexView();
            wrapper.setIndexView(indexView);
        }

        indexView.setUuid(UUID.randomUUID().toString());
        indexView.setStartingStatus(startingStatus);
        indexView.setEndingStatus(wrapper.getIncidentRequest().getIncident().getApplicationStatus());

        localizationService.enrichLocalizedApplicationStatuses(wrapper, startingStatus);

        // get array of filestore download links
        String tenantId = wrapper.getIncidentRequest().getIncident().getTenantId();
        List<Document> verificationDocuments = wrapper.getIncidentRequest().getWorkflow().getVerificationDocuments();

        String fileStoreUrls = verificationDocuments == null ? "" :
                verificationDocuments.stream()
                        .filter(doc -> doc.getFileStoreId() != null)
                        .filter(doc -> !"HLS".equalsIgnoreCase(doc.getDocumentType()))
                        .map(doc -> String.format("%s?tenantId=%s&fileStoreId=%s",
                                config.getFileStoreDownloadEndpoint(), tenantId, doc.getFileStoreId()))
                        .collect(Collectors.joining(" , "));

        indexView.setDocumentUrls(fileStoreUrls);
    }
}
