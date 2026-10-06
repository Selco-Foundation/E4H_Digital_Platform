package org.egov.field_planner.web.models;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ICCReportUploadRequest {

    @NotBlank
    private String systemType;

    @NotBlank
    private String totalSystemCapacity;
}
