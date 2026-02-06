package org.egov.filestore.domain.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.filestore.domain.model.Artifact;
import org.egov.filestore.persistence.repository.ArtifactRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class HLSStorageService {

    private final ArtifactRepository artifactRepository;
    private final ArtifactMapper artifactMapper;

    private static final String UPLOAD_MESSAGE = "Received upload request for module: %s, tag: %s with file count: %s";


    public List<String> save(
            List<MultipartFile> filesToStore, String module, String tag,
            String tenantId, RequestInfo requestInfo) {

        log.trace("Entering save method for HLS with module: {}, tag: {}, tenantId: {}, fileCount: {}", 
                module, tag, tenantId, filesToStore.size());
        log.info(UPLOAD_MESSAGE, module, tag, filesToStore.size());

        log.debug("Mapping {} HLS files to artifacts", filesToStore.size());
        List<Artifact> artifacts =
                artifactMapper.mapHLSArtifact(filesToStore, module, tag, tenantId);
        log.debug("Mapped {} files to {} HLS artifacts", filesToStore.size(), artifacts.size());

        log.info("Saving HLS artifacts to repository for module: {}, tag: {}", module, tag);
        List<String> fileStoreIds = this.artifactRepository.saveHLS(artifacts, requestInfo);
        log.debug("Saved HLS artifacts, generated {} fileStoreIds", fileStoreIds.size());
        log.info("HLS file storage completed for module: {}, tag: {}, fileStoreIds count: {}", 
                module, tag, fileStoreIds.size());
        return fileStoreIds;
    }
}
