package org.egov.processor.utils;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItem;
import org.egov.processor.config.ProcessorConfiguration;
import org.egov.processor.models.ProcessingContext;
import org.egov.processor.models.storage.CommonMultipartFile;
import org.egov.processor.models.storage.StorageResponse;
import org.egov.processor.repositories.ServiceRequestRepository;
import org.egov.tracer.model.CustomException;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Component
@Slf4j
public class StorageUtil {

    private final ProcessorConfiguration configuration;
    private final ServiceRequestRepository serviceRequestRepository;


    /**
     * Calls File-store service to store files and returns list of file ids
     *
     * @param filesToStore
     * @return storage response from filestore service
     * @throws IOException
     */
    public StorageResponse uploadToHLSFileStorage(List<MultipartFile> filesToStore,
                                                  ProcessingContext context) throws IOException {
        log.trace("Method invoked: uploadToHLSFileStorage, videoId: {}, file count: {}", context.getVideoId(), filesToStore != null ? filesToStore.size() : 0);
        final String URL = getFileStoreURL(configuration.getFileStoreHlsUploadEndpoint()).toString();
        log.info("Uploading {} files to file-store service for videoId: {}", filesToStore.size(), context.getVideoId());
        log.debug("File store URL: {}", URL);
        return serviceRequestRepository.uploadFiles(
                filesToStore, context, URL);
    }

    /**
     * Returns the url for file-storage upload endpoint
     *
     * @return url for filestore upload endpoint
     */
    public StringBuilder getFileStoreURL() {
        log.trace("Method invoked: getFileStoreURL");
        return new StringBuilder().append(configuration.getFileStoreHost())
                .append(configuration.getFileStoreUploadEndpoint());
    }

    /**
     * Returns the url for file-storage upload endpoint
     *
     * @return url for filestore upload endpoint
     */
    public StringBuilder getFileStoreURL(String endPoint) {
        log.trace("Method invoked: getFileStoreURL, endpoint: {}", endPoint);
        String host = configuration.getFileStoreHost();
        if (configuration.getFileStoreHost().endsWith("/")) {
            host = configuration.getFileStoreHost().substring(0, configuration.getFileStoreHost().length() - 1);
        }
        return new StringBuilder().append(host)
                .append(endPoint);
    }


    /**
     * Fetches and returns the requested file as a Resource
     *
     * @return the fetched file as a Resource
     */
    public Resource getFile(String tenantId, String fileStoreId) {
        log.trace("Method invoked: getFile, tenantId: {}, fileStoreId: {}", tenantId, fileStoreId);
        ResponseEntity<Resource> response =
                serviceRequestRepository.fetchFile(getFileStoreURL().toString(), tenantId, fileStoreId);
        if (response.getStatusCode().is2xxSuccessful()) {
            log.debug("Successfully fetched file: {}", fileStoreId);
            return response.getBody();
        }
        log.error("Failed to fetch file: {}, status code: {}", fileStoreId, response.getStatusCode());
        throw new CustomException("Error fetching file", fileStoreId);
    }

    public void writeFileToTempFile(Resource resource, Path tempFile) throws IOException {
        log.trace("Method invoked: writeFileToTempFile, tempFile: {}", tempFile);
        try {
            File newFile = tempFile.toFile();

            // Check if the file does not exist
            if (!newFile.exists()) {
                log.debug("File does not exist, creating: {}", newFile.getAbsolutePath());
                boolean fileCreated = newFile.createNewFile();
                if (!fileCreated) {
                    // If the file cannot be created, throw a custom exception
                    log.error("Failed to create the file: {}", newFile.getAbsolutePath());
                    throw new CustomException("Failed to create the file: ", newFile.getAbsolutePath());
                }
                log.debug("File created: {}", newFile.getAbsolutePath());
            }

            // Writing file contents
            log.trace("Writing file contents to temporary file");
            try (FileOutputStream fos = new FileOutputStream(newFile);
                 InputStream inputStream = resource.getInputStream();
                 BufferedInputStream bis = new BufferedInputStream(inputStream);
                 BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                byte[] buffer = new byte[16384];  // 16KB buffer for reading and writing
                int bytesRead;
                long totalBytes = 0;
                while ((bytesRead = bis.read(buffer)) != -1) {
                    bos.write(buffer, 0, bytesRead);
                    totalBytes += bytesRead;
                }
                log.debug("Written {} bytes to temporary file: {}", totalBytes, tempFile);
            }
        } catch (NoSuchFileException ex) {
            log.error("File not found while processing temporary file: {}", tempFile.toAbsolutePath(), ex);
            throw new CustomException(
                    String.format("ERROR_PROCESSING_TEMP_FILE: File not found - %s ", tempFile.toAbsolutePath()), ex.getMessage());
        } catch (IOException ex) {
            log.error("I/O error while processing file: {}", tempFile.toAbsolutePath(), ex);
            throw new CustomException(
                    String.format("ERROR_PROCESSING_TEMP_FILE: I/O error %s- ", tempFile.toAbsolutePath()), ex.getMessage());
        } catch (Exception ex) {
            log.error("Unexpected error while processing file: {}", tempFile.toAbsolutePath(), ex);
            throw new CustomException(
                    String.format("Unexpected error while processing file: %s", tempFile.toAbsolutePath()), ex.getMessage());
        }
    }


