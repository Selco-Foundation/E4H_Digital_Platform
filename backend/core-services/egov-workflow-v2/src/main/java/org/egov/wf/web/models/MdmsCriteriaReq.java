package org.egov.wf.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import org.egov.common.contract.request.RequestInfo;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.beans.ConstructorProperties;

@Builder
@Data
public class MdmsCriteriaReq {
    @JsonProperty("RequestInfo")
    @Valid
    @NotNull
    private RequestInfo requestInfo;
    @JsonProperty("MdmsCriteria")
    @Valid
    @NotNull
    private MdmsCriteria mdmsCriteria;

}
