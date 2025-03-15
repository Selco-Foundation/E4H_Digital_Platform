package org.egov.im.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.im.util.StorageUtil;
import org.egov.im.util.VideoUtil;
import org.egov.im.web.models.ProcessingContext;
import org.egov.tracer.model.CustomException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;


@Component
@Slf4j
@RequiredArgsConstructor
public class VideoUploaderService {

    private final StorageUtil storageUtil;
    private final VideoUtil videoUtil;

    @Async
    public CompletableFuture<Void> uploadProcessedFile(ProcessingContext context, List<MultipartFile> multipartFiles) {
        try {
            // Upload files to HLS storage
            storageUtil.uploadToHLSFileStorage(multipartFiles, context);

        } catch (IOException e) {
            log.error("Error uploading processed files: {}", e.getMessage(), e);
            throw new CustomException("Error uploading files", e.getMessage());
        }
        return CompletableFuture.completedFuture(null);
    }
}