    // file extension
    public String getFileExtension(Resource resource) {
        log.trace("Method invoked: getFileExtension");
        String originalFilename = resource.getFilename();
        return (originalFilename != null && originalFilename.contains("."))
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".tmp";
    }


    // file extension
    public File createTempFile(File tempDir, Resource resource) {
        log.trace("Method invoked: createTempFile");
        String extension = getFileExtension(resource);
        // Create custom temp file using the pre-initialized temp directory
        String uniqueFileName = String.format("%s_%s%s", "video", UUID.randomUUID(), extension);
        log.debug("Creating temp file: {}", uniqueFileName);
        return new File(tempDir, uniqueFileName);
    }


    /**
     * Cleans up temporary files after processing.
     */
    public void cleanupTemporaryFiles(String videoId, File tempFile, Path outputPath) {
        log.info("Deleting temporary files for videoId: {}", videoId);

        Path videoDirectory = outputPath.resolve(videoId);

        // Clean up the temporary files
        cleanTempFile(tempFile);

        // Clean up video directory if it exists
        cleanVideoDirectory(videoDirectory);

        // Delete master playlist if it exists
        deleteMasterPlaylist(videoId, outputPath);

        log.info("Cleanup completed for videoId: {}", videoId);
    }

    /**
     * Deletes the temp file if it exists.
     */
    private void cleanTempFile(File tempFile) {
        log.trace("Method invoked: cleanTempFile, file: {}", tempFile != null ? tempFile.getName() : "null");
        if (tempFile.exists()) {
            try {
                Files.delete(tempFile.toPath());
                log.debug("Deleted temp file: {}", tempFile);
            } catch (IOException e) {
                log.warn("Failed to delete temp file: {}", tempFile, e);
            }
        }
    }

    /**
     * Deletes all files and subdirectories in the video directory in reverse order.
     */
    private void cleanVideoDirectory(Path videoDirectory) {
        log.trace("Method invoked: cleanVideoDirectory, directory: {}", videoDirectory);
        if (Files.exists(videoDirectory)) {
            try (Stream<Path> paths = Files.walk(videoDirectory)) {
                paths.sorted(Comparator.reverseOrder()) // Sorting paths in reverse order
                        .forEach(path -> deleteFile(path));
            } catch (IOException e) {
                log.warn("Error walking through video directory: {}", videoDirectory, e);
            }
        } else {
            log.debug("Video directory does not exist: {}", videoDirectory);
        }
    }

    /**
     * Deletes the given file and logs the result.
     */
    private void deleteFile(Path path) {
        log.trace("Method invoked: deleteFile, path: {}", path);
        try {
            Files.delete(path);
            log.debug("Deleted: {}", path);
        } catch (IOException e) {
            log.warn("Failed to delete: {}", path, e);
        }
    }

    /**
     * Deletes the master playlist if it exists.
     */
    private void deleteMasterPlaylist(String videoId, Path outputPath) {
        log.trace("Method invoked: deleteMasterPlaylist, videoId: {}", videoId);
        Path masterPlaylist = outputPath.resolve(videoId + "_master.m3u8");
        try {
            if (Files.exists(masterPlaylist)) {
                Files.delete(masterPlaylist);
                log.debug("Deleted master playlist: {}", masterPlaylist);
            }
        } catch (IOException e) {
            log.warn("Failed to delete master playlist: {}", masterPlaylist, e);
        }
    }

    /**
     * Deletes all files in the provided list.
     * Logs a message for each file deleted and handles any deletion errors.
     */
    public void deleteFiles(List<File> files) {
        if (files == null || files.isEmpty()) {
            log.warn("No files to delete.");
            return;
        }

        for (File file : files) {
            try {
                if (file.exists()) {
                    Files.delete(file.toPath());
                    log.debug("Deleted file: {}", file.getAbsolutePath());
                } else {
                    log.warn("File does not exist: {}", file.getAbsolutePath());
                }
            } catch (IOException e) {
                log.warn("Failed to delete file: {}", file.getAbsolutePath(), e);
            }
        }
    }

    public MultipartFile getMultipartFileFromS3(String tenantId, String fileStoreId) {
        log.trace("Method invoked: getMultipartFileFromS3, tenantId: {}, fileStoreId: {}", tenantId, fileStoreId);
        try {
            Resource resource = getFile(tenantId, fileStoreId);
            String filename = resource.getFilename() != null ? resource.getFilename() : "file";
            log.debug("Retrieved file from S3: {}", filename);
            return new CommonMultipartFile(resource, filename);

        } catch (Exception e) {
            log.error("Error retrieving multipart file from S3, fileStoreId: {}", fileStoreId, e);
            throw new RuntimeException(e);
        }
    }



}

