package org.egov.processor.service;

import org.egov.processor.models.ProcessingContext;
import org.egov.processor.settings.VideoQualitySettings;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public interface VideoQualityProcessor {

    List<MultipartFile> processQuality(
            ProcessingContext context, File inputFile, Path outputPath, VideoQualitySettings quality);
}
