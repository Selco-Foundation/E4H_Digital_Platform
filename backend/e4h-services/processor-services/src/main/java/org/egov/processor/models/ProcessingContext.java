package org.egov.processor.models;

import lombok.Builder;
import lombok.Data;
import lombok.With;

@With
@Builder
@Data
public class ProcessingContext {
    private String videoId;
    private String module;
    private String tag;
    private String tenantId;
    private String requestInfo;
}
