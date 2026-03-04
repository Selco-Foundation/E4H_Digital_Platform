package org.egov.filestore.persistence.repository;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.filestore.domain.model.FileInfo;
import org.egov.filestore.domain.model.FileLocation;
import org.egov.filestore.domain.model.Resource;
import org.egov.filestore.persistence.entity.Artifact;
import org.egov.filestore.repository.CloudFileManagerV2;
import org.egov.filestore.repository.CloudFilesManager;
import org.egov.filestore.repository.impl.AzureBlobStorageImpl;
import org.egov.filestore.repository.impl.minio.MinioConfig;
import org.egov.filestore.repository.impl.minio.MinioRepository;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArtifactRepository {

    private final FileStoreJpaRepository fileStoreJpaRepository;
    private final CloudFilesManager cloudFilesManager;
    private final CloudFileManagerV2 cloudFileManagerV2;
    private final MinioConfig minioConfig;

    @Value("${isAzureStorageEnabled}")
    private Boolean isAzureStorageEnabled;

    @Value("${source.azure.blob}")
    private String azureBlobSource;

    public List<String> save(List<org.egov.filestore.domain.model.Artifact> artifacts, RequestInfo requestInfo) {
        log.trace("Entering save method with artifactCount: {}", artifacts.size());
        log.info("Saving {} artifacts to cloud storage and database", artifacts.size());
        cloudFilesManager.saveFiles(artifacts);
        log.debug("Files saved to cloud storage successfully");
        List<Artifact> artifactEntities = new ArrayList<>();
        artifacts.forEach(artifact -> artifactEntities.add(mapToEntity(artifact, requestInfo)));
        log.debug("Mapped {} artifacts to entities", artifactEntities.size());
        if (artifactEntities.isEmpty()) {
            log.warn("No artifact entities to save after mapping");
            return List.of();
        }
        List<Artifact> savedArtifacts = fileStoreJpaRepository.saveAll(artifactEntities);
        log.debug("Saved {} artifacts to database", savedArtifacts.size());
        List<String> fileStoreIds = savedArtifacts.stream()
                .map(Artifact::getFileStoreId)
                .toList();
        log.info("Successfully saved {} artifacts, generated {} fileStoreIds", artifacts.size(), fileStoreIds.size());
        return fileStoreIds;
    }

    public List<String> saveHLS(
            List<org.egov.filestore.domain.model.Artifact> artifacts, RequestInfo requestInfo) {
        log.trace("Entering saveHLS method with artifactCount: {}", artifacts.size());
        log.info("Saving {} HLS artifacts to cloud storage and database", artifacts.size());
        cloudFileManagerV2.saveFiles(artifacts);
        log.debug("HLS files saved to cloud storage successfully");
        List<Artifact> artifactEntities = new ArrayList<>();
        artifacts.forEach(artifact -> {
            if (artifact.isInsertable() && artifact.getFileLocation().getFileStoreId() != null) {
                artifact = artifact.withFileLocation(artifact.getFileLocation()
                        .withFileName(String.format("%s/%s", minioConfig.getBucketName(),
                                artifact.getFileLocation().getFileName())));
                artifactEntities.add(mapToEntity(artifact, requestInfo));
            }
        });
        log.debug("Mapped {} insertable HLS artifacts to entities", artifactEntities.size());

        if (artifactEntities.isEmpty()) {
            log.warn("No insertable HLS artifact entities to save after mapping");
            return List.of();
        }

        List<Artifact> savedArtifacts = fileStoreJpaRepository.saveAll(artifactEntities);
        log.debug("Saved {} HLS artifacts to database", savedArtifacts.size());
        List<String> fileStoreIds = savedArtifacts.stream()
                .map(Artifact::getFileStoreId)
                .toList();
        log.info("Successfully saved {} HLS artifacts, generated {} fileStoreIds", 
                artifacts.size(), fileStoreIds.size());
        return fileStoreIds;
    }

    /**
     * Converts POJO artifact to JPA Entity artifact
     *
     * @param artifact
     * @return
     */
    private Artifact mapToEntity(org.egov.filestore.domain.model.Artifact artifact, RequestInfo requestInfo) {

        FileLocation fileLocation = artifact.getFileLocation();
        Artifact entityArtifact = Artifact.builder().fileStoreId(fileLocation.getFileStoreId())
                .fileName(fileLocation.getFileName()).contentType(artifact.getMultipartFile().getContentType())
                .module(fileLocation.getModule()).tag(fileLocation.getTag()).tenantId(fileLocation.getTenantId())
                .fileSource(fileLocation.getFileSource())
                // .createdBy(requestInfo.getUserInfo().getUuid())
                // .lastModifiedBy(requestInfo.getUserInfo().getUuid())
                // .createdTime(System.currentTimeMillis())
                // .lastModifiedTime(System.currentTimeMillis())
                .build();
        if (isAzureStorageEnabled)
            entityArtifact.setFileSource(azureBlobSource);

        return entityArtifact;
    }

    /*
     * private List<Artifact>
     * mapArtifactsListToEntitiesList(List<org.egov.filestore.domain.model.
     * Artifact> artifacts) { return artifacts.stream() .map(this::mapToEntity)
     * .collect(Collectors.toList()); }
     *
     * private Artifact mapToEntity(org.egov.filestore.domain.model.Artifact
     * artifact) {
     *
     * FileLocation fileLocation = artifact.getFileLocation(); return
     * Artifact.builder().fileStoreId(fileLocation.getFileStoreId()).fileName(
     * fileLocation.getFileName())
     * .contentType(artifact.getMultipartFile().getContentType()).module(
     * fileLocation.getModule())
     * .tag(fileLocation.getTag()).tenantId(fileLocation.getTenantId()).build();
     * }
     */

    /**
     * @param fileStoreId
     * @param tenantId
     * @return
     * @throws IOException This api needs to be enhanced to pick right object .All
     *                     repositories should implement cloudmanager and it should
     *                     provide
     *                     simple get api too
     */
    public Resource find(String fileStoreId, String tenantId) throws IOException {
        log.trace("Entering find method with fileStoreId: {}, tenantId: {}", fileStoreId, tenantId);
        log.info("Finding artifact for fileStoreId: {}, tenantId: {}", fileStoreId, tenantId);
        Artifact artifact = fileStoreJpaRepository.findByFileStoreIdAndTenantId(fileStoreId, tenantId);
        if (artifact == null) {
            log.error("Artifact not found for fileStoreId: {}, tenantId: {}", fileStoreId, tenantId);
            throw new CustomException("NOT_FOUND", "Invalid filestoreid or tenantid");
        }
        log.debug("Artifact found, fileSource: {}, fileName: {}", 
                artifact.getFileLocation().getFileSource(), artifact.getFileName());

        org.springframework.core.io.Resource resource = null;

        if (artifact.getFileLocation().getFileSource().equals("minio")) {
            log.debug("Reading file from MinIO repository");
            // if only DiskFileStoreRepository use read else ignore
            MinioRepository repo = (MinioRepository) cloudFilesManager;
            resource = repo.read(artifact.getFileLocation());
        } else if (artifact.getFileLocation().getFileSource().equals("AzureBlobStorage")) {
            log.debug("Reading file from Azure Blob Storage repository");
            AzureBlobStorageImpl repo = (AzureBlobStorageImpl) cloudFilesManager;
            resource = repo.read(artifact.getFileLocation());
        } else {
            log.warn("Unknown fileSource: {} for fileStoreId: {}", 
                    artifact.getFileLocation().getFileSource(), fileStoreId);
        }

        if (null != resource) {
            long fileSize = resource.getFile().length();
            log.debug("File resource retrieved successfully, size: {} bytes", fileSize);
            return new Resource(artifact.getContentType(), artifact.getFileName(), resource, artifact.getTenantId(),
                    "" + fileSize + " bytes");
        } else {
            log.warn("Resource is null for fileStoreId: {}, tenantId: {}", fileStoreId, tenantId);
            return null;
        }
    }

    public List<FileInfo> findByTag(String tag, String tenantId) {
        log.trace("Entering findByTag method with tag: {}, tenantId: {}", tag, tenantId);
        log.info("Finding files by tag: {}, tenantId: {}", tag, tenantId);
        List<Artifact> artifacts = fileStoreJpaRepository.findByTagAndTenantId(tag, tenantId);
        log.debug("Found {} artifacts for tag: {}, tenantId: {}", artifacts.size(), tag, tenantId);
        List<FileInfo> fileInfoList = artifacts.stream().map(this::mapArtifactToFileInfo)
                .collect(Collectors.toList());
        log.info("Mapped {} artifacts to FileInfo objects for tag: {}", fileInfoList.size(), tag);
        return fileInfoList;
    }

    private FileInfo mapArtifactToFileInfo(Artifact artifact) {
        FileLocation fileLocation = new FileLocation(artifact.getFileStoreId(), artifact.getModule(), artifact.getTag(),
                artifact.getTenantId(), artifact.getFileName(), artifact.getFileSource());

        return new FileInfo(artifact.getContentType(), fileLocation, artifact.getTenantId());
    }

    public List<Artifact> getByTenantIdAndFileStoreIdList(String tenantId, List<String> fileStoreIds) {
        log.trace("Entering getByTenantIdAndFileStoreIdList method with tenantId: {}, fileStoreIds count: {}", 
                tenantId, fileStoreIds.size());
        log.info("Retrieving artifacts for tenantId: {}, fileStoreIds count: {}", tenantId, fileStoreIds.size());
        List<Artifact> artifacts = fileStoreJpaRepository.findByTenantIdAndFileStoreIdList(tenantId, fileStoreIds);
        log.debug("Retrieved {} artifacts from database", artifacts.size());
        return artifacts;
    }

    public Resource findByPath(FileLocation fileLocation) {
        log.trace("Entering findByPath method with fileName: {}, tenantId: {}", 
                fileLocation.getFileName(), fileLocation.getTenantId());
        log.info("Finding file by path: {}, tenantId: {}", fileLocation.getFileName(), fileLocation.getTenantId());
        MinioRepository repo = (MinioRepository) cloudFilesManager;
        org.springframework.core.io.Resource resource = repo.read(fileLocation);

        return Optional.ofNullable(resource)
                .map(res -> {
                    try {
                        Path filePath = res.getFile().toPath();
                        String contentType = Files.probeContentType(filePath);
                        long fileSize = res.getFile().length();
                        log.debug("File found by path, contentType: {}, fileSize: {} bytes", contentType, fileSize);

                        return new Resource(
                                contentType,
                                fileLocation.getFileName(),
                                res,
                                fileLocation.getTenantId(),
                                String.format("%d bytes", fileSize));
                    } catch (IOException e) {
                        log.error("Error fetching file from bucket for path: {}, tenantId: {}", 
                                fileLocation.getFileName(), fileLocation.getTenantId(), e);
                        throw new CustomException("Error fetching file from bucket", e.getMessage());
                    }
                })
                .orElse(null);
    }

    public String findS3SignedUrl(String fileStoreId, String tenantId) throws IOException {
        log.trace("Entering findS3SignedUrl method with fileStoreId: {}, tenantId: {}", fileStoreId, tenantId);
        log.info("Finding S3 signed URL for fileStoreId: {}, tenantId: {}", fileStoreId, tenantId);
        Artifact artifact = fileStoreJpaRepository.findByFileStoreIdAndTenantId(fileStoreId, tenantId);
        if (artifact == null) {
            log.error("Artifact not found for fileStoreId: {}, tenantId: {}", fileStoreId, tenantId);
            throw new CustomException("NOT_FOUND", "Invalid filestoreid or tenantid");
        }
        log.debug("Artifact found for fileStoreId: {}, fileName: {}", fileStoreId, artifact.getFileName());

        MinioRepository repo = (MinioRepository) cloudFilesManager;
        String fileLocation = artifact.getFileLocation().getFileName();
        String fileName = fileLocation.substring(fileLocation.indexOf('/') + 1, fileLocation.length());
        log.debug("Extracted fileName: {} from fileLocation: {}", fileName, fileLocation);
        String signedUrl = repo.getSignedUrl(fileName);
        log.debug("Generated signed URL successfully for fileStoreId: {}", fileStoreId);
        return signedUrl;
    }
}
