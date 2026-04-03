package digit.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.validation.annotation.Validated;

@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BoundaryRelationshipV2SearchRequest {

    @JsonProperty("RequestInfo")
    RequestInfo requestInfo;

    @NotNull
    @JsonProperty("BoundaryRelationship")
    BoundaryRelationshipSearchCriteria boundaryRelationshipSearchCriteria;

}
