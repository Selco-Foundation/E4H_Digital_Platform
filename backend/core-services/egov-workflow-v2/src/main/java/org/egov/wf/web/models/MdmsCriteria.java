package org.egov.wf.web.models;

import lombok.Builder;
import lombok.Data;

import java.beans.ConstructorProperties;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Builder
@Data
public class MdmsCriteria {
    @NotNull
    private String tenantId;
    @NotNull
    private String schemaCode;
    private int limit;
}