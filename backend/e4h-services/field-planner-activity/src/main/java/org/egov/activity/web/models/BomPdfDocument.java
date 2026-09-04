package org.egov.activity.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BomPdfDocument {

    @JsonProperty("documentType")
    private String documentType;

    @JsonProperty("documentName")
    private String documentName;

    @JsonProperty("fileStoreIds")
    private List<String> fileStoreIds;
}