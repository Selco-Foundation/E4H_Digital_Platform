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
        log.trace("Method invoked: uploadProcessedFile, videoId: {}, file count: {}", context.getVideoId(), multipartFiles != null ? multipartFiles.size() : 0);
        try {
            log.info("Uploading {} processed files to HLS storage for videoId: {}", multipartFiles.size(), context.getVideoId());
            StorageResponse response = storageUtil.uploadToHLSFileStorage(multipartFiles, context);
            log.info("Successfully uploaded files to HLS storage for videoId: {}", context.getVideoId());
            return response;

        } catch (IOException e) {
            log.error("Error uploading processed files for videoId: {}", context.getVideoId(), e);
            throw new CustomException("Error uploading files", e.getMessage());
        }
    }
}
