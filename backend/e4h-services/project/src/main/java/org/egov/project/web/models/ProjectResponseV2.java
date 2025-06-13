package org.egov.project.web.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.response.ResponseInfo;
import org.springframework.validation.annotation.Validated;

@Validated
@JsonIgnoreProperties(
        ignoreUnknown = true
)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ProjectResponseV2 {
    @JsonProperty("ResponseInfo")
    private @NotNull @Valid ResponseInfo responseInfo = null;
    @JsonProperty("Project")
    private @NotNull @Valid List<ProjectV2> project = new ArrayList();
    @JsonProperty("TotalCount")
    private Integer totalCount = 0;

}