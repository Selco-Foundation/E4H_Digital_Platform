package org.egov.im.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.im.config.IMConfiguration;
import org.egov.im.repository.ServiceRequestRepository;
import org.egov.im.web.models.ProcessingContext;
import org.egov.im.web.models.storage.StorageResponse;
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
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Component
@Slf4j
public class StorageUtil {

    private final IMConfiguration configuration;
    private final ServiceRequestRepository serviceRequestRepository;


    /**
     * Calls File-store service to store files and returns list of file ids
     *
     * @param filesToStore
     * @return storage response from filestore service
     * @throws IOException
     */
    @Async
    public CompletableFuture<StorageResponse> uploadToFileStorage(List<MultipartFile> filesToStore,
                                                                  ProcessingContext context) throws IOException {

        final String URL = getFileStoreURL().toString();
        log.info("uploading to filestore service at {}", URL);
        return serviceRequestRepository.uploadFiles(
                filesToStore, context, URL);
    }

    /**
     * Calls File-store service to store files and returns list of file ids
     *
     * @param filesToStore
     * @return storage response from filestore service
     * @throws IOException
     */
    @Async
    public CompletableFuture<StorageResponse> uploadToHLSFileStorage(List<MultipartFile> filesToStore,
                                                                     ProcessingContext context) throws IOException {

        final String URL = getFileStoreURL(configuration.getFileStoreHlsUploadEndpoint()).toString();
        log.info("uploading {} to file-store service at {}", filesToStore, URL);
        return serviceRequestRepository.uploadFiles(
                filesToStore, context, URL);
    }

    /**
     * Returns the url for file-storage upload endpoint
     *
     * @return url for filestore upload endpoint
     */
    public StringBuilder getFileStoreURL() {
        return new StringBuilder().append(configuration.getFileStoreHost())
                .append(configuration.getFileStoreUploadEndpoint());
    }

    /**
     * Returns the url for file-storage upload endpoint
     *
     * @return url for filestore upload endpoint
     */
    public StringBuilder getFileStoreURL(String endPoint) {
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
        ResponseEntity<Resource> response =
                serviceRequestRepository.fetchFile(getFileStoreURL().toString(), tenantId, fileStoreId);
        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        }
        throw new CustomException("Error fetching file", fileStoreId);
    }

    public void writeFileToTempFile(Resource resource, Path tempFile) throws IOException {
        try {
            File newFile = tempFile.toFile();

            // Check if the file does not exist
            if (!newFile.exists()) {
                log.warn("The file {} does not exist. Creating file.", newFile.getAbsolutePath());
                boolean fileCreated = newFile.createNewFile();
                if (!fileCreated) {
                    // If the file cannot be created, throw a custom exception
                    log.error("Failed to create the file {}", newFile.getAbsolutePath());
                    throw new CustomException("Failed to create the file: ", newFile.getAbsolutePath());
                }
                log.info("File created: {}", newFile.getAbsolutePath());
            }

            // Writing file contents
            try (FileOutputStream fos = new FileOutputStream(newFile);
                 InputStream inputStream = resource.getInputStream();
                 BufferedInputStream bis = new BufferedInputStream(inputStream);
                 BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                byte[] buffer = new byte[16384];  // 16KB buffer for reading and writing
                int bytesRead;
                while ((bytesRead = bis.read(buffer)) != -1) {
                    bos.write(buffer, 0, bytesRead);
                }
            }
        } catch (NoSuchFileException ex) {
            log.error("Error processing temporary file. File not found: {}", tempFile.toAbsolutePath(), ex);
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
        String originalFilename = resource.getFilename();
        return (originalFilename != null && originalFilename.contains("."))
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".tmp";
    }


    // file extension
    public File createTempFile(File tempDir, Resource resource) {
        String extension = getFileExtension(resource);
        // Create custom temp file using the pre-initialized temp directory
        String uniqueFileName = String.format("%s_%s%s", "video", UUID.randomUUID(), extension);
        return new File(tempDir, uniqueFileName);
    }


    /**
     * Cleans up temporary files after processing.
     */
    @Async
    public CompletableFuture<Void> cleanupTemporaryFiles(String videoId, File tempFile, Path outputPath) {
        log.info("Deleting temporary files for videoId: {}", videoId);

        Path videoDirectory = outputPath.resolve(videoId);

        // Check if video directory exists and delete temp file if it exists
        if (Files.exists(videoDirectory) || Files.exists(tempFile.toPath())) {
            if (tempFile.exists()) {
                try {
                    Files.delete(tempFile.toPath());
                    log.debug("Deleted temp file: {}", tempFile);
                } catch (IOException e) {
                    log.warn("Failed to delete temp file: {}", tempFile, e);
                }
            }

            log.info("Cleaning up temporary files for videoId: {}", videoId);

            // Delete all files and subdirectories in reverse order
            try (Stream<Path> paths = Files.walk(videoDirectory)) {
                paths.sorted(Comparator.reverseOrder()) // Sorting paths in reverse order
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                                log.debug("Deleted: {}", path);
                            } catch (IOException e) {
                                log.warn("Failed to delete: {}", path, e);
                            }
                        });
            } catch (IOException e) {
                log.warn("Error walking through video directory for videoId: {}", videoId, e);
            }

            // Delete master playlist if it exists
            Path masterPlaylist = outputPath.resolve(videoId + "_master.m3u8");
            try {
                if (Files.exists(masterPlaylist)) {
                    Files.delete(masterPlaylist);
                    log.debug("Deleted master playlist: {}", masterPlaylist);
                }
            } catch (IOException e) {
                log.warn("Failed to delete master playlist: {}", masterPlaylist, e);
            }
        } else {
            log.warn("Video directory does not exist: {}", videoDirectory);
        }

        log.info("Cleanup completed for videoId: {}", videoId);
        return CompletableFuture.completedFuture(null);
    }

}
