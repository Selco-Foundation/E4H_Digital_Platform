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
        log.trace("Entering initTempFile method");
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
        log.trace("Entering mapFilesToArtifact method with module: {}, tag: {}, tenantId: {}, fileCount: {}, isHLS: {}", 
                module, tag, tenantId, files.size(), isHLS);
        String folderName = getFolderName(module, tenantId);
        log.debug("Generated folderName: {}", folderName);
        List<Artifact> artifacts = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                log.trace("Processing file: {}", file.getOriginalFilename());
                String originalFileName = file.getOriginalFilename();
                if (originalFileName == null) {
                    log.error("File name is missing for file in module: {}, tag: {}", module, tag);
                    throw new CustomException("INVALID_FILE", "File name is missing");
                }

                // Generate file name
                String fileName = isHLS
                        ? String.format("%s%s", getFolderNameForVideo(tenantId), originalFileName)
                        : folderName + System.currentTimeMillis() + getRandomFileSuffix(originalFileName);
                log.debug("Generated fileName: {} for originalFileName: {}", fileName, originalFileName);

                // Generate file location
                String source = isHLS
                        ? storageUtil.createTempFile(tempDir, file.getResource()).getAbsolutePath()
                        : null;
                if (isHLS) {
                    log.debug("Created temp file for HLS at: {}", source);
                }

                String id = idGeneratorService.getId();
                log.debug("Generated fileStoreId: {} for fileName: {}", id, originalFileName);
                FileLocation fileLocation =
                        new FileLocation(id, module, tag, tenantId, fileName, source);

                // Read file content
                String fileContent = IOUtils.toString(file.getInputStream(), fileStoreConfig.getImageCharsetType());
                log.debug("Read file content, size: {} bytes", fileContent.length());

                // Create artifact
                Artifact artifact = Artifact.builder()
                        .fileContentInString(fileContent)
                        .multipartFile(file)
                        .fileLocation(fileLocation)
                        .build();

                //make video chunks not insertable or
                artifact = !originalFileName.endsWith(".ts") && !originalFileName.endsWith("playlist.m3u8")
                        ? artifact.withInsertable(true)
                        : artifact;
                log.debug("Artifact insertable flag set to: {}", artifact.isInsertable());

                // Validate artifact
                storageValidator.validate(artifact);
                log.trace("Artifact validated successfully for fileName: {}", originalFileName);

                // Set thumbnail if it's an image
                if (isImageFile(artifact)) {
                    log.debug("File is an image, generating thumbnails for: {}", originalFileName);
                    setThumbnailImages(artifact);
                }

                artifacts.add(artifact);
                log.trace("Successfully mapped file to artifact: {}", originalFileName);
            } catch (IOException e) {
                log.error("I/O Exception while mapping files to artifact for fileName: {}, module: {}, tag: {}", 
                        file.getOriginalFilename(), module, tag, e);
                throw new CustomException("FILE_MAPPING_ERROR", "Error processing file: " + e.getMessage());
            }
        }

        log.info("Successfully mapped {} files to artifacts for module: {}, tag: {}", artifacts.size(), module, tag);
        return artifacts;
    }

    /**
     * Maps regular files to Artifact objects.
     */
    public List<Artifact> mapFilesToArtifact(List<MultipartFile> files, String module, String tag, String tenantId) {
        log.trace("Entering mapFilesToArtifact (regular) method");
        return mapFilesToArtifact(files, module, tag, tenantId, false);
    }

    /**
     * Maps HLS files to Artifact objects.
     */
    public List<Artifact> mapHLSArtifact(List<MultipartFile> files, String module, String tag, String tenantId) {
        log.trace("Entering mapHLSArtifact method");
        return mapFilesToArtifact(files, module, tag, tenantId, true);
    }

    /**
     * Sets thumbnail images for an artifact if it's an image.
     */
    private void setThumbnailImages(Artifact artifact) {
        log.trace("Entering setThumbnailImages method for fileName: {}", 
                artifact.getFileLocation().getFileName());
        try {
            String inputStreamAsString = artifact.getFileContentInString();
            InputStream ipStreamForImg = IOUtils.toInputStream(inputStreamAsString, fileStoreConfig.getImageCharsetType());

            Map<String, BufferedImage> thumbnails = util.createVersionsOfImage(ipStreamForImg,
                    extractFileName(artifact.getFileLocation().getFileName()));
            log.debug("Generated {} thumbnail versions for fileName: {}", 
                    thumbnails.size(), artifact.getFileLocation().getFileName());

            artifact.setThumbnailImages(thumbnails);
            log.trace("Thumbnail images set successfully for fileName: {}", 
                    artifact.getFileLocation().getFileName());
        } catch (Exception e) {
            log.error("Failed to generate thumbnail images for file: {}", artifact.getFileLocation().getFileName(), e);
            throw new CustomException("THUMBNAIL_GENERATION_ERROR", "Error generating thumbnails");
        }
    }

    /**
     * Generates a folder name based on module and tenant.
     */
    private String getFolderName(String module, String tenantId) {
        log.trace("Entering getFolderName method with module: {}, tenantId: {}", module, tenantId);
        Calendar calendar = Calendar.getInstance();
        String folderName = String.format("%s/%s/%s/%d/",
                minioConfig.getBucketName(),
                tenantId,
                module,
                calendar.get(Calendar.DATE));
        log.debug("Generated folderName: {}", folderName);
        return folderName;
    }

    /**
     * Generates a folder name based on module and tenant.
     */
    private String getFolderNameForVideo(String tenantId) {
        log.trace("Entering getFolderNameForVideo method with tenantId: {}", tenantId);
        String folderName = String.format("%s/", tenantId);
        log.debug("Generated video folderName: {}", folderName);
        return folderName;
    }

    /**
     * Checks if an artifact is an image.
     */
    private boolean isImageFile(Artifact artifact) {
        log.trace("Entering isImageFile method");
        String extension = FilenameUtils.getExtension(artifact.getMultipartFile().getOriginalFilename());
        boolean isImage = fileStoreConfig.getImageFormats().contains(extension);
        log.debug("File isImage: {} for extension: {}", isImage, extension);
        return isImage;
    }

    /**
     * Generates a random file suffix.
     */
    private String getRandomFileSuffix(String originalFileName) {
        log.trace("Entering getRandomFileSuffix method");
        String extension = FilenameUtils.getExtension(originalFileName);
        String randomString = RandomStringUtils.random(props.getFilenameLength(), props.getUseLetters(), props.getUseNumbers());
        String suffix = randomString + "." + extension;
        log.debug("Generated random file suffix for extension: {}", extension);
        return suffix;
    }

    /**
     * Extracts the file name from a file path.
     */
    private String extractFileName(String fullPath) {
        log.trace("Entering extractFileName method with fullPath: {}", fullPath);
        String fileName = fullPath.substring(fullPath.indexOf('/') + 1);
        log.debug("Extracted fileName: {} from fullPath", fileName);
        return fileName;
    }
}
