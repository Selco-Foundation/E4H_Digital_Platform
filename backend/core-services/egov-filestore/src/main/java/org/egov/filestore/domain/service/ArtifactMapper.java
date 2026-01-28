package org.egov.filestore.domain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.egov.filestore.config.FileStoreConfig;
import org.egov.filestore.config.Properties;
import org.egov.filestore.domain.model.Artifact;
import org.egov.filestore.domain.model.FileLocation;
import org.egov.filestore.repository.impl.CloudFileMgrUtils;
import org.egov.filestore.repository.impl.minio.MinioConfig;
import org.egov.filestore.utils.StorageUtil;
import org.egov.filestore.validator.StorageValidator;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RequiredArgsConstructor
@Slf4j
@Component
public class ArtifactMapper {

    private final IdGeneratorService idGeneratorService;
    private final StorageValidator storageValidator;
    private final MinioConfig minioConfig;
    private final Properties props;
    private final FileStoreConfig fileStoreConfig;
    private final CloudFileMgrUtils util;
    private final StorageUtil storageUtil;

    private File tempDir;

    private static final String INPUT_DIR = "input";

    private ObjectMapper objectMapper;

    @PostConstruct
    private void initTempFile() {
        Path customTempDir = Paths.get(System.getProperty("user.dir"), INPUT_DIR);
        tempDir = new File(customTempDir.toAbsolutePath().toString());
        if (!tempDir.exists()) {
            tempDir.mkdirs();  // Ensure directory exists
            log.info("Created temporary directory at: {}", customTempDir);
        } else {
            log.info("Temporary directory already exists at: {}", customTempDir);
        }
    }


    /**
     * Maps given files to Artifact objects.
     *
     * @param files    List of files to map.
     * @param module   Module name.
     * @param tag      Tag for the files.
     * @param tenantId Tenant ID.
     * @param isHLS    Boolean flag indicating if it's an HLS artifact.
     * @return List of mapped artifacts.
     */
    private List<Artifact> mapFilesToArtifact(List<MultipartFile> files, String module, String tag, String tenantId, boolean isHLS) {
        List<Artifact> artifacts = new ArrayList<>();
        String folderName = getFolderName(module, tenantId);

        for (MultipartFile file : files) {
            try {
                Artifact artifact = createArtifact(file, module, tag, tenantId, isHLS, folderName);
                artifacts.add(postProcessArtifact(artifact));
            } catch (IOException e) {
                log.error("I/O Exception while mapping files to artifact: {}", e.getMessage(), e);
                throw new CustomException("FILE_MAPPING_ERROR", "Error processing file: " + e.getMessage());
            }
        }

        return artifacts;
    }

    /**
     * Creates an {@link Artifact} instance for the given file.
     */
    private Artifact createArtifact(MultipartFile file,
                                    String module,
                                    String tag,
                                    String tenantId,
                                    boolean isHLS,
                                    String folderName) throws IOException {

        String originalFileName = extractOriginalFileName(file);
        String fileName = generateFileName(originalFileName, tenantId, isHLS, folderName);
        String source = resolveSourcePath(file, isHLS);

        FileLocation fileLocation = buildFileLocation(module, tag, tenantId, fileName, source);
        String fileContent = readFileContent(file);

        return Artifact.builder()
                .fileContentInString(fileContent)
                .multipartFile(file)
                .fileLocation(fileLocation)
                .build();
    }

    /**
     * Performs validation and image thumbnail generation for the given artifact.
     */
    private Artifact postProcessArtifact(Artifact artifact) {
        Artifact validatedArtifact = applyInsertableFlag(artifact);

        storageValidator.validate(validatedArtifact);

        if (isImageFile(validatedArtifact)) {
            setThumbnailImages(validatedArtifact);
        }

        return validatedArtifact;
    }

