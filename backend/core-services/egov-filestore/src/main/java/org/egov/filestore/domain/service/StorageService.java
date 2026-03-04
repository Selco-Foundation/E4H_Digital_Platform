package org.egov.filestore.domain.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.egov.common.contract.request.RequestInfo;
import org.egov.filestore.domain.model.Artifact;
import org.egov.filestore.domain.model.FileInfo;
import org.egov.filestore.domain.model.FileLocation;
import org.egov.filestore.domain.model.Resource;
import org.egov.filestore.persistence.repository.ArtifactRepository;
import org.egov.filestore.repository.CloudFilesManager;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class StorageService {


	private final CloudFilesManager cloudFilesManager;

	private static final String UPLOAD_MESSAGE =
			"Received upload request for  module: {}, tag: {} with file count: {}";

	private final ArtifactRepository artifactRepository;
	private final ArtifactMapper artifactMapper;

	public List<String> save(
			List<MultipartFile> filesToStore, String module, String tag, String tenantId, RequestInfo requestInfo) {
		log.trace("Entering save method with module: {}, tag: {}, tenantId: {}, fileCount: {}", 
				module, tag, tenantId, filesToStore.size());
		log.info(UPLOAD_MESSAGE, module, tag, filesToStore.size());
		log.debug("Mapping {} files to artifacts", filesToStore.size());
		List<Artifact> artifacts = artifactMapper.mapFilesToArtifact(filesToStore, module, tag, tenantId);
		log.debug("Mapped {} files to {} artifacts", filesToStore.size(), artifacts.size());
		log.info("Saving artifacts to repository for module: {}, tag: {}", module, tag);
		List<String> fileStoreIds = this.artifactRepository.save(artifacts, requestInfo);
		log.debug("Saved {} artifacts, generated {} fileStoreIds", artifacts.size(), fileStoreIds.size());
		log.info("File storage completed for module: {}, tag: {}, fileStoreIds count: {}", 
				module, tag, fileStoreIds.size());
		return fileStoreIds;
	}

	public List<FileInfo> retrieveByTag(String tag, String tenantId) {
		log.trace("Entering retrieveByTag method with tag: {}, tenantId: {}", tag, tenantId);
		log.info("Retrieving files by tag: {}, tenantId: {}", tag, tenantId);
		List<FileInfo> fileInfoList = artifactRepository.findByTag(tag, tenantId);
		log.debug("Retrieved {} files for tag: {}, tenantId: {}", fileInfoList.size(), tag, tenantId);
		return fileInfoList;
	}

	public Map<String, String> getUrls(String tenantId, List<String> fileStoreIds) {
		log.trace("Entering getUrls method with tenantId: {}, fileStoreIds count: {}", tenantId, fileStoreIds.size());
		log.info("Retrieving URLs for {} fileStoreIds, tenantId: {}", fileStoreIds.size(), tenantId);
		List<org.egov.filestore.persistence.entity.Artifact> artifacts = 
				artifactRepository.getByTenantIdAndFileStoreIdList(tenantId, fileStoreIds);
		log.debug("Retrieved {} artifacts from repository", artifacts.size());
		Map<String, String> urlMap = getUrlMap(artifacts);
		log.debug("Generated {} URLs for tenantId: {}", urlMap.size(), tenantId);
		return urlMap;
	}

	private Map<String, String> getUrlMap(List<org.egov.filestore.persistence.entity.Artifact> artifactList) {
		log.trace("Entering getUrlMap method with artifactList size: {}", artifactList.size());
		Map<String, String> urlMap = cloudFilesManager.getFiles(artifactList);
		log.debug("Generated URL map with {} entries", urlMap.size());
		return urlMap;
	}

	public Resource retrieve(String fileStoreId, String tenantId) throws IOException {
		log.trace("Entering retrieve method with fileStoreId: {}, tenantId: {}", fileStoreId, tenantId);
		log.info("Retrieving file for fileStoreId: {}, tenantId: {}", fileStoreId, tenantId);
		Resource resource = artifactRepository.find(fileStoreId, tenantId);
		if (resource != null) {
			log.debug("File retrieved successfully, fileName: {}, contentType: {}", 
					resource.getFileName(), resource.getContentType());
		} else {
			log.warn("File not found for fileStoreId: {}, tenantId: {}", fileStoreId, tenantId);
		}
		return resource;
	}

	public String retrieveSignedUrl(String fileStoreId, String tenantId) throws IOException {
		log.trace("Entering retrieveSignedUrl method with fileStoreId: {}, tenantId: {}", fileStoreId, tenantId);
		log.info("Retrieving signed URL for fileStoreId: {}, tenantId: {}", fileStoreId, tenantId);
		String signedUrl = artifactRepository.findS3SignedUrl(fileStoreId, tenantId);
		log.debug("Signed URL generated successfully for fileStoreId: {}", fileStoreId);
		return signedUrl;
	}

	public Resource retrieve(String fileStoreId, String quality, String fileName, String tenantId)  {
		log.trace("Entering retrieve method for HLS chunk with fileStoreId: {}, quality: {}, fileName: {}, tenantId: {}", 
				fileStoreId, quality, fileName, tenantId);
		log.info("Retrieving HLS chunk for fileStoreId: {}, quality: {}, fileName: {}, tenantId: {}", 
				fileStoreId, quality, fileName, tenantId);
		// tenantId was intentionally added twice as a hack, as this will be trimmed out down the line
		String fileSource = String.format("%s/%s/%s/hls/%s/%s",
				tenantId,
				tenantId,
				fileStoreId,
				quality,
				fileName);
		log.debug("Constructed fileSource path: {}", fileSource);

		FileLocation fileLocation = FileLocation.builder()
				.fileStoreId(fileSource)
				.fileName(fileSource)
				.tenantId(tenantId)
				.build();

		Resource resource = artifactRepository.findByPath(fileLocation);
		if (resource != null) {
			log.debug("HLS chunk retrieved successfully, fileName: {}", fileName);
		} else {
			log.warn("HLS chunk not found for fileStoreId: {}, quality: {}, fileName: {}", 
					fileStoreId, quality, fileName);
		}
		return resource;
	}

	
}
