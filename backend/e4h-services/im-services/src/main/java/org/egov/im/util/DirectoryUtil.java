package org.egov.im.util;

import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


@Slf4j
@Component
public class DirectoryUtil {

    /**
     * Ensures the output directory exists, creates it if necessary.
     *
     * @param path directory path where HLS chunks will be stored.
     */
    public Path createDirectory(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            log.debug("Creating output directory: {}", path);
            if (!dir.mkdirs()) {
                log.error("Failed to create output directory: {}", path);
                throw new CustomException("Failed to create output directory: ", path);
            }
        }
        return dir.toPath();
    }

    /**
     * Creates a file at the specified path
     *
     * @param path the path where the file should be created.
     */
    public Path createFile(Path path) {
        try {
            return Files.createFile(path);
        } catch (IOException e) {
            log.error("Failed to create output file: {}", path);
            throw new CustomException("Failed to create output file: ", path.toString());
        }
    }
}
