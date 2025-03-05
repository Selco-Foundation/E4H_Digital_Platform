package org.egov.filestore.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class FileInfo {
    private String contentType;
    private FileLocation fileLocation;
    private String tenantId;
}
