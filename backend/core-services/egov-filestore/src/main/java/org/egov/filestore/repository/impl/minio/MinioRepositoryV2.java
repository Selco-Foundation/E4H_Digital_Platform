package org.egov.filestore.repository.impl.minio;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.filestore.config.Properties;
import org.egov.filestore.domain.model.Artifact;
import org.egov.filestore.repository.CloudFileManagerV2;
import org.egov.filestore.utils.StorageUtil;
import org.egov.tracer.model.CustomException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class MinioRepositoryV2 implements CloudFileManagerV2 {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;
    private final StorageUtil storageUtil;
    private final Properties properties;

    private static final String ERROR_IN_CONFIGURATION = "Error in Configuration";

    @Async
    @Override
    public void saveFiles(List<Artifact> artifacts) {
        log.trace("Entering saveFiles method (async) with artifactCount: {}", artifacts.size());
        log.info("Saving {} HLS files to MinIO (async)", artifacts.size());
        try {
            artifacts.forEach(artifact -> {
                log.trace("Processing HLS artifact: {}", artifact.getFileLocation().getFileName());
                String fileSource = artifact.getFileLocation().getFileSource();
                String originalFilename = artifact.getFileLocation().getFileName();
                log.debug("Uploading HLS file from source: {} to MinIO", fileSource);

                try {
                    pushWithRetry(Path.of(fileSource),
                            artifact.getMultipartFile().getContentType(),
                            originalFilename,
                            properties.getVideoUploadRetry());
                    log.debug("Successfully uploaded HLS file: {}", originalFilename);

                } catch (Exception e) {
                    log.error("Error uploading HLS file: {}", originalFilename, e);
                }
            });
            log.info("Successfully uploaded {} HLS files to MinIO", artifacts.size());
        } finally {
            log.info("Cleaning up temporal files for {} artifacts", artifacts.size());
            artifacts.forEach(artifact -> {
                String fileSource = artifact.getFileLocation().getFileSource();
                log.trace("Deleting temp file: {}", fileSource);
                storageUtil.deleteFiles(Path.of(fileSource).toFile());
            });
            log.debug("Temporal files cleanup completed");
        }
    }

    private void pushWithRetry(Path file, String contentType, String fileNameWithPath, int retriesLeft) {
        log.trace("Entering pushWithRetry method for fileName: {}, retriesLeft: {}", fileNameWithPath, retriesLeft);
        try (InputStream is = Files.newInputStream(file)) {
            long fileSize = Files.size(file);
            log.debug("File size: {} bytes for fileName: {}", fileSize, fileNameWithPath);

            PutObjectArgs.Builder putObjectArgsBuilder = PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(fileNameWithPath)
                    .stream(is, fileSize, -1) // Set part size to -1 for auto detection
                    .contentType(contentType);

            log.info("Writing file: {} to MinIO bucket: {}", fileNameWithPath, minioConfig.getBucketName());
            minioClient.putObject(putObjectArgsBuilder.build());
            log.debug("Upload successful for file: {}", fileNameWithPath);

        } catch (IOException e) {
            if (retriesLeft > 0) {
                log.warn("IOException occurred during HLS file upload. Retries left: {}. Retrying for file: {}", 
                        retriesLeft, fileNameWithPath, e);
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("Thread interrupted during retry delay for file: {}", fileNameWithPath, ie);
                    throw new CustomException("INTERRUPTED", "Thread interrupted during retry delay: " + ie.getMessage());
                }
                pushWithRetry(file, contentType, fileNameWithPath, retriesLeft - 1);
            } else {
                log.error("Max retries reached for HLS file: {}. IOException occurred", fileNameWithPath, e);
                throw new CustomException("EOFEXCEPTION", "End of file reached unexpectedly after retries: " + e.getMessage());
            }
        } catch (MinioException | InvalidKeyException | IllegalArgumentException | NoSuchAlgorithmException e) {
            log.error("Configuration error occurred while uploading HLS file: {}", fileNameWithPath, e);
            throw new CustomException(ERROR_IN_CONFIGURATION, e.toString());
        } catch (Exception e) {
            log.error("Unexpected exception occurred while uploading HLS file: {}", fileNameWithPath, e);
            throw new CustomException("EXCEPTION", e.getMessage());
        }
    }
}