    /**
     * Reads and validates the original file name.
     */
    private String extractOriginalFileName(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null) {
            throw new CustomException("INVALID_FILE", "File name is missing");
        }
        return originalFileName;
    }

    /**
     * Generates the full file name for storage.
     */
    private String generateFileName(String originalFileName, String tenantId, boolean isHLS, String folderName) {
        if (isHLS) {
            return String.format("%s%s", getFolderNameForVideo(tenantId), originalFileName);
        }

        return folderName + System.currentTimeMillis() + getRandomFileSuffix(originalFileName);
    }

    /**
     * Resolves the source path for the file in case of HLS uploads.
     */
    private String resolveSourcePath(MultipartFile file, boolean isHLS) throws IOException {
        if (!isHLS) {
            return null;
        }
        return storageUtil.createTempFile(tempDir, file.getResource()).getAbsolutePath();
    }

    /**
     * Builds the {@link FileLocation} object.
     */
    private FileLocation buildFileLocation(String module, String tag, String tenantId, String fileName, String source) {
        String id = idGeneratorService.getId();
        return new FileLocation(id, module, tag, tenantId, fileName, source);
    }

    /**
     * Reads the file content using the configured charset.
     */
    private String readFileContent(MultipartFile file) throws IOException {
        return IOUtils.toString(file.getInputStream(), fileStoreConfig.getImageCharsetType());
    }

    /**
     * Sets the insertable flag based on file type (for HLS chunks).
     */
    private Artifact applyInsertableFlag(Artifact artifact) {
        String originalFileName = artifact.getMultipartFile().getOriginalFilename();
        if (originalFileName == null) {
            return artifact;
        }

        // make video chunks not insertable
        boolean isChunk = originalFileName.endsWith(".ts") || originalFileName.endsWith("playlist.m3u8");
        return isChunk ? artifact : artifact.withInsertable(true);
    }

    /**
     * Maps regular files to Artifact objects.
     */
    public List<Artifact> mapFilesToArtifact(List<MultipartFile> files, String module, String tag, String tenantId) {
        return mapFilesToArtifact(files, module, tag, tenantId, false);
    }

    /**
     * Maps HLS files to Artifact objects.
     */
    public List<Artifact> mapHLSArtifact(List<MultipartFile> files, String module, String tag, String tenantId) {
        return mapFilesToArtifact(files, module, tag, tenantId, true);
    }

    /**
     * Sets thumbnail images for an artifact if it's an image.
     */
    private void setThumbnailImages(Artifact artifact) {
        try {
            String inputStreamAsString = artifact.getFileContentInString();
            InputStream ipStreamForImg = IOUtils.toInputStream(inputStreamAsString, fileStoreConfig.getImageCharsetType());

            Map<String, BufferedImage> thumbnails = util.createVersionsOfImage(ipStreamForImg,
                    extractFileName(artifact.getFileLocation().getFileName()));

            artifact.setThumbnailImages(thumbnails);
        } catch (Exception e) {
            log.error("Failed to generate thumbnail images for file: {}", artifact.getFileLocation().getFileName(), e);
            throw new CustomException("THUMBNAIL_GENERATION_ERROR", "Error generating thumbnails");
        }
    }

    /**
     * Generates a folder name based on module and tenant.
     */
    private String getFolderName(String module, String tenantId) {
        Calendar calendar = Calendar.getInstance();
        return String.format("%s/%s/%s/%d/",
                minioConfig.getBucketName(),
                tenantId,
                module,
                calendar.get(Calendar.DATE));
    }

    /**
     * Generates a folder name based on module and tenant.
     */
    private String getFolderNameForVideo(String tenantId) {
        return String.format("%s/", tenantId);
    }

    /**
     * Checks if an artifact is an image.s
     */
    private boolean isImageFile(Artifact artifact) {
        return fileStoreConfig.getImageFormats()
                .contains(FilenameUtils.getExtension(artifact.getMultipartFile().getOriginalFilename()));
    }

    /**
     * Generates a random file suffix.
     */
    private String getRandomFileSuffix(String originalFileName) {
        String extension = FilenameUtils.getExtension(originalFileName);
        String randomString = RandomStringUtils.random(props.getFilenameLength(), props.getUseLetters(), props.getUseNumbers());
        return randomString + "." + extension;
    }

    /**
     * Extracts the file name from a file path.
     */
    private String extractFileName(String fullPath) {
        return fullPath.substring(fullPath.indexOf('/') + 1);
    }
}
