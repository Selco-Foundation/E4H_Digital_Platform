package org.egov.rms.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IMServiceRequest {

    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

    @JsonProperty("incident")
    private Incident incident;

    @JsonProperty("workflow")
    private Workflow workflow;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Incident {
        @JsonProperty("incidentType")
        private String incidentType;

        @JsonProperty("incidentSubtype")
        private String incidentSubType;

        @JsonProperty("tenantId")
        private String tenantId;

        @JsonProperty("district")
        private String district;

        @JsonProperty("block")
        private String block;

        @JsonProperty("phcType")
        private String phcType;

        @JsonProperty("phcSubType")
        private String phcSubType;

        @JsonProperty("comments")
        private String comments;

        @JsonProperty("systemFunctional")
        private String systemFunctional;

        @JsonProperty("applicationStatus")
        private String applicationStatus;

        @JsonProperty("source")
        private String source;

        @JsonProperty("reporterType")
        private String reporterType;

        @JsonProperty("boundaryCode")
        private String boundaryCode;

        @JsonProperty("additionalDetail")
        private Map<String, Object> additionalDetail;

        @JsonProperty("reporter")
        private User reporter;

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Workflow {
        @JsonProperty("action")
        private String action;

        @JsonProperty("comments")
        private String comments;

        @JsonProperty("assignes")
        private List<String> assignes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class User {
        @JsonProperty("uuid")
        private String uuid;

        @JsonProperty("tenantId")
        private String tenantId;
    }
}

