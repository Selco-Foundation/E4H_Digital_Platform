package facility.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;

/**
 * Body of {@code _approve}, {@code _reject} and {@code _cancel}. The proposed number is not part
 * of it: nobody may edit a request, only decide on it.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MobileChangeRequestActionRequest {

    @JsonProperty("RequestInfo")
    @NotNull
    @Valid
    private RequestInfo requestInfo;

    @JsonProperty("id")
    @NotBlank
    private String id;

    @JsonProperty("comment")
    @Size(max = 1024)
    private String comment;
}
