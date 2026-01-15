package org.egov.im.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.im.web.models.ProcessingContext;
import org.egov.im.web.models.storage.StorageResponse;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Repository;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Repository
@Slf4j
public class ServiceRequestRepository {

    private final ObjectMapper mapper;
    private final RestTemplate restTemplate;

    public Object fetchResult(StringBuilder uri, Object request) {
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        Object response = null;
        try {
            response = restTemplate.postForObject(uri.toString(), request, Map.class);
        } catch (HttpClientErrorException e) {
            log.error("HTTP client error during service call: ", e);
            throw new ServiceCallException(e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            log.error("HTTP server error during service call: ", e);
            throw new ServiceCallException("Server error while fetching from service: " + e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            log.error("Network error during service call: ", e);
            throw new ServiceCallException("Network error while fetching from service: " + e.getMessage());
        } catch (RestClientException e) {
            log.error("Error during service call: ", e);
            throw new ServiceCallException("Error while fetching from service: " + e.getMessage());
        }
        return response;
    }

    public StorageResponse uploadFiles(List<MultipartFile> files,
                                                         ProcessingContext context,
                                                         String url) throws IOException {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        for (MultipartFile file : files) {
            try (var inputStream = file.getInputStream()) {
                ByteArrayResource fileResource = new ByteArrayResource(inputStream.readAllBytes()) {
                    @Override
                    public String getFilename() {
                        return file.getOriginalFilename();
                    }
                };
                body.add("file", fileResource);
            }
        }
        body.add("tenantId", context.getTenantId());
        body.add("module", context.getModule());
        body.add("tag", context.getTag());
        body.add("requestInfo", context.getRequestInfo());
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<StorageResponse> responseEntity =
                    restTemplate.exchange(url, HttpMethod.POST, requestEntity, StorageResponse.class);
            if (responseEntity.getStatusCode() != HttpStatus.CREATED) {
                throw new ServiceCallException(String.format("File upload failed with status: %s",
                        responseEntity.getStatusCode()));
            }
            return responseEntity.getBody();
        } catch (HttpClientErrorException e) {
            log.error("File upload failed: {}", e.getResponseBodyAsString());
            throw new ServiceCallException(e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            log.error("HTTP server error during file upload: status={} body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new ServiceCallException("Server error during file upload: " + e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            log.error("Network error during file upload: ", e);
            throw new ServiceCallException("Network error during file upload: " + e.getMessage());
        } catch (RestClientException e) {
            log.error("Error during file upload: ", e);
            throw new ServiceCallException("File upload failed: " + e.getMessage());
        }
    }

    public ResponseEntity<Resource> fetchFile(String baseUrl, String tenantId, String fileStoreId) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/id")
                .queryParam("tenantId", tenantId)
                .queryParam("fileStoreId", fileStoreId)
                .toUriString();

        log.info("fetching file from {} ", url);

        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                Resource.class
        );
    }

}
