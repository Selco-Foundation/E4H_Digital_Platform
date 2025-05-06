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
 * Description of an AMC visit
 */
@Schema(description = "Description of an AMC visit")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2025-04-11T15:29:49.453244911+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssetAMCVisit   {
        @JsonProperty("tenant_id")

                private Object tenantId = null;

        @JsonProperty("visitId")

                private Object visitId = null;

        @JsonProperty("assetId")

                private Object assetId = null;

        @JsonProperty("facilityId")

                private Object facilityId = null;

        @JsonProperty("scheduledDate")

                private Object scheduledDate = null;

        @JsonProperty("visitDate")

                private Object visitDate = null;

        @JsonProperty("engineerName")

                private Object engineerName = null;

        @JsonProperty("observations")

                private Object observations = null;

        @JsonProperty("nextDueDate")

                private Object nextDueDate = null;

        @JsonProperty("visitStatus")

                private Object visitStatus = null;

        @JsonProperty("additionalDetails")

                private Object additionalDetails = null;


}
