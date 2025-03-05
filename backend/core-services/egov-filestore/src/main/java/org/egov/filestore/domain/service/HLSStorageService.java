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

        log.info(UPLOAD_MESSAGE, module, tag, filesToStore.size());

        List<Artifact> artifacts =
                artifactMapper.mapHLSArtifact(filesToStore, module, tag, tenantId);

        return this.artifactRepository.save(artifacts, requestInfo);
    }
}
