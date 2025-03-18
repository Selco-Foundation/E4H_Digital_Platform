package org.egov.filestore.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.UUID;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class StorageUtil {

    private ObjectMapper objectMapper;

    @Autowired
    public StorageUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public RequestInfo getRequestInfo(String requestInfoBase64) {
        RequestInfo requestInfo = null;
        try {
            //String decoded = new String(Base64.getDecoder().decode(requestInfoBase64));
            if (requestInfoBase64 != null)
                requestInfo = objectMapper.readValue(requestInfoBase64, RequestInfo.class);
            else
                return new RequestInfo();
        } catch (IOException e) {

            log.error(e.getMessage());
            throw new CustomException("INVALID_REQ_INFO", "Failed to deserialization the requestinfo object");
        }
        return requestInfo;
    }

	/*public void enrichAuditDetails(RequestInfo requestInfo, Artifact artifact) {
		if (requestInfo.getUserInfo() != null) {
			artifact.setCreatedBy(requestInfo.getUserInfo().getUuid());
			artifact.setLastModifiedBy(requestInfo.getUserInfo().getUuid());
		}
		artifact.setCreatedTime(System.currentTimeMillis());
		artifact.setLastModifiedTime(System.currentTimeMillis());
	}*/

    // file extension
    public String getFileExtension(Resource resource) {
        String originalFilename = resource.getFilename();
        return (originalFilename != null && originalFilename.contains("."))
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".tmp";
    }

    // file extension
    public File createTempFile(File tempDir, Resource resource) {
        try {
            String extension = getFileExtension(resource);
            // Create custom temp file using the pre-initialized temp directory
            String uniqueFileName = String.format("%s_%s%s", "video", UUID.randomUUID(), extension);
            File newFile = new File(tempDir, uniqueFileName);
            boolean fileCreated = newFile.createNewFile();
            if (!fileCreated) {
                // If the file cannot be created, throw a custom exception
                log.error("Failed to create the file {}", newFile.getAbsolutePath());
                throw new CustomException("Failed to create the file: ", newFile.getAbsolutePath());
            }
            log.info("File created: {}", newFile.getAbsolutePath());
            writeFileToTempFile(resource, newFile.toPath());
            return newFile;
        } catch (IOException e) {
            throw new CustomException("ERROR_CREATING_TEMP_FILE", e.getMessage());
        }

    }

    private void writeFileToTempFile(Resource resource, Path tempFile) throws IOException {
        try {
            File newFile = tempFile.toFile();

            // Check if the file does not exist
            if (!newFile.exists()) {
                log.warn("The file {} does not exist. Creating file.", newFile.getAbsolutePath());
                createTempFile(tempFile.toFile(), resource);
            }

            // Writing content to new file
            try (FileOutputStream fileOutput = new FileOutputStream(newFile);
                 InputStream input = resource.getInputStream();
                 BufferedInputStream bufferedInput = new BufferedInputStream(input);
                 BufferedOutputStream bufferedOutput = new BufferedOutputStream(fileOutput)) {
                byte[] dataBuffer = new byte[16384];  // 16KB buffer for read/write operations
                int bytesProcessed;
                while ((bytesProcessed = bufferedInput.read(dataBuffer)) != -1) {
                    bufferedOutput.write(dataBuffer, 0, bytesProcessed);
                }
            }
        } catch (NoSuchFileException exception) {
            log.error("ERROR_HANDLING_TEMP_FILE: {}", tempFile.toAbsolutePath(), exception);
            throw new CustomException(
                    String.format("ERROR_HANDLING_TEMP_FILE:- %s ", tempFile.toAbsolutePath()), exception.getMessage());
        } catch (IOException exception) {
            log.error("I/O error occurred: {}", tempFile.toAbsolutePath(), exception);
            throw new CustomException(
                    String.format("IO_ERROR_HANDLING_TEMP_FILE: %s- ", tempFile.toAbsolutePath()), exception.getMessage());
        } catch (Exception exception) {
            log.error("Unexpected error during file processing: {}", tempFile.toAbsolutePath(), exception);
            throw new CustomException(
                    String.format("Unexpected error during file processing: %s", tempFile.toAbsolutePath()), exception.getMessage());
        }

    }

    /**
     * Deletes all files in the provided list.
     * Logs a message for each file deleted and handles any deletion errors.
     */
    @Async
    public void deleteFiles(File file) {
        try {
            if (file.exists()) {
                Files.delete(file.toPath());
                log.info("Deleted file: {}", file.getAbsolutePath());
            } else {
                log.warn("File does not exist: {}", file.getAbsolutePath());
            }
        } catch (IOException e) {
            log.warn("Failed to delete file: {}", file.getAbsolutePath(), e);
        }
    }
}
