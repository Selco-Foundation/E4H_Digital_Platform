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
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

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
    public StorageResponse uploadToFileStorage(List<MultipartFile> filesToStore,
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
    public StorageResponse uploadToHLSFileStorage(List<MultipartFile> filesToStore,
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
        if(configuration.getFileStoreHost().endsWith("/")) {
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
        File newFile = tempFile.toFile();

        // Check if the file does not exist
        if (!newFile.exists()) {
            log.warn("The file {} does not exist. creating file", newFile.getAbsolutePath());
            boolean fileCreated = newFile.createNewFile();
            log.info("file created: {}", fileCreated );
            //  throw new CustomException("File does not exist:",  newFile.getAbsolutePath());
        }

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
        String extension =  getFileExtension(resource);
        // Create custom temp file using the pre-initialized temp directory
        String uniqueFileName = String.format("%s_%s%s", "video", UUID.randomUUID(), extension);
        return new File(tempDir, uniqueFileName);
    }
}
