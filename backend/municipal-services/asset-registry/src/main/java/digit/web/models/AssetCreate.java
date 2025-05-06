package digit.web.models;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import digit.web.models.Asset;
import digit.web.models.HttpsrawGithubusercontentComegovernmentsDIGITSpecscommonContractUpdateCommon20ServicescommonContractYamlcomponentsschemasWorkflow;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;

/**
 * AssetCreate
 */
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2025-04-11T15:29:49.453244911+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssetCreate   {
        @JsonProperty("Asset")
          @NotNull

          @Valid
                private Asset asset = null;

        @JsonProperty("workflow")
          @NotNull

          @Valid
                private HttpsrawGithubusercontentComegovernmentsDIGITSpecscommonContractUpdateCommon20ServicescommonContractYamlcomponentsschemasWorkflow workflow = null;


}
