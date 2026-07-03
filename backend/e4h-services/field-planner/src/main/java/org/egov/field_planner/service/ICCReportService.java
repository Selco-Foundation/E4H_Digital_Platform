package org.egov.field_planner.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.producer.Producer;
import org.egov.field_planner.config.FieldPlannerConfiguration;
import org.egov.field_planner.repository.IccTemplateRepository;
import org.egov.field_planner.web.models.ICCReportUploadRequest;
import org.egov.field_planner.web.models.ICCReportUploadResponse;
import org.egov.field_planner.web.models.IccTemplateSearchRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ICCReportService {

    private final FileStoreService fileStoreService;
    private final FieldPlannerConfiguration fieldPlannerConfiguration;
    private final Producer producer;
    private final IccTemplateRepository repository;

    public ICCReportService(FileStoreService fileStoreService, FieldPlannerConfiguration fieldPlannerConfiguration, Producer producer, IccTemplateRepository repository){

        this.fileStoreService = fileStoreService;
        this.fieldPlannerConfiguration = fieldPlannerConfiguration;
        this.producer = producer;
        this.repository = repository;
    }

    public ICCReportUploadResponse upload(RequestInfo requestInfo, ICCReportUploadRequest request, MultipartFile file) {

        String fileStoreId = null;
        try {
            fileStoreId = fileStoreService.upload(requestInfo, file);
        } catch (IOException e) {
            log.error("Failed to upload ICC report file", e);
            throw new CustomException("ERROR_ICC_REPORT_UPLOAD", "Failed to upload ICC report file: " + e.getMessage());
        }
        ICCReportUploadResponse response = new ICCReportUploadResponse();

        response.setFileStoreId(fileStoreId);
        response.setSystemType(request.getSystemType());
        response.setTotalSystemCapacity(request.getTotalSystemCapacity());
        response.setId(UUID.randomUUID().toString());

        producer.push(fieldPlannerConfiguration.getSaveIccTemplate(), List.of(response));
        log.info("ICC template creation request pushed to Kafka topic: {}", fieldPlannerConfiguration.getSaveIccTemplate());

        return response;
    }

    public List<ICCReportUploadResponse> search(IccTemplateSearchRequest request) {

        return repository.search(
                request.getSystemType(),
                request.getTotalSystemCapacity()
        );
    }
}
