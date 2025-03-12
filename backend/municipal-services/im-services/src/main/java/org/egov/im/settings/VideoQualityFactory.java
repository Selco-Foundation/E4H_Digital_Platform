package org.egov.im.settings;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import jakarta.annotation.PostConstruct;
import lombok.Getter;

@Component
public class VideoQualityFactory {

    @Getter
    private static final Map<String, VideoQualitySettings> dynamicQualities = new HashMap<>();

    @PostConstruct
    private void init() {
        addQuality("LOWEST_144P", VideoQualitySettings.of("256x144", "144p", 30, "64k", false));
        addQuality("LOW_240P", VideoQualitySettings.of("426x240", "240p", 28, "96k", false));
        addQuality("SD_480P", VideoQualitySettings.of("854x480", "480p", 26, "128k", false));
        addQuality("HD_720P", VideoQualitySettings.of("1280x720", "720p", 23, "160k", false));
        addQuality("FHD_1080P", VideoQualitySettings.of("1920x1080", "1080p", 23, "192k", false));
    }

    private static void addQuality(String name, VideoQualitySettings quality) {
        dynamicQualities.put(name, quality);
    }

    public Collection<VideoQualitySettings> getAllQualities() {
        return dynamicQualities.values();
    }

    public VideoQualitySettings getQualitySettings(String key) {
        return dynamicQualities.get(key);
    }
}
