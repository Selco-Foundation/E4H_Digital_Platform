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
 * AssetUpdate
 */
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2025-04-11T15:29:49.453244911+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssetUpdate   {
        @JsonProperty("tenant_id")

                private Object tenantId = null;

        @JsonProperty("assetID")

                private Object assetID = null;

        @JsonProperty("warrantyStartDate")

                private Object warrantyStartDate = null;

        @JsonProperty("warrantyDuration")

                private Object warrantyDuration = null;

        @JsonProperty("warrantyEndDate")

                private Object warrantyEndDate = null;


}
