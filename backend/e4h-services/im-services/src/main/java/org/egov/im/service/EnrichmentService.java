package org.egov.im.service;

import com.jayway.jsonpath.JsonPath;
import org.apache.commons.lang.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.im.config.IMConfiguration;
import org.egov.im.repository.IdGenRepository;
import org.egov.im.repository.ServiceRequestRepository;
import org.egov.im.util.HRMSUtil;
import org.egov.im.util.IMUtils;
import org.egov.im.util.MDMSUtils;
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
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static org.egov.im.util.IMConstants.*;

@Slf4j
@org.springframework.stereotype.Service
public class EnrichmentService {


    private IMUtils utils;

    private final HRMSUtil hrmsUtil;

    private MDMSUtils mdmsUtils;

    private IdGenRepository idGenRepository;

    private IMConfiguration config;

    private UserService userService;

    private LocalizationService localizationService;

    private NotificationService notificationService;

    private WorkflowService workflowService;

    private SLAService slaService;

    private RestTemplate restTemplate;

    @Autowired
    public EnrichmentService(
            IMUtils utils, HRMSUtil hrmsUtil, MDMSUtils mdmsUtils, IdGenRepository idGenRepository,
            IMConfiguration config, UserService userService, LocalizationService localizationService,
            NotificationService notificationService, @Lazy WorkflowService workflowService,
            SLAService slaService, RestTemplate restTemplate) {
        this.utils = utils;
        this.hrmsUtil = hrmsUtil;
        this.mdmsUtils = mdmsUtils;
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
     * @param incidentRequest The create request
     * @param boundary The boundary object
     */
    public void enrichCreateRequest(IncidentRequest incidentRequest, Boundary boundary) {
        log.info("EnrichmentService::Enriching create request");

        RequestInfo requestInfo = incidentRequest.getRequestInfo();
        Incident incident = incidentRequest.getIncident();
        Workflow workflow = incidentRequest.getWorkflow();
        String tenantId = incident.getTenantId();

        incident.setAccountId(incidentRequest.getIncident().getReporter().getUuid());
        incident.setReporterTenant(incidentRequest.getIncident().getReporter().getTenantId());

        localizationService.enrichLocalizedDistrictAndBlockNames(incidentRequest, boundary);

        userService.callUserService(incidentRequest);

        if (StringUtils.isEmpty(incident.getReporterType())) {
            List<org.egov.common.contract.request.Role> userRoles = Optional.ofNullable(requestInfo.getUserInfo())
                    .map(org.egov.common.contract.request.User::getRoles)
                    .orElse(new ArrayList<>());
            if (userRoles.stream().anyMatch(role -> role.getCode().equalsIgnoreCase("RMS"))) {
                incident.setReporterType("RMS");
            } else if (userRoles.stream().anyMatch(role -> role.getCode().equalsIgnoreCase("COMPLAINT_ASSESSOR"))) {
                incident.setReporterType("CRM");
            } else {
                incident.setReporterType("HCR");
            }
        }

        AuditDetails auditDetails = utils.getAuditDetails(requestInfo.getUserInfo().getUuid(), incident, true);

        incident.setAuditDetails(auditDetails);
        incident.setId(UUID.randomUUID().toString());

        if (workflow.getVerificationDocuments() != null) {
            workflow.getVerificationDocuments().forEach(document -> {
                document.setId(UUID.randomUUID().toString());
            });
        }

        // Enrich facilityId from facility registry using boundaryCode from request (only if not already set)
        enrichFacilityDetailsFromBoundaryCode(incidentRequest);

        List<String> customIds = getIdList(
                requestInfo, tenantId, "", getIdGenIncidentIdFormat(incidentRequest, boundary), 1
        );
        incident.setIncidentId(customIds.get(0));
    }

    private String getIdGenIncidentIdFormat(IncidentRequest incidentRequest, Boundary boundary) {
        Incident incident = incidentRequest.getIncident();
        RequestInfo requestInfo = incidentRequest.getRequestInfo();

        String idGenIncidentIdFormat = config.getServiceRequestIdGenFormat();

        StringBuilder hcrUserSearchUri = hrmsUtil.getHRMSURI(null, incident.getTenantId(), "COMPLAINANT", incident.getBoundaryCode());
        hcrUserSearchUri.append("&searchOnlyInBoundary=");
        hcrUserSearchUri.append(true);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("RequestInfo", requestInfo);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(
                hcrUserSearchUri.toString(),
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<>() {}
        );
        Map<String, Object> responseMap = responseEntity.getBody();
        String hcrUser = Optional.ofNullable(safeJsonPathRead(responseMap, "$.Employees[0].code"))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .orElseThrow(() -> new CustomException("HCR_NOT_FOUND", "HCR not found for given boundary"));

        Object mdmsResponse = mdmsUtils.fetchMDMSData(requestInfo, incident.getTenantId(), "common-masters", List.of("StateInfo"), null);
        List<?> stateInfoList = Optional.ofNullable(safeJsonPathRead(mdmsResponse, "$.MdmsRes.common-masters.StateInfo"))
                .filter(List.class::isInstance)
                .map(List.class::cast)
                .orElseThrow(() -> new CustomException("STATE_INFO_MISSING", "Cannot fetch StateInfo for tenant " + incident.getTenantId()));
        String stateCode = stateInfoList.stream()
                .map(Map.class::cast)
                .filter(item -> boundary.getStateCode().equals(item.get("boundaryCode")))
                .map(item -> item.get("code"))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .findFirst()
                .orElseThrow(() -> new CustomException("STATE_CODE_NOT_FOUND", "State code not found for boundary " + boundary.getStateCode()));

        Map<String, String> values = Map.of(
                "STATE_CODE", stateCode,
                "HCR_USERNAME", hcrUser,
                "FACILITY_ID", incident.getFacilityId().replace("/", "_")
        );

        for (Map.Entry<String, String> entry : values.entrySet()) {
            idGenIncidentIdFormat = idGenIncidentIdFormat.replaceAll(
                    "\\[" + entry.getKey() + "]",
                    Matcher.quoteReplacement(entry.getValue())
            );
        }

        return idGenIncidentIdFormat;
    }

    private Object safeJsonPathRead(Object json, String path) {
        try {
            return JsonPath.read(json, path);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Enriches the update request (updates the lastModifiedTime in auditDetails0
     *
     * @param incidentRequest The update request
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

    public void enrichFieldsForIndexing(IncidentRequestWrapper wrapper, Boundary boundary) {
        log.info("EnrichmentService::Enriching incident fields for indexing");
        IncidentRequest incidentRequest = wrapper.getIncidentRequest();

        // Ensure IndexView is initialized and reused (not replaced)
        IndexView indexView = wrapper.getIndexView();
        if (indexView == null) {
            indexView = new IndexView();
            wrapper.setIndexView(indexView);
        }

        // Fetch HCR and Vendor details
        Map<String, String> hcrDetails = notificationService.getHRMSEmployeeForIndexing(incidentRequest, null, ROLE_COMPLAINANT);
        Map<String, String> vendorDetails = notificationService.getHRMSEmployeeForIndexing(incidentRequest, null, ROLE_COMPLAINT_RESOLVER);

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
        if (boundary != null) {
            indexView.setBoundary(boundary);
        }

        // Enrich localized fields first (will populate IndexView inside the wrapper)
        localizationService.enrichLocalizedFieldsForIndexing(wrapper);
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
     * Enriches facilityId from facility registry search API using boundaryCode from incident request
     * @param incidentRequest The incident request containing boundaryCode
     */
    private void enrichFacilityDetailsFromBoundaryCode(IncidentRequest incidentRequest) {
        Incident incident = incidentRequest.getIncident();
        String boundaryCode = incident.getBoundaryCode();
        String tenantId = incident.getTenantId();

        if (boundaryCode == null || boundaryCode.isEmpty()) {
            log.error("No boundaryCode provided in incident request, skipping facility enrichment");
            throw new CustomException("BOUNDARY_CODE_MISSING", "Boundary code not provided to enrich facility details");
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

                        // Set phcType to tenantId
                        incident.setPhcType(tenantId);

                        // Set phcSubType to facilityType from facility response
                        String facilityType = (String) facility.get("facility_type");
                        if (facilityType == null) {
                            facilityType = (String) facility.get("type");
                        }
                        if (facilityType != null) {
                            incident.setPhcSubType(facilityType);
                        }

                        log.debug("Enriched facilityId: {}, phcType: {}, phcSubType: {} for boundaryCode: {}",
                                facilityId, tenantId, facilityType, boundaryCode);
                    } else {
                        log.error("Facility found but facility_id is null for boundaryCode: {}", boundaryCode);
                        throw new CustomException("FACILITY_NOT_FOUND", "Cannot find facility");
                    }
                } else {
                    log.warn("No facility found for boundaryCode: {}", boundaryCode);
                    throw new CustomException("FACILITY_NOT_FOUND", "Cannot find facility");
                }
            }
        } catch (Exception e) {
            log.error("Error enriching facility details for boundaryCode: {}", boundaryCode, e);
            throw new CustomException("FACILITY_NOT_FOUND", "Cannot find facility");
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
