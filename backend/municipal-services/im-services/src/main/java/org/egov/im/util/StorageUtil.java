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

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Component
@Slf4j
public class StorageUtil {

    private final IMConfiguration configuration;
    private final ServiceRequestRepository serviceRequestRepository;

    /**
     * Calls File-store service to store files and returns list of file ids
     * @param filesToStore
     *
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
     * Returns the url for file-storage upload endpoint
     *
     * @return url for filestore upload endpoint
     */
    public StringBuilder getFileStoreURL() {
        return new StringBuilder().append(configuration.getFileStoreHost())
                .append(configuration.getFileStoreUploadEndpoint());
    }


    /**
     * Fetches and returns the requested file as a Resource
     * @return the fetched file as a Resource
     */
    public Resource getFile(String tenantId, String fileStoreId) {
        ResponseEntity<Resource> response = serviceRequestRepository
                .fetchFile(getFileStoreURL().toString(), tenantId, fileStoreId);
        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        }
        throw new CustomException("Error fetching file", fileStoreId);
    }
}
