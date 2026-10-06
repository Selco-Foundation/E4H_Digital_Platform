package org.egov.amc.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.amc.service.ServiceRequestRepository;
import org.egov.amc.web.models.DecryptionRequest;
import org.egov.amc.web.models.DecryptionRequestWrapper;
import org.egov.amc.web.models.EncryptObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class EncryptionDecryptionUtil {

    private final ServiceRequestRepository serviceRequestRepository;
    private final ObjectMapper objectMapper;

    @Value("${egov.enc.host}")
    private String encServiceHost;

    @Value("${egov.enc.decrypt.endpoint}")
    private String encServiceDecryptPath;

    public EncryptionDecryptionUtil(
            ServiceRequestRepository serviceRequestRepository,
            @Qualifier("objectMapper") ObjectMapper objectMapper) {
        this.serviceRequestRepository = serviceRequestRepository;
        this.objectMapper = objectMapper;
    }

    public List<Map<String, EncryptObject>> decryptObject(DecryptionRequest request) {
        String uri = UriComponentsBuilder
                .fromUriString(encServiceHost)
                .path(encServiceDecryptPath)
                .toUriString();

        Object response = serviceRequestRepository.fetchEncServiceResult(new StringBuilder(uri), request);
        DecryptionRequestWrapper wrapper = objectMapper.convertValue(response, DecryptionRequestWrapper.class);
        return wrapper.getDecryptionRequests();
    }
}
