package org.egov.processor.service;

import org.egov.processor.models.ProcessingContext;
import org.egov.processor.models.storage.StorageProcessingContext;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

public interface StorageService {

    List<File> createTempFiles(List<MultipartFile> files);

    void createAndSaveChunks(String fileStoreId, File resource, ProcessingContext context);

    void processAndStoreFiles(StorageProcessingContext storageProcessingContext);
}
