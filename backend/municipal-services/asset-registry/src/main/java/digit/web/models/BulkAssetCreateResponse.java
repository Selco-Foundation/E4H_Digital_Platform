package digit.web.models;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import digit.web.models.HttpsrawGithubusercontentComegovernmentsDIGITSpecscommonContractUpdateCommon20ServicescommonContractYamlcomponentsschemasResponseInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;

/**
 * BulkAssetCreateResponse
 */
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2025-04-11T15:29:49.453244911+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BulkAssetCreateResponse   {
        @JsonProperty("ResponseInfo")
          @NotNull

          @Valid
                private HttpsrawGithubusercontentComegovernmentsDIGITSpecscommonContractUpdateCommon20ServicescommonContractYamlcomponentsschemasResponseInfo responseInfo = null;

        @JsonProperty("assets")
          @NotNull

                private Object assets = null;


}
