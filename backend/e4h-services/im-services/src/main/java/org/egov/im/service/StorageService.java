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

    public StorageResponse saveOriginalFileToS3(List<MultipartFile> filesToStore, ProcessingContext context) {
        storageValidator.validate(filesToStore);
        try {
            StorageResponse storageResponse = storageUtil.uploadToFileStorage(filesToStore, context);
            log.info("file store response: {}", storageResponse);
            return storageResponse;
        } catch (IOException e) {
            throw new CustomException("Error saving original file to S3", e.getMessage());
        }
    }

    public StorageResponse createAndSaveMasterFiles(StorageResponse storageResponse, List<File> filesToStore, ProcessingContext context) {
        // Create master files
        List<org.egov.im.web.models.storage.File> updatedFiles = storageResponse.getFiles()
                .stream()
                .map(fileMetadata -> {
                    String fileStoreId = fileMetadata.getFileStoreId();
                    try {
                        int index = storageResponse.getFiles().indexOf(fileMetadata);
                        File tempFile = filesToStore.get(index);

                        // Process the video synchronously and return response
                        StorageResponse response =
                                videoService.processVideo(tempFile, context.withVideoId(fileStoreId));

                        String masterFileStoreId = response.getFiles().get(0).getFileStoreId();

                        return fileMetadata.toBuilder()
                                .masterFileStoreId(masterFileStoreId)
                                .build();

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
    public void createAndSaveChunks(String fileStoreId,
                                    File resource, ProcessingContext context) {
        try {
            log.info("File received: {}, Filename: {}", resource, resource.getName());
            // Process the video asynchronously
            videoService.processVideoAsync(resource, context.withVideoId(fileStoreId));

        } catch (CustomException ex) {
            log.error("Custom Exception for fileStoreId {}: {}", fileStoreId, ex.getMessage(), ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while processing fileStoreId {}: {}", fileStoreId, ex.getMessage(), ex);
            throw new CustomException("Unexpected error processing video", ex.getMessage());
        }
    }

    public List<File> createTempFiles(List<MultipartFile> files) {
        List<File> tempFiles = new ArrayList<>();
        files.forEach(file -> {
            try {
                Resource resource = file.getResource();
                File tempFile = storageUtil.createTempFile(tempDir, resource);
                storageUtil.writeFileToTempFile(resource, tempFile.toPath());
                tempFiles.add(tempFile);
            } catch (IOException e) {
                log.error("Error processing file: {}", file.getOriginalFilename(), e);
                throw new CustomException("ERROR_CREATING_TEMP_FILES", e.getMessage());
            }
        });
        return tempFiles;
    }

}
