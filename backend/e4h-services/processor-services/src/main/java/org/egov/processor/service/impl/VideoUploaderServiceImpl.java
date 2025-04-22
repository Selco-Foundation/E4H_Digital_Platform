package org.egov.processor.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.processor.models.ProcessingContext;
import org.egov.processor.models.storage.StorageResponse;
import org.egov.processor.service.VideoUploaderService;
import org.egov.processor.utils.StorageUtil;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoUploaderServiceImpl implements VideoUploaderService {

    private final StorageUtil storageUtil;

    public StorageResponse uploadProcessedFile(ProcessingContext context, List<MultipartFile> multipartFiles) {
        try {
            // Upload files to HLS storage
            return storageUtil.uploadToHLSFileStorage(multipartFiles, context);

        } catch (IOException e) {
            log.error("Error uploading processed files: {}", e.getMessage(), e);
            throw new CustomException("Error uploading files", e.getMessage());
        }
    }
}
