package org.egov.rms.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.rms.config.RMSConfiguration;
import org.egov.rms.model.Alert;
import org.egov.rms.model.FacilityDetails;
import org.egov.rms.repository.AlertRepository;
import org.egov.rms.util.IncidentTypeMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Answers "is there already an open ticket for this facility?" the way im-services does when it
 * flags potential duplicates on create: same boundaryCode, incident type and sub-type, in an open
 * status. Matching on boundaryCode catches tickets raised from any source (HCR, UI), not only the
 * ones RMS created itself.
 *
 * When im-services cannot answer (facility boundaryCode unresolvable, call fails), falls back to the
 * eg_incident_v2 checks in {@link AlertRepository}, which only reliably see RMS-created tickets.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OpenIncidentChecker {

    private final RMSConfiguration config;
    private final RestTemplate restTemplate;
    private final FacilityServiceClient facilityServiceClient;
    private final AlertRepository alertRepository;

    /**
     * @return true when the alert's facility has an open ticket of the alert's own type and sub-type.
     */
    public boolean hasOpenTicket(Alert alert) {
        String incidentType = IncidentTypeMapper.toIncidentType(alert.getAlertType());
        String incidentSubType = IncidentTypeMapper.toIncidentSubType(alert.getAlertSubType());

        return countOpenIncidentsInIm(alert, incidentType, Collections.singleton(incidentSubType))
                .orElseGet(() -> alertRepository.hasOpenTicket(
                        alert.getFacilityId(), alert.getAlertType(), alert.getAlertSubType()));
    }

    /**
     * @return true when the alert's facility has an open ticket of the given type and any of the given sub-types.
     */
    public boolean hasOpenIncident(Alert alert, String incidentType, Set<String> incidentSubTypes) {
        return countOpenIncidentsInIm(alert, incidentType, incidentSubTypes)
                .orElseGet(() -> incidentSubTypes.stream().anyMatch(subType ->
                        alertRepository.hasOpenIncidentForRmsFacility(alert.getFacilityId(), incidentType, subType)));
    }

    /**
     * @return whether IM has a matching open incident, or empty when IM could not be asked.
     */
    private Optional<Boolean> countOpenIncidentsInIm(Alert alert, String incidentType, Set<String> incidentSubTypes) {
        String boundaryCode = resolveBoundaryCode(alert);
        if (!StringUtils.hasText(boundaryCode)) {
            log.warn("OPEN_INCIDENT_CHECK_FALLBACK alertId={} facilityId={} hfrId={} type={} subTypes={} cause=BOUNDARY_CODE_UNRESOLVED",
                    alert.getId(), alert.getFacilityId(), alert.getHfrId(), incidentType, incidentSubTypes);
            return Optional.empty();
        }

        URI uri = UriComponentsBuilder
                .fromUriString(config.getImServiceBaseUrl() + config.getImServiceCountEndpoint())
                .queryParam("tenantId", config.getDefaultTenantId())
                .queryParam("boundaryCode", boundaryCode)
                .queryParam("incidentType", incidentType)
                .queryParam("incidentSubType", incidentSubTypes.toArray())
                .queryParam("applicationStatus", IncidentTypeMapper.OPEN_STATUSES.toArray())
                .encode()
                .build()
                .toUri();
        Map<String, Object> body = Collections.singletonMap("RequestInfo", RequestInfo.builder().apiId("Rainmaker").build());

        try {
            JsonNode response = restTemplate.postForObject(uri, body, JsonNode.class);
            JsonNode count = response == null ? null : response.get("count");
            if (count == null || !count.isNumber()) {
                log.warn("OPEN_INCIDENT_CHECK_FALLBACK alertId={} boundaryCode={} type={} subTypes={} cause=COUNT_MISSING_IN_RESPONSE",
                        alert.getId(), boundaryCode, incidentType, incidentSubTypes);
                return Optional.empty();
            }
            boolean open = count.asInt() > 0;
            log.debug("Open incident check alertId={} boundaryCode={} type={} subTypes={} openCount={}",
                    alert.getId(), boundaryCode, incidentType, incidentSubTypes, count.asInt());
            return Optional.of(open);
        } catch (Exception e) {
            log.warn("OPEN_INCIDENT_CHECK_FALLBACK alertId={} boundaryCode={} type={} subTypes={} cause=IM_COUNT_FAILED reason={}",
                    alert.getId(), boundaryCode, incidentType, incidentSubTypes, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Looks the facility up by hfrId (as ticket creation does) and remembers the boundaryCode on the
     * alert so the several checks made for one alert share a single registry lookup.
     */
    private String resolveBoundaryCode(Alert alert) {
        if (StringUtils.hasText(alert.getBoundaryCode())) {
            return alert.getBoundaryCode();
        }
        try {
            FacilityDetails facility = facilityServiceClient.getFacilityByHfrId(alert.getHfrId(), config.getDefaultTenantId());
            if (facility != null && StringUtils.hasText(facility.getBoundaryCode())) {
                alert.setBoundaryCode(facility.getBoundaryCode().trim());
            }
        } catch (Exception e) {
            log.warn("Could not resolve facility for alert {} (hfrId={}): {}", alert.getId(), alert.getHfrId(), e.getMessage());
        }
        return alert.getBoundaryCode();
    }
}
