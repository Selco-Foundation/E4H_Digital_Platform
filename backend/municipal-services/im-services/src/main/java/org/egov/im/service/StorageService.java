package org.egov.im.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public StorageResponse save(List<MultipartFile> filesToStore, ProcessingContext context) throws IOException {
        storageValidator.validate(filesToStore);
        StorageResponse storageResponse = storageUtil.uploadToFileStorage(filesToStore, context);

        List<org.egov.im.web.models.storage.File> updatedFiles = storageResponse.getFiles()
                .stream()
                .map(fileMetadata -> {
                    String fileStoreId = fileMetadata.getFileStoreId();
                    try {
                        int index = storageResponse.getFiles().indexOf(fileMetadata);
                        Resource resource = filesToStore.get(index).getResource();

                        log.info("File received: {}, Filename: {}", resource, resource.getFilename());

                        String originalFilename = resource.getFilename();
                        String extension = (originalFilename != null && originalFilename.contains("."))
                                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                                : ".tmp";
                        File tempFile = File.createTempFile("video_", extension);

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

                        StorageResponse response = videoService.processVideo(tempFile, context.withVideoId(fileStoreId));
                        String masterFileStoreId = response.getFiles().get(0).getFileStoreId();

                        videoService.processVideoAsync(tempFile, context.withVideoId(fileStoreId));

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
}
