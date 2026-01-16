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
        log.trace("Method invoked: initTempFile");
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
        log.trace("Method invoked: createTempFiles, file count: {}", files != null ? files.size() : 0);
        List<File> tempFiles = new ArrayList<>();
        files.forEach(file -> {
            try {
                log.debug("Creating temp file for: {}", file.getOriginalFilename());
                Resource resource = file.getResource();
                File tempFile = storageUtil.createTempFile(tempDir, resource);
                storageUtil.writeFileToTempFile(resource, tempFile.toPath());
                tempFiles.add(tempFile);
            } catch (IOException e) {
                log.error("Error processing file: {}", file.getOriginalFilename(), e);
                throw new CustomException("ERROR_CREATING_TEMP_FILES", e.getMessage());
            }
        });
        log.debug("Created {} temporary files", tempFiles.size());
        return tempFiles;
    }

    public void createAndSaveChunks(String fileStoreId, File resource, ProcessingContext context) {
        log.trace("Method invoked: createAndSaveChunks, fileStoreId: {}, filename: {}", fileStoreId, resource != null ? resource.getName() : "null");
        try {
            log.info("Processing file: {} for fileStoreId: {}", resource.getName(), fileStoreId);
            
            // Only process videos, skip images
            if (isVideoFile(resource)) {
                log.debug("File is a video, proceeding with async processing");
                videoService.processVideoAsync(resource, context.withVideoId(fileStoreId));
            } else {
                log.info("Skipping video processing for non-video file: {}", resource.getName());
            }

        } catch (CustomException ex) {
            log.error("Custom exception while processing fileStoreId: {}", fileStoreId, ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while processing fileStoreId: {}", fileStoreId, ex);
            throw new CustomException("Unexpected error processing file", ex.getMessage());
        }
    }

    public void processAndStoreFiles(StorageProcessingContext storageProcessingContext){
        log.trace("Method invoked: processAndStoreFiles");
        log.info("Starting processing of storage files");
        List<File> tempFiles = null;
        try {
            log.debug("Retrieving files from storage response");
            List<MultipartFile> files = storageProcessingContext.getStorageResponse().getFiles()
                    .stream()
                    .map(file -> storageUtil.getMultipartFileFromS3(file.getTenantId(), file.getFileStoreId()))
                    .collect(Collectors.toList());

            log.info("Creating temporary files, count: {}", files.size());
            tempFiles = createTempFiles(files);
            log.info("Starting asynchronous chunk processing for {} files", storageProcessingContext.getStorageResponse().getFiles().size());

            // Process chunks asynchronously without waiting for completion
            for (int index = 0; index < storageProcessingContext.getStorageResponse().getFiles().size(); index++) {
                org.egov.processor.models.storage.File fileMetadata = storageProcessingContext.getStorageResponse().getFiles().get(index);
                File file = tempFiles.get(index);
                String fileStoreId = fileMetadata.getFileStoreId();

                createAndSaveChunks(fileStoreId, file, storageProcessingContext.getContext());
            }
            log.info("Completed processing all files");
        }catch (Exception e){
            log.error("Error uploading to filestore", e);
            throw new CustomException("ERROR_UPLOADING_TO_FILESTORE", e.getMessage());
        }finally {
            log.info("Deleting temporary files");
            storageUtil.deleteFiles(tempFiles);
        }
    }

    private boolean isVideoFile(File file) {
        log.trace("Method invoked: isVideoFile, filename: {}", file.getName());
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".mp4") || fileName.endsWith(".avi") || 
               fileName.endsWith(".mov") || fileName.endsWith(".wmv");
    }
}
