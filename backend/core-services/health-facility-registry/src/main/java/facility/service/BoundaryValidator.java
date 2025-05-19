package facility.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import facility.repository.ServiceRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class BoundaryValidator {

    private final ServiceRequestRepository serviceRequestRepository;
    private final ObjectMapper mapper;

    @Value("${egov.boundary.host}")
    private String boundaryHost;

    @Value("${egov.boundary.path:/boundary-service/boundary/_search}")
    private String boundaryPath;

    public void validateBoundary(Object boundaryCode, String tenantId, RequestInfo requestInfo) {
        if (boundaryCode == null || boundaryCode.toString().isBlank()) {
            throw new IllegalArgumentException("boundaryCode is required for facility");
        }

        String code = boundaryCode.toString();
        String uri = UriComponentsBuilder.fromUriString(boundaryHost)
                .path(boundaryPath)
                .queryParam("tenantId", tenantId)
                .queryParam("codes", code)
                .toUriString();
        Map<String, Object> requestBody = Map.of("RequestInfo", requestInfo);

        try {
            Object rawResponse = serviceRequestRepository.fetchResult(new StringBuilder(uri), requestBody);
            Map<String, Object> response = mapper.convertValue(rawResponse, new TypeReference<>() {});
            validateResponse(code, response);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error validating boundaryCode: " + code, e);
        }
    }

    private void validateResponse(String code, Map<String, Object> response) {
        if (response == null || !response.containsKey("Boundary")) {
            throw new IllegalArgumentException("Boundary response is missing 'Boundary' field");
        }

        Object boundaries = response.get("Boundary");
        if (!(boundaries instanceof List<?> list) || list.isEmpty()) {
            throw new IllegalArgumentException("Invalid or empty 'Boundary' response for code: " + code);
        }
    }
}
