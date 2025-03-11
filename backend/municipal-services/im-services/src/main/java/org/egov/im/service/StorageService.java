package org.egov.im.service;

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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Service
@Slf4j
public class StorageService {

    private final StorageValidator storageValidator;
    private final StorageUtil storageUtil;
    private final VideoService videoService;

    public StorageResponse save(List<MultipartFile> filesToStore, ProcessingContext context) throws IOException {
        storageValidator.validate(filesToStore);
        StorageResponse storageResponse = storageUtil.uploadToFileStorage(filesToStore, context);

        //create master files
        List<org.egov.im.web.models.storage.File> updatedFiles = storageResponse.getFiles()
                .stream()
                .map(fileMetadata -> {
                    String fileStoreId = fileMetadata.getFileStoreId();
                    try {
                        int index = storageResponse.getFiles().indexOf(fileMetadata);
                        Resource resource = filesToStore.get(index).getResource();

                        String extension = storageUtil.getFileExtension(resource);
                        File tempFile = File.createTempFile("video_", extension);

                        //call temp file creator
                        storageUtil.writeFileToTempFile(resource, tempFile);

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

                // Generate a unique temp file name using UUID and the file extension
                String extension = storageUtil.getFileExtension(resource);
                String tempFileName = "video_" + UUID.randomUUID().toString() + extension;
                File tempFile = new File(System.getProperty("java.io.tmpdir"), tempFileName);

                // Write the file to the temporary location
                storageUtil.writeFileToTempFile(resource, tempFile);

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
