package org.egov.filestore.domain.model;

import lombok.*;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Builder
public class FileLocation {
    private String fileStoreId;
    private String module;
    private String tag;
    private String tenantId;
    @With
    private String fileName;
    private String fileSource;
}
