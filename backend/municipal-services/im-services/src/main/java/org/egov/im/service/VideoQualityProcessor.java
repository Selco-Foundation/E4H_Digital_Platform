package org.egov.im.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.im.settings.VideoQualitySettings;
import org.egov.im.util.VideoUtil;
import org.egov.im.web.models.ProcessingContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoQualityProcessor {

    private final FFmpegService fFmpegService;
    private final VideoUploaderService uploaderService;
    private final VideoUtil videoUtil;

    @Async
    public CompletableFuture<Void> processQualitiesInParallel(
            ProcessingContext context, File inputFile, Path outputPath, List<VideoQualitySettings> qualities) {
        log.info("Processing videoId: {} and qualities: {}", context.getVideoId(), qualities);

        List<CompletableFuture<Void>> uploadFutures = qualities.stream()
                .map(quality -> fFmpegService.processQuality(context, inputFile.getAbsolutePath(), outputPath, quality)
                        .thenApply(outputFilePath ->
                                videoUtil.convertToMultipartFiles(context, outputPath, outputFilePath))
                        .thenCompose(multipartFiles ->
                                uploaderService.uploadProcessedFile(context, multipartFiles))
                        .exceptionally(ex -> {
                            log.error("Error processing quality: {}", quality, ex);
                            return null;
                        }))
                .toList();

        return CompletableFuture.allOf(uploadFutures.toArray(new CompletableFuture[0]));
    }

    public List<MultipartFile> processQuality(
            ProcessingContext context, File inputFile, Path outputPath, VideoQualitySettings quality) {
        log.info("Processing videoId: {} and quality: {}", context.getVideoId(), quality);
        String outputFilePath = fFmpegService.processQuality(context, inputFile.getAbsolutePath(), outputPath, quality).join();
        return videoUtil.convertToMultipartFiles(context, outputPath, outputFilePath);
    }
}
