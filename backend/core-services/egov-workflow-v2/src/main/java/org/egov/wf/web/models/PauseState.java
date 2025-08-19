package org.egov.wf.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(of = {"businessId", "businessService"})
@ToString
public class PauseState {

    @JsonProperty("id")
    private Long id = null;

    @NotNull
    @Size(max = 128)
    @JsonProperty("businessId")
    private String businessId = null;

    @NotNull
    @Size(max = 128)
    @JsonProperty("businessService")
    private String businessService = null;

    @JsonProperty("isPaused")
    private Boolean isPaused = false;

    @JsonProperty("comments")
    private Map<String, Object> comments = null;

    @JsonProperty("auditDetails")
    private AuditDetails auditDetails = null;
}
