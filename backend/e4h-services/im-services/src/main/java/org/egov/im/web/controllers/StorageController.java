package org.egov.im.web.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.im.service.StorageService;
import org.egov.im.util.StorageUtil;
import org.egov.im.web.models.ProcessingContext;
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
    private final StorageUtil storageUtil;

    @PostMapping(value = "upload")
    public StorageResponse storeFiles(@RequestParam("file") List<MultipartFile> files,
                                      @RequestParam(value = "tenantId") String tenantId,
                                      @RequestParam(value = "module", required = true) String module,
                                      @RequestParam(value = "tag", required = false) String tag,
                                      @RequestParam(value = "requestInfo", required = false) String requestInfo) {

        log.info("Received upload request for jurisdiction: {}, module: {}, tag: {} with file count: {}",
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

            log.info("Done creating master files: {}", storageResponse);
            log.info("Start processing chunks asynchronously");

            // Process chunks asynchronously without waiting for completion
            for (int index = 0; index < storageResponse.getFiles().size(); index++) {
                org.egov.im.web.models.storage.File fileMetadata = storageResponse.getFiles().get(index);
                File file = tempFiles.get(index);
                String fileStoreId = fileMetadata.getFileStoreId();

                // Submit each chunk processing task asynchronously
                storageService.createAndSaveChunks(fileStoreId, file, context);
            }
            log.info("Chunk processing tasks submitted. Returning response immediately.");
            return storageResponse;

        } catch (Exception e) {
            log.error("ERROR_UPLOADING_TO_FILESTORE: {}", e.getMessage());
            log.info("deleting all temporary files ");
            storageUtil.deleteFiles(tempFiles);
            throw new CustomException("ERROR_UPLOADING_TO_FILESTORE", e.getMessage());
        }
    }
}
