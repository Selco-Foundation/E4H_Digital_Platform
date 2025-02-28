package org.egov.im.util;

import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Slf4j
@Component
public class VideoUtil {

    public String getScaleForQuality(String quality) {
        log.trace("Getting scale for quality: {}", quality);
        try {
            switch (quality) {
                case "144p": return "256:144";
                case "240p": return "426:240";
                case "480p": return "854:480";
                case "720p": return "1280:720";
                case "1080p": return "1920:1080";
                default:
                    log.error("Invalid quality requested: {}", quality);
                    throw new IllegalArgumentException("Invalid quality: " + quality);
            }
        } catch (Exception e) {
            log.error("Error getting scale for quality: {}. Error: {}", quality, e.getMessage());
            throw e;
        }
    }

    public int getBandwidthForQuality(String quality) {
        switch (quality) {
            case "144p": return 300000;
            case "240p": return 700000;
            case "480p": return 1500000;
            case "720p": return 3000000;
            case "1080p": return 6000000;
            default: throw new IllegalArgumentException("Invalid quality: " + quality);
        }
    }

    public String getResolutionForQuality(String quality) {
        switch (quality) {
            case "144p": return "256x144";
            case "240p": return "426x240";
            case "480p": return "854x480";
            case "720p": return "1280x720";
            case "1080p": return "1920x1080";
            default: throw new IllegalArgumentException("Invalid quality: " + quality);
        }
    }

    public int getBandwidthForResolution(int width, int height) {
        // Estimate bandwidth based on resolution
        int pixels = width * height;
        if (pixels <= 256 * 144) return 300000;
        if (pixels <= 426 * 240) return 700000;
        if (pixels <= 854 * 480) return 1500000;
        if (pixels <= 1280 * 720) return 3000000;
        if (pixels <= 1920 * 1080) return 6000000;
        return 8000000; // for higher resolutions
    }

    public MultipartFile convertFileToMultipartFile(File file) {
        byte[] fileContent = null;
        try {
            fileContent = Files.readAllBytes(file.toPath());

            return ByteArrayMultipartFile.builder()
                    .content(fileContent)
                    .name(file.getName())
                    .originalFilename(file.getName())
                    .contentType(Files.probeContentType(file.toPath()))
                    .build();
        } catch (IOException e) {
            throw new CustomException("ERROR_CONVERTING_TO_MULTIPARTFILE", e.getMessage());
        }
    }
}
