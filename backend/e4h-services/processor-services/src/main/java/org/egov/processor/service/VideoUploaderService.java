package org.egov.processor.service;

import org.egov.processor.models.ProcessingContext;
import org.egov.processor.models.storage.StorageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VideoUploaderService {
    StorageResponse uploadProcessedFile(ProcessingContext context, List<MultipartFile> multipartFiles);
}
