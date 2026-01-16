package org.egov.im.web.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.im.config.IMConfiguration;
import org.egov.im.producer.Producer;
import org.egov.im.service.StorageService;
import org.egov.im.util.StorageUtil;
import org.egov.im.web.models.ProcessingContext;
import org.egov.im.web.models.storage.StorageProcessingContext;
import org.egov.im.web.models.storage.StorageResponse;
import org.egov.tracer.model.CustomException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v2/video")
@Slf4j
public class StorageController {

    private final StorageService storageService;
    private final Producer producer;
    private final IMConfiguration configuration;
    private final StorageUtil storageUtil;

    @PostMapping(value = "upload")
    public StorageResponse storeFiles(@RequestParam("file") List<MultipartFile> files,
                                      @RequestParam(value = "tenantId") String tenantId,
                                      @RequestParam(value = "module", required = true) String module,
                                      @RequestParam(value = "tag", required = false) String tag,
                                      @RequestParam(value = "requestInfo", required = false) String requestInfo) {
        log.trace("StorageController::storeFiles method invoked");
        log.info("Received upload request for tenantId: {}, module: {}, tag: {} with file count: {}",
                tenantId, module, tag, files.size());

        List<java.io.File> tempFiles = null;
        try {
            // Build the processing context
            ProcessingContext context = ProcessingContext.builder()
                    .requestInfo(requestInfo)
                    .tag(tag)
                    .tenantId(tenantId)
                    .module(module)
                    .build();

            //crete temp files
            tempFiles = storageService.createTempFiles(files);
            StorageResponse storageResponse =
                    storageService.saveOriginalFileToS3(files, context);

            log.info("Start processing master files");
            storageResponse =
                    storageService.createAndSaveMasterFiles(storageResponse, tempFiles, context);

            StorageProcessingContext storageProcessingContext = StorageProcessingContext.builder()
                    .storageResponse(storageResponse)
                    .context(context)
                    .build();

            log.info("Master files created successfully, fileCount={}", storageResponse.getFiles() != null ? storageResponse.getFiles().size() : 0);
            log.trace("Pushing storage response to Kafka topic: {}", configuration.getVideoProcessorTopic());
            producer.push(tenantId, configuration.getVideoProcessorTopic(),storageProcessingContext);
            log.info("Storage response pushed to Kafka topic successfully");

            return storageResponse;

        } catch (Exception e) {
            log.error("ERROR_UPLOADING_TO_FILESTORE: {}", e.getMessage());
            throw new CustomException("ERROR_UPLOADING_TO_FILESTORE", e.getMessage());
        }finally {
            log.debug("Deleting {} temporary files", tempFiles != null ? tempFiles.size() : 0);
            storageUtil.deleteFiles(tempFiles);
        }
    }
}
