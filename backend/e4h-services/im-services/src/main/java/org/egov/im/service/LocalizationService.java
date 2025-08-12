package org.egov.im.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.im.config.IMConfiguration;
import org.egov.im.web.models.Incident;
import org.egov.im.web.models.IncidentRequestWrapper;
import org.egov.im.web.models.IndexView;
import org.egov.im.web.models.LocalizationResponse;
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

    public void enrichLocalizedFieldsForIndexing(IncidentRequestWrapper wrapper) {
        Incident incident = wrapper.getIncidentRequest().getIncident();
        RequestInfo requestInfo = wrapper.getIncidentRequest().getRequestInfo();

        String tenantId = incident.getTenantId();
        String stateTenant = tenantId.split("\\.")[0];
        String locale = "en_IN";

        String stateCode = "HEADER_TENANT_TENANTS_" + stateTenant.toUpperCase();
        String incidentTypeCode = "SERVICEDEFS." + incident.getIncidentType().toUpperCase();
        String incidentSubTypeCode = "SERVICEDEFS." + incident.getIncidentSubType().toUpperCase();

        String appStatusCode = Optional.ofNullable(incident.getApplicationStatus())
                .map(String::toUpperCase)
                .map(status -> "CS_COMMON_" + status)
                .orElse("");

        String tenantCode = "TENANT_TENANTS_" + tenantId.replace(".", "_").toUpperCase();
        String imCodes = String.join(",", incidentTypeCode, incidentSubTypeCode, appStatusCode);
        String commonCodes = tenantCode;

        LocalizationResponse stateTenantResponse = getLocalizationMessages(requestInfo, stateTenant, "rainmaker-" + stateTenant, locale, stateCode);
        LocalizationResponse imResponse = getLocalizationMessages(requestInfo, stateTenant, "rainmaker-im", locale, imCodes);
        LocalizationResponse commonResponse = getLocalizationMessages(requestInfo, stateTenant, "rainmaker-common", locale, commonCodes);

        IndexView indexView = wrapper.getIndexView();
        if (indexView == null) {
            indexView = new IndexView();
            wrapper.setIndexView(indexView);
        }

        indexView.setState(stateTenantResponse.getMessageByCode(stateCode));
        indexView.setIncidentTypeLocalized(imResponse.getMessageByCode(incidentTypeCode));
        indexView.setIncidentSubTypeLocalized(imResponse.getMessageByCode(incidentSubTypeCode));
        indexView.setApplicationStatusLocalized(imResponse.getMessageByCode(appStatusCode));
        indexView.setTenantIdLocalized(commonResponse.getMessageByCode(tenantCode));
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