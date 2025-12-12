package org.egov.im.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.im.config.IMConfiguration;
import org.egov.im.web.models.*;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalizationService {

    private final RestTemplate restTemplate;
    private final IMConfiguration config;

    public LocalizationResponse getLocalizationMessages(RequestInfo requestInfo, String stateTenant, String module, String locale, String codes) {
        String baseUrl = config.getLocalizationHost() + config.getLocalizationContextPath() + config.getLocalizationSearchEndpoint();

        StringBuilder urlBuilder = new StringBuilder(baseUrl);
        urlBuilder.append("?tenantId=").append(stateTenant);
        urlBuilder.append("&module=").append(module);
        urlBuilder.append("&locale=").append(locale);
        urlBuilder.append("&codes=").append(codes);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> requestEntity = new HttpEntity<>(Collections.singletonMap("RequestInfo", requestInfo), headers);

        try {
            ResponseEntity<LocalizationResponse> responseEntity = restTemplate.exchange(
                    urlBuilder.toString(),
                    HttpMethod.POST,
                    requestEntity,
                    LocalizationResponse.class
            );
            return responseEntity.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch localization messages for codes: {}, error: {}", codes, e.getMessage(), e);
            return new LocalizationResponse(); // return empty response object to avoid NPE
        }
    }

    public void enrichLocalizedDistrictAndBlockNames(IncidentRequest incidentRequest, Boundary boundary) {
        Incident incident = incidentRequest.getIncident();
        String tenantId = incident.getTenantId();
        String locale = "en_IN";

        String districtCode = "Boundary_" + boundary.getDistrictCode();
        String blockCode = "Boundary_" + boundary.getBlockCode();
        String boundaryCodes = String.join(",", districtCode, blockCode);

        LocalizationResponse boundaryResponse = getLocalizationMessages(incidentRequest.getRequestInfo(), tenantId, "rainmaker-in", locale, boundaryCodes);

        incident.setDistrict(boundaryResponse.getMessageByCode(districtCode));
        incident.setBlock(boundaryResponse.getMessageByCode(blockCode));
    }

    public void enrichLocalizedFieldsForIndexing(IncidentRequestWrapper wrapper) {
        Incident incident = wrapper.getIncidentRequest().getIncident();
        RequestInfo requestInfo = wrapper.getIncidentRequest().getRequestInfo();
        IndexView indexView = wrapper.getIndexView();

        String tenantId = incident.getTenantId();
        String stateTenant = tenantId.split("\\.")[0];
        String locale = "en_IN";

        String stateCode = "Boundary_" + indexView.getBoundary().getStateCode();
        String facilityCode = "Boundary_" + indexView.getBoundary().getFacilityCode();
        String incidentTypeCode = "SERVICEDEFS." + incident.getIncidentType().toUpperCase();
        String incidentSubTypeCode = "SERVICEDEFS." + incident.getIncidentSubType().toUpperCase();

        String appStatusCode = Optional.ofNullable(incident.getApplicationStatus())
                .map(String::toUpperCase)
                .map(status -> "CS_COMMON_" + status)
                .orElse("");

        String imCodes = String.join(",", incidentTypeCode, incidentSubTypeCode, appStatusCode);
        String boundaryCodes = String.join(",", stateCode, facilityCode);

        LocalizationResponse boundaryResponse = getLocalizationMessages(requestInfo, stateTenant, "rainmaker-in", locale, boundaryCodes);
        LocalizationResponse imResponse = getLocalizationMessages(requestInfo, stateTenant, "rainmaker-im", locale, imCodes);

        indexView.setState(boundaryResponse.getMessageByCode(stateCode));
        indexView.setIncidentTypeLocalized(imResponse.getMessageByCode(incidentTypeCode));
        indexView.setIncidentSubTypeLocalized(imResponse.getMessageByCode(incidentSubTypeCode));
        indexView.setApplicationStatusLocalized(imResponse.getMessageByCode(appStatusCode));
        indexView.setTenantIdLocalized(boundaryResponse.getMessageByCode(facilityCode));
    }

    public void enrichLocalizedApplicationStatuses(IncidentRequestWrapper wrapper,String startingStatus) {
        Incident incident = wrapper.getIncidentRequest().getIncident();
        RequestInfo requestInfo = wrapper.getIncidentRequest().getRequestInfo();

        String tenantId = incident.getTenantId();
        String stateTenant = tenantId.split("\\.")[0];
        String locale = "en_IN";

        String startingStatusCode = Optional.ofNullable(startingStatus)
                .map(String::toUpperCase)
                .map(status -> "CS_COMMON_" + status)
                .orElse("");

        String endingStatusCode = Optional.ofNullable(wrapper.getIncidentRequest().getIncident().getApplicationStatus())
                .map(String::toUpperCase)
                .map(status -> "CS_COMMON_" + status)
                .orElse("");

        String imCodes = String.join(",", startingStatusCode, endingStatusCode);

        LocalizationResponse imResponse = getLocalizationMessages(requestInfo, stateTenant, "rainmaker-im", locale, imCodes);

        wrapper.getIndexView().setStartingStatusLocalized(imResponse.getMessageByCode(startingStatusCode));
        wrapper.getIndexView().setEndingStatusLocalized(imResponse.getMessageByCode(endingStatusCode));

    }



}