package org.egov.asset.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * AssetCreate
 */
@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Document {

    @JsonProperty("id")
    private String id = null;
    @JsonProperty("documentType")
    private String documentType = null;
    @JsonProperty("fileStore")
    private String fileStore = null;
    @JsonProperty("documentUid")
    private String documentUid = null;
    @JsonProperty("additionalDetails")
    private Object additionalDetails = null;
    @JsonProperty("geoLocation")
    private GeoLocation geoLocation= null;
}
