package org.egov.im.web.models.storage;

import lombok.*;

@Builder(toBuilder = true)
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class File {
    private String fileStoreId;
    private String masterFileStoreId;
    private String tenantId;
}

