package org.egov.rms.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IMServiceResponse {

    @JsonProperty("responseInfo")
    private ResponseInfo responseInfo;

    @JsonProperty("IncidentWrappers")
    private List<IncidentWrapper> incidentWrappers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseInfo {
        @JsonProperty("apiId")
        private String apiId;

        @JsonProperty("ver")
        private String ver;

        @JsonProperty("ts")
        private Long ts;

        @JsonProperty("resMsgId")
        private String resMsgId;

        @JsonProperty("msgId")
        private String msgId;

        @JsonProperty("status")
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IncidentWrapper {
        @JsonProperty("incident")
        private Incident incident;

        @JsonProperty("workflow")
        private Object workflow;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Incident {
        @JsonProperty("incidentId")
        private String incidentId;

        @JsonProperty("id")
        private String id;

        @JsonProperty("tenantId")
        private String tenantId;
    }
}

