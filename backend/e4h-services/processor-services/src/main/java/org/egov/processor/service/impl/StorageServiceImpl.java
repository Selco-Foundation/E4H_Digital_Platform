package org.egov.processor.service.impl;

import jakarta.annotation.PostConstruct;
import org.egov.processor.models.ProcessingContext;
import org.egov.processor.models.storage.StorageProcessingContext;
import org.egov.processor.service.StorageService;
import org.egov.processor.service.VideoService;
import org.egov.processor.utils.StorageUtil;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StorageServiceImpl implements StorageService {

    @Autowired
    private StorageUtil storageUtil;

    @Autowired
    private VideoService videoService;

    private File tempDir;

    private static final String INPUT_DIR = "input";

    @PostConstruct
    private void initTempFile() {
        Path customTempDir = Paths.get(System.getProperty("user.dir"), INPUT_DIR);
        tempDir = new File(customTempDir.toAbsolutePath().toString());
        if (!tempDir.exists()) {
            tempDir.mkdirs();
            log.info("Created temporary directory at: {}", customTempDir);
        } else {
            log.info("Temporary directory already exists at: {}", customTempDir);
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

    public void createAndSaveChunks(String fileStoreId, File resource, ProcessingContext context) {
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

    public void processAndStoreFiles(StorageProcessingContext storageProcessingContext){
        log.info("Start processing master files");
        List<File> tempFiles = null;
        try {
            List<MultipartFile> files = storageProcessingContext.getStorageResponse().getFiles()
                    .stream()
                    .map(file -> storageUtil.getMultipartFileFromS3(file.getTenantId(), file.getFileStoreId()))
                    .collect(Collectors.toList());

            tempFiles = createTempFiles(files);
            log.info("Start processing chunks asynchronously");

            // Process chunks asynchronously without waiting for completion
            for (int index = 0; index < storageProcessingContext.getStorageResponse().getFiles().size(); index++) {
                org.egov.processor.models.storage.File fileMetadata = storageProcessingContext.getStorageResponse().getFiles().get(index);
                File file = tempFiles.get(index);
                String fileStoreId = fileMetadata.getFileStoreId();

                createAndSaveChunks(fileStoreId, file, storageProcessingContext.getContext());
            }
        }catch (Exception e){
            log.error("ERROR_UPLOADING_TO_FILESTORE: {}", e.getMessage());
            throw new CustomException("ERROR_UPLOADING_TO_FILESTORE", e.getMessage());
        }finally {
            log.info("deleting all temporary files ");
            storageUtil.deleteFiles(tempFiles);
        }
    }
}
