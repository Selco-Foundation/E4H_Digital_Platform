package org.egov.processor.repositories;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.processor.models.ProcessingContext;
import org.egov.processor.models.storage.StorageResponse;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Repository;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
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
        log.trace("Method invoked: fetchResult, URI: {}", uri);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        Object response = null;
        try {
            log.debug("Calling external service: {}", uri);
            response = restTemplate.postForObject(uri.toString(), request, Map.class);
            log.debug("External service call successful");
        } catch (HttpClientErrorException e) {
            log.error("External service returned error, URI: {}, status: {}", uri, e.getStatusCode(), e);
            throw new ServiceCallException(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Exception while calling external service, URI: {}", uri, e);
        }
        return response;
    }

    public StorageResponse uploadFiles(List<MultipartFile> files,
                                       ProcessingContext context,
                                       String url) throws IOException {
        log.trace("Method invoked: uploadFiles, videoId: {}, file count: {}, URL: {}", context.getVideoId(), files != null ? files.size() : 0, url);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        log.debug("Preparing file resources for upload");
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
            log.info("Uploading {} files to file store service for videoId: {}", files.size(), context.getVideoId());
            ResponseEntity<StorageResponse> responseEntity =
                    restTemplate.exchange(url, HttpMethod.POST, requestEntity, StorageResponse.class);
            if (responseEntity.getStatusCode() != HttpStatus.CREATED) {
                log.error("File upload failed with status: {} for videoId: {}", responseEntity.getStatusCode(), context.getVideoId());
                throw new ServiceCallException(String.format("File upload failed with status: %s",
                        responseEntity.getStatusCode()));
            }
            log.info("Successfully uploaded files to file store service for videoId: {}", context.getVideoId());
            return responseEntity.getBody();
        } catch (HttpClientErrorException e) {
            log.error("File upload failed for videoId: {}, status: {}", context.getVideoId(), e.getStatusCode(), e);
            throw new ServiceCallException(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Unexpected error during file upload for videoId: {}", context.getVideoId(), e);
            throw new ServiceCallException("File upload failed: " + e.getMessage());
        }
    }

    public ResponseEntity<Resource> fetchFile(String baseUrl, String tenantId, String fileStoreId) {
        log.trace("Method invoked: fetchFile, tenantId: {}, fileStoreId: {}", tenantId, fileStoreId);
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/id")
                .queryParam("tenantId", tenantId)
                .queryParam("fileStoreId", fileStoreId)
                .toUriString();

        log.info("Fetching file from file store service, fileStoreId: {}", fileStoreId);
        log.debug("File store URL: {}", url);

        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                Resource.class
        );
    }

}

