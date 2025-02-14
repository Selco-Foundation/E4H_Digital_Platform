package org.egov.im.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.egov.mdms.model.MdmsCriteria;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class MdmsCriteriaReq {
    @JsonProperty("RequestInfo")
    private @Valid
    @NotNull RequestInfo requestInfo;
    @JsonProperty("MdmsCriteria")
    private @Valid
    @NotNull MdmsCriteria mdmsCriteria;
}