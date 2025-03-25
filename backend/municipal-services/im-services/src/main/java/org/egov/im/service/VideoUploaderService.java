package org.egov.im.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.im.util.StorageUtil;
import org.egov.im.util.VideoUtil;
import org.egov.im.web.models.ProcessingContext;
import org.egov.im.web.models.storage.StorageResponse;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@Component
@Slf4j
@RequiredArgsConstructor
public class VideoUploaderService {

    private final StorageUtil storageUtil;
    private final VideoUtil videoUtil;

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
