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
import org.springframework.web.multipart.MultipartFile;

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
        try {
            artifacts.forEach(artifact -> {
                String fileSource = artifact.getFileLocation().getFileSource();
                String originalFilename = artifact.getFileLocation().getFileName();

                try {
                    pushWithRetry(Path.of(fileSource),
                            artifact.getMultipartFile().getContentType(),
                            originalFilename,
                            properties.getVideoUploadRetry());

                } catch (Exception e) {
                    log.error("Error uploading file: {}", originalFilename, e);
                }
            });
        } finally {
            log.info("cleaning up temporal files");
            artifacts.forEach(artifact -> {
                String fileSource = artifact.getFileLocation().getFileSource();
                storageUtil.deleteFiles(Path.of(fileSource).toFile());
            });
        }
    }

    private void pushWithRetry(Path file, String contentType, String fileNameWithPath, int retriesLeft) {
        try (InputStream is = Files.newInputStream(file)) {
            long fileSize = Files.size(file);

            PutObjectArgs.Builder putObjectArgsBuilder = PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(fileNameWithPath)
                    .stream(is, fileSize, -1) // Set part size to -1 for auto detection
                    .contentType(contentType);

            log.info("Writing file: {} to S3", String.format("%s/%s", minioConfig.getBucketName(), fileNameWithPath));
            minioClient.putObject(putObjectArgsBuilder.build());
            log.debug("Upload Successful");

        } catch (IOException e) {
            if (retriesLeft > 0) {
                log.warn("EOFException occurred. Retries left: {}. Retrying...", retriesLeft, e);
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new CustomException("INTERRUPTED", "Thread interrupted during retry delay: " + ie.getMessage());
                }
                pushWithRetry(file, contentType, fileNameWithPath, retriesLeft - 1);
            } else {
                log.error("Max retries reached for file: {}", fileNameWithPath, e);
                throw new CustomException("EOFEXCEPTION", "End of file reached unexpectedly after retries: " + e.getMessage());
            }
        } catch (MinioException | InvalidKeyException | IllegalArgumentException | NoSuchAlgorithmException e) {
            log.error("Error occurred: ", e);
            throw new CustomException(ERROR_IN_CONFIGURATION, e.toString());
        } catch (Exception e) {
            log.error("Exception occurred: ", e);
            throw new CustomException("EXCEPTION", e.getMessage());
        }
    }
}
