package org.egov.processor.service;

import org.egov.processor.models.ProcessingContext;
import org.egov.processor.settings.VideoQualitySettings;

import java.nio.file.Path;

public interface FFMpegService {

    String processQuality(ProcessingContext context, String inputPath, Path outputPath, VideoQualitySettings videoQuality);
}
