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
 * AssetAMC
 */
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2025-04-11T15:29:49.453244911+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssetAMC   {
        @JsonProperty("tenant_id")

                private Object tenantId = null;

        @JsonProperty("amcID")

                private Object amcID = null;

        @JsonProperty("assetID")

                private Object assetID = null;

        @JsonProperty("contractNumber")

                private Object contractNumber = null;

        @JsonProperty("vendorCode")

                private Object vendorCode = null;

        @JsonProperty("contractStartDate")

                private Object contractStartDate = null;

        @JsonProperty("contractEndDate")

                private Object contractEndDate = null;

        @JsonProperty("visitSchedule")

                private Object visitSchedule = null;

        @JsonProperty("visits")

                private Object visits = null;

        @JsonProperty("documents")

                private Object documents = null;

        @JsonProperty("auditDetails")

          @Valid
                private HttpsrawGithubusercontentComegovernmentsDIGITSpecscommonContractUpdateCommon20ServicescommonContractYamlcomponentsschemasAuditDetails auditDetails = null;

        @JsonProperty("additionalDetails")

                private Object additionalDetails = null;


}
