package org.egov.im.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IndexView {

    @JsonProperty("incidentType_localized")
    private String incidentTypeLocalized;

    @JsonProperty("incidentSubType_localized")
    private String incidentSubTypeLocalized;

    @JsonProperty("applicationStatus_localized")
    private String applicationStatusLocalized;

    @JsonProperty("phcSubType_localized")
    private String phcSubTypeLocalized;

    @JsonProperty("tenantId_localized")
    private String tenantIdLocalized;

    @JsonProperty("state")
    private String state;

    @JsonProperty("nin_hfr_id")
    private String ninHfrId;

    @JsonProperty("mappedVendor")
    private String mappedVendor;

    @JsonProperty("lastActionTakenBy")
    private String lastActionTakenBy;

    @JsonProperty("overallSla")
    private Long overallSla;
}
