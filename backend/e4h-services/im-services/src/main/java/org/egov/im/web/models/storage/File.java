package org.egov.im.web.models.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder(toBuilder = true)
@Getter
@AllArgsConstructor
@Setter
public class File {
    private String fileStoreId;
    private String masterFileStoreId;
    private String tenantId;
}

