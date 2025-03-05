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
			"Received upload request for  module: %s, tag: %s with file count: %s";

	private final ArtifactRepository artifactRepository;
	private final ArtifactMapper artifactMapper;

	public List<String> save(
			List<MultipartFile> filesToStore, String module, String tag, String tenantId, RequestInfo requestInfo) {
		log.info(UPLOAD_MESSAGE, module, tag, filesToStore.size());
		List<Artifact> artifacts = artifactMapper.mapFilesToArtifact(filesToStore, module, tag, tenantId);
		return this.artifactRepository.save(artifacts, requestInfo);
	}

	public List<FileInfo> retrieveByTag(String tag, String tenantId) {
		return artifactRepository.findByTag(tag, tenantId);
	}

	public Map<String, String> getUrls(String tenantId, List<String> fileStoreIds) {
		Map<String, String> urlMap = getUrlMap(
				artifactRepository.getByTenantIdAndFileStoreIdList(tenantId, fileStoreIds));
		return urlMap;
	}

	private Map<String, String> getUrlMap(List<org.egov.filestore.persistence.entity.Artifact> artifactList) {
		return cloudFilesManager.getFiles(artifactList);
	}

	public Resource retrieve(String fileStoreId, String tenantId) throws IOException {
		return artifactRepository.find(fileStoreId, tenantId);
	}

	public Resource retrieve(String fileStoreId, String quality, String fileName, String tenantId)  {
		// tenantId was intentionally added teice as a hack , as this wil be trimmed out down the line
		String fileSource = String.format("%s/%s/%s/hls/%s/%s",
				tenantId,
				tenantId,
				fileStoreId,
				quality,
				fileName);

		FileLocation fileLocation = FileLocation.builder()
				.fileStoreId(fileSource)
				.fileName(fileSource)
				.tenantId(tenantId)
				.build();

		return artifactRepository.findByPath(fileLocation);
	}

	
}
