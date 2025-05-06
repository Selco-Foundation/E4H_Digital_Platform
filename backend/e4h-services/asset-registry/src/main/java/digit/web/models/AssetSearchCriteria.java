package digit.web.models;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;

/**
 * Criteria to search for assets. tenant_id is mandatory. Send one of the rest
 */
@Schema(description = "Criteria to search for assets. tenant_id is mandatory. Send one of the rest")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2025-04-11T15:29:49.453244911+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssetSearchCriteria   {
        @JsonProperty("tenant_id")
          @NotNull

                private Object tenantId = null;

        @JsonProperty("assetID")

                private Object assetID = null;

        @JsonProperty("facilityID")

                private Object facilityID = null;

        @JsonProperty("serialNumber")

                private Object serialNumber = null;

        @JsonProperty("modelNumber")

                private Object modelNumber = null;

        @JsonProperty("brandID")

                private Object brandID = null;

        @JsonProperty("wfStatus")

                private Object wfStatus = null;


}
