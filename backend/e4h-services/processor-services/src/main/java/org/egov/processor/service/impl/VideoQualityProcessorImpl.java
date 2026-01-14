package org.egov.processor.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.processor.models.ProcessingContext;
import org.egov.processor.service.FFMpegService;
import org.egov.processor.service.VideoQualityProcessor;
import org.egov.processor.settings.VideoQualitySettings;
import org.egov.processor.utils.VideoUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoQualityProcessorImpl implements VideoQualityProcessor {

    private final FFMpegService fFmpegService;

    private final VideoUtil videoUtil;

    public List<MultipartFile> processQuality(
            ProcessingContext context, File inputFile, Path outputPath, VideoQualitySettings quality) {
        log.trace("Method invoked: processQuality, videoId: {}, quality: {}", context.getVideoId(), quality.getLabel());
        log.info("Processing quality level: {} for videoId: {}", quality.getLabel(), context.getVideoId());
        String outputFilePath = fFmpegService.processQuality(context, inputFile.getAbsolutePath(), outputPath, quality);
        log.debug("FFmpeg processing completed, output path: {}", outputFilePath);
        log.trace("Converting output files to multipart files");
        return videoUtil.convertToMultipartFiles(context, outputPath, outputFilePath);
    }
}
