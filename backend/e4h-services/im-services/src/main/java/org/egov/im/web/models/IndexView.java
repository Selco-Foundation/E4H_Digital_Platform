package org.egov.im.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

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

    @JsonProperty("definedTotalSla")
    private Long definedTotalSla;

    @JsonProperty("startingStatus")
    private String startingStatus;

    @JsonProperty("endingStatus")
    private String endingStatus;

    @JsonProperty("startingStatus_localized")
    private String startingStatusLocalized;

    @JsonProperty("endingStatus_localized")
    private String endingStatusLocalized;

    @JsonProperty("uuid")
    private String uuid;

    @JsonProperty("documentUrls")
    private List<String> documentUrls ;
}
