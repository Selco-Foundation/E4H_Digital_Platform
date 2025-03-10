package org.egov.filestore.domain.service;

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
import org.egov.filestore.validator.StorageValidator;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
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
        String folderName = getFolderName(module, tenantId);
        List<Artifact> artifacts = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                String originalFileName = file.getOriginalFilename();
                if (originalFileName == null) {
                    throw new CustomException("INVALID_FILE", "File name is missing");
                }

                // Generate file name
                String fileName = isHLS
                        ? String.format("%s%s", getFolderNameForVideo(tenantId), originalFileName)
                        : folderName + System.currentTimeMillis() + getRandomFileSuffix(originalFileName);

                // Generate file location
                String id = idGeneratorService.getId();
                FileLocation fileLocation = new FileLocation(id, module, tag, tenantId, fileName, null);

                // Read file content
                String fileContent = IOUtils.toString(file.getInputStream(), fileStoreConfig.getImageCharsetType());

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

                // Validate artifact
                storageValidator.validate(artifact);

                // Set thumbnail if it's an image
                if (isImageFile(artifact)) {
                    setThumbnailImages(artifact);
                }

                artifacts.add(artifact);
            } catch (IOException e) {
                log.error("I/O Exception while mapping files to artifact: {}", e.getMessage(), e);
                throw new CustomException("FILE_MAPPING_ERROR", "Error processing file: " + e.getMessage());
            }
        }

        return artifacts;
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
        return String.format("%s/%s/",
                minioConfig.getBucketName(),
                tenantId);
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
