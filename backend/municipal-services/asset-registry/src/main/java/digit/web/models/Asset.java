package digit.web.models;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import digit.web.models.HttpsrawGithubusercontentComegovernmentsDIGITSpecscommonContractUpdateCommon20ServicescommonContractYamlcomponentsschemasAuditDetails;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;

/**
 * Asset
 */
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2025-04-11T15:29:49.453244911+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Asset   {
        @JsonProperty("tenant_id")

                private Object tenantId = null;

        @JsonProperty("assetID")

                private Object assetID = null;

        @JsonProperty("facilityID")
          @NotNull

                private Object facilityID = null;

        @JsonProperty("assetTypeID")
          @NotNull

                private Object assetTypeID = null;

        @JsonProperty("serialNumber")
          @NotNull

                private Object serialNumber = null;

        @JsonProperty("modelNumber")
          @NotNull

                private Object modelNumber = null;

        @JsonProperty("brandID")
          @NotNull

                private Object brandID = null;

        @JsonProperty("assetDetails")

                private Object assetDetails = null;

        @JsonProperty("warrantyStartDate")
          @NotNull

                private Object warrantyStartDate = null;

        @JsonProperty("warrantyDuration")
          @NotNull

                private Object warrantyDuration = null;

        @JsonProperty("warrantyEndDate")
          @NotNull

                private Object warrantyEndDate = null;

        @JsonProperty("wfStatus")

                private Object wfStatus = null;

        @JsonProperty("isActive")

                private Object isActive = null;

        @JsonProperty("documents")

                private Object documents = null;

        @JsonProperty("auditDetails")

          @Valid
                private HttpsrawGithubusercontentComegovernmentsDIGITSpecscommonContractUpdateCommon20ServicescommonContractYamlcomponentsschemasAuditDetails auditDetails = null;

        @JsonProperty("additionalDetails")

                private Object additionalDetails = null;


}
