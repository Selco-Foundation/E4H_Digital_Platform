package org.egov.amc.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.*;
import org.egov.common.contract.models.AuditDetails;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Size;

/**
 * A Object holds the basic data for a Trade License
 */
@ApiModel(description = "A Object holds the basic data for a Trade License")
@Validated
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(of = {"id"})
public class Document {

        @Size(max=64)
        @JsonProperty("id")
        private String id = null;

        @Size(max=64)
        @JsonProperty("tenantId")
        private String tenantId = null;

        @Size(max=64)
        @JsonProperty("bomId")
        private String bomId = null;

        @Size(max=64)
        @JsonProperty("documentType")
        private String documentType = null;

        @Size(max=64)
        @JsonProperty("fileStoreId")
        private String fileStoreId = null;

        @Size(max=64)
        @JsonProperty("documentUid")
        private String documentUid = null;

        @JsonProperty("additionalDetails")
        private Object additionalDetails = null;

        @Size(max=64)
        @JsonProperty("status")
        private String status = null;

        @JsonProperty("auditDetails")
        private AuditDetails auditDetails = null;


}

