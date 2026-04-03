package org.egov.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.repository.ServiceRequestRepository;
import org.egov.web.models.DecryptionRequest;
import org.egov.web.models.DecryptionRequestWrapper;
import org.egov.web.models.EncryptObject;
import org.egov.web.models.EncryptionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class EncryptionDecryptionUtilV2 {
    private final ServiceRequestRepository serviceRequestRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Value("${egov.enc.host}")
    private String encServiceHost;

    // Path to the encrypt endpoint
    @Value("${egov.enc.encrypt.endpoint}")
    private String encServiceEncryptPath;

    // Path to the decrypt endpoint
    @Value("${egov.enc.decrypt.endpoint}")
    private String encServiceDecryptPath;

    public EncryptionDecryptionUtilV2(ServiceRequestRepository serviceRequestRepository) {
        this.serviceRequestRepository = serviceRequestRepository;
    }

    public List<Map<String, EncryptObject>> encryptObject(EncryptionRequest encryptionRequest) {
        String uri = UriComponentsBuilder
            .fromUriString(encServiceHost)
            .path(encServiceEncryptPath)
            .toUriString();

        Object response = serviceRequestRepository.fetchEncServiceResult(new StringBuilder(uri), encryptionRequest);
//            String jsonResponse = objectMapper.writeValueAsString(response);
        return objectMapper.convertValue(response, new TypeReference<List<Map<String, EncryptObject>>>() {});
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
