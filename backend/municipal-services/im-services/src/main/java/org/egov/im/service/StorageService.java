package org.egov.im.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.im.config.IMConfiguration;
import org.egov.im.util.StorageUtil;
import org.egov.im.validator.StorageValidator;
import org.egov.im.web.models.ProcessingContext;
import org.egov.im.web.models.storage.StorageResponse;
import org.egov.tracer.model.CustomException;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

@RequiredArgsConstructor
@Service
@Slf4j
public class StorageService {

    private final StorageValidator storageValidator;
    private final StorageUtil storageUtil;
    private final VideoService videoService;

    public StorageResponse save(List<MultipartFile> filesToStore,
                                ProcessingContext context) throws IOException {

        storageValidator.validate(filesToStore);
        StorageResponse storageResponse =
                storageUtil.uploadToFileStorage(filesToStore, context);

        storageResponse.getFiles().forEach(fileMetadata -> {
            String fileStoreId = fileMetadata.getFileStoreId();

            try {
                Resource resource = storageUtil.getFile(context.getTenantId(), fileStoreId);
                log.info("File received: {}, Filename: {}", resource, resource.getFilename());

                // Create a temporary file
                String originalFilename = resource.getFilename();
                String extension = originalFilename != null && originalFilename.contains(".")
                        ? originalFilename.substring(originalFilename.lastIndexOf("."))
                        : ".tmp";
                File tempFile = File.createTempFile("video_", extension);

                // Write byte array data to temp file
                try (FileOutputStream fos = new FileOutputStream(tempFile);
                     java.io.InputStream inputStream = resource.getInputStream();
                     java.io.BufferedInputStream bis = new java.io.BufferedInputStream(inputStream);
                     java.io.BufferedOutputStream bos = new java.io.BufferedOutputStream(fos)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = bis.read(buffer)) != -1) {
                        bos.write(buffer, 0, bytesRead);
                    }
                }

                try {
                    // Process video using the temp file
                    videoService.processVideo(tempFile, context.withVideoId(fileStoreId));
                    log.info("Video processing completed for fileStoreId: {}", fileStoreId);
                } finally {
                    // Explicitly delete the temp file after processing
                    if (!tempFile.delete()) {
                        log.warn("Failed to delete temporary file: {}", tempFile.getAbsolutePath());
                        tempFile.deleteOnExit();
                    }
                }
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
        });

        return storageResponse;
    }
}
