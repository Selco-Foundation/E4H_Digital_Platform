package org.egov.filestore.repository;

import org.egov.filestore.domain.model.Artifact;

import java.util.List;

public interface CloudFileManagerV2 {

    void saveFiles(List<Artifact> artifacts);
}
