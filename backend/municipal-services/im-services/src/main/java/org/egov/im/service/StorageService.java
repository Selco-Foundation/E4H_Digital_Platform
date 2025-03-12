package org.egov.im.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.im.util.StorageUtil;
import org.egov.im.validator.StorageValidator;
import org.egov.im.web.models.ProcessingContext;
import org.egov.im.web.models.storage.StorageResponse;
import org.egov.tracer.model.CustomException;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
@RequiredArgsConstructor
@Service
@Slf4j
public class StorageService {

    private final StorageValidator storageValidator;
    private final StorageUtil storageUtil;
    private final VideoService videoService;

    private File tempDir;

    private static final String INPUT_DIR = "input";

    @PostConstruct
    private void initTempFile() {
        Path customTempDir = Paths.get(System.getProperty("user.dir"), INPUT_DIR);
        tempDir = new File(customTempDir.toAbsolutePath().toString());
        if (!tempDir.exists()) {
            tempDir.mkdirs();  // Ensure directory exists
            log.info("Created temporary directory at: {}", customTempDir);
        } else {
            log.info("Temporary directory already exists at: {}", customTempDir);
        }
    }

    public StorageResponse save(List<MultipartFile> filesToStore, ProcessingContext context) throws IOException {
        storageValidator.validate(filesToStore);
        StorageResponse storageResponse = storageUtil.uploadToFileStorage(filesToStore, context);

        // Create master files
        List<org.egov.im.web.models.storage.File> updatedFiles = storageResponse.getFiles()
                .stream()
                .map(fileMetadata -> {
                    String fileStoreId = fileMetadata.getFileStoreId();
                    try {
                        int index = storageResponse.getFiles().indexOf(fileMetadata);
                        Resource resource = filesToStore.get(index).getResource();

                        File tempFile = storageUtil.createTempFile(tempDir, resource);

                        // Call temp file creator (write file to temp location)
                        storageUtil.writeFileToTempFile(resource, tempFile.toPath());

                        // Process the video synchronously and return response
                        StorageResponse response = videoService.processVideo(tempFile, context.withVideoId(fileStoreId));
                        String masterFileStoreId = response.getFiles().get(0).getFileStoreId();

                        return fileMetadata.toBuilder()
                                .masterFileStoreId(masterFileStoreId)
                                .build();

                    } catch (IOException ex) {
                        log.error("I/O Error while processing fileStoreId {}: {}", fileStoreId, ex.getMessage(), ex);
                        throw new CustomException("I/O Error processing video", ex.getMessage());

                    } catch (CustomException ex) {
                        log.error("Custom Exception for fileStoreId {}: {}", fileStoreId, ex.getMessage(), ex);
                        throw ex;

                    } catch (Exception ex) {
                        log.error("Unexpected error while processing fileStoreId {}: {}", fileStoreId, ex.getMessage(), ex);
                        throw new CustomException("Unexpected error processing video", ex.getMessage());
                    }
                })
                .toList();

        return storageResponse.toBuilder().files(updatedFiles).build();
    }

    @Async
    public CompletableFuture<Void> saveChunks(List<MultipartFile> filesToStore,
                                              StorageResponse storageResponse, ProcessingContext context) {

        for (org.egov.im.web.models.storage.File fileMetadata : storageResponse.getFiles()) {
            String fileStoreId = fileMetadata.getFileStoreId();

            try {
                int index = storageResponse.getFiles().indexOf(fileMetadata);
                Resource resource = filesToStore.get(index).getResource();

                log.info("File received: {}, Filename: {}", resource, resource.getFilename());

                File tempFile = storageUtil.createTempFile(tempDir, resource);

                // Write the file to the temporary location
                storageUtil.writeFileToTempFile(resource, tempFile.toPath());

                // Process the video asynchronously
                videoService.processVideoAsync(tempFile, context.withVideoId(fileStoreId));

            } catch (IOException ex) {
                log.error("I/O Error while processing fileStoreId {}: {}", fileStoreId, ex.getMessage(), ex);
                throw new CustomException("I/O Error processing video", ex.getMessage());
            } catch (CustomException ex) {
                log.error("Custom Exception for fileStoreId {}: {}", fileStoreId, ex.getMessage(), ex);
                throw ex;
            } catch (Exception ex) {
                log.error("Unexpected error while processing fileStoreId {}: {}", fileStoreId, ex.getMessage(), ex);
                throw new CustomException("Unexpected error processing video", ex.getMessage());
            }
        }

        return CompletableFuture.completedFuture(null);
    }
}
