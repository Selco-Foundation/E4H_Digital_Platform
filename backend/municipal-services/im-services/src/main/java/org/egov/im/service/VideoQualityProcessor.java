package org.egov.im.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.im.settings.VideoQualitySettings;
import org.egov.im.util.VideoUtil;
import org.egov.im.web.models.ProcessingContext;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoQualityProcessor {

    private final FFmpegService fFmpegService;
    private final VideoUtil videoUtil;

    public List<MultipartFile> processQuality(
            ProcessingContext context, File inputFile, Path outputPath, List<VideoQualitySettings> qualities) {
        log.info("Processing videoId: {} and qualities: {}", context.getVideoId(), qualities);

        List<MultipartFile> multipartFilesList = new ArrayList<>();

        for (VideoQualitySettings quality : qualities) {
            try {
                String outputFilePath = fFmpegService.processQuality(
                        context, inputFile.getAbsolutePath(), outputPath, quality);

                List<MultipartFile> multipartFiles = videoUtil.convertToMultipartFiles(
                        context, outputPath, outputFilePath);

                multipartFilesList.addAll(multipartFiles);

            } catch (Exception ex) {
                log.error("Error processing quality: {}", quality, ex);
            }
        }

        return multipartFilesList;
    }

    public List<MultipartFile> processQuality(
            ProcessingContext context, File inputFile, Path outputPath, VideoQualitySettings quality) {
        log.info("Processing videoId: {} and quality: {}", context.getVideoId(), quality);
        String outputFilePath = fFmpegService.processQuality(context, inputFile.getAbsolutePath(), outputPath, quality);
        return videoUtil.convertToMultipartFiles(context, outputPath, outputFilePath);
    }
}
