package org.egov.im.web.models.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class File {
    private String fileStoreId;
    private String tenantId;
}

