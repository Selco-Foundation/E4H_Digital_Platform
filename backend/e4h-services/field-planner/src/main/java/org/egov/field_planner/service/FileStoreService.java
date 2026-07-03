package org.egov.field_planner.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.request.RequestInfo;
import org.egov.field_planner.config.FieldPlannerConfiguration;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class FileStoreService {

    private final RestTemplate restTemplate;
    private final FieldPlannerConfiguration fieldPlannerConfiguration;
    @Qualifier("objectMapper")
    private final ObjectMapper mapper;

    public FileStoreService(RestTemplate restTemplate, FieldPlannerConfiguration fieldPlannerConfiguration, ObjectMapper mapper){
        this.restTemplate = restTemplate;
        this.fieldPlannerConfiguration = fieldPlannerConfiguration;
        this.mapper = mapper;
    }

    public String upload(RequestInfo requestInfo, MultipartFile multipartFile) throws IOException {
        String fileStoreHost = fieldPlannerConfiguration.getFileStoreHost();
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        ByteArrayResource resource = new ByteArrayResource(multipartFile.getBytes()) {
            @Override
            public String getFilename() {
                return multipartFile.getOriginalFilename();
            }
        };
        body.add("file", resource);
        body.add("tenantId", "in");
        body.add("module", fieldPlannerConfiguration.getFileStoreModule());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        Object response = restTemplate.postForObject(fileStoreHost + "/filestore/v1/files", entity, Map.class);

        Map<String, Object> fileStore = mapper.convertValue(response, Map.class);
        if(fileStore == null){
            throw new CustomException(
                    "ERROR_FILE_UPLOAD",
                    "Error occurred while uploading file to filestore"
            );
        }
        List<Map<String, Object>> filestoreIds = (List<Map<String, Object>>) fileStore.get("files");
        if (filestoreIds == null || filestoreIds.isEmpty()) {
            throw new CustomException("ERROR_REPORT_UPLOAD", "No filestoreId returned");
        }
        String fileStoreId = (String)filestoreIds.get(0).get("fileStoreId");
        return fileStoreId;
    }
}
