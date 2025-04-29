package facility.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.Arrays;
import java.util.Objects;

/**
 * FacilityCreateRequest
 */

@Setter
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-04-24T16:00:58.522282987+05:30[Asia/Kolkata]", comments = "Generator version: 7.4.0")
public class FacilityCreateRequest {

    private FacilityAssessmentCreateRequestRequestInfo requestInfo;

    private Facility facility;

    public FacilityCreateRequest() {
        super();
    }

    /**
     * Constructor with only required parameters
     */
    public FacilityCreateRequest(FacilityAssessmentCreateRequestRequestInfo requestInfo) {
        this.requestInfo = requestInfo;
    }

    private static <T> boolean equalsNullable(JsonNullable<T> a, JsonNullable<T> b) {
        return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
    }

    private static <T> int hashCodeNullable(JsonNullable<T> a) {
        if (a == null) {
            return 1;
        }
        return a.isPresent() ? Arrays.deepHashCode(new Object[]{a.get()}) : 31;
    }

    public FacilityCreateRequest requestInfo(FacilityAssessmentCreateRequestRequestInfo requestInfo) {
        this.requestInfo = requestInfo;
        return this;
    }

    /**
     * Get requestInfo
     *
     * @return requestInfo
     */
    @NotNull
    @Valid
    @Schema(name = "RequestInfo", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("RequestInfo")
    public FacilityAssessmentCreateRequestRequestInfo getRequestInfo() {
        return requestInfo;
    }

    /**
     * Get facilities
     *
     * @return facilities
     */

    @Schema(name = "facilities", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("facility")
    public Facility getFacility() {
        return facility;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class FacilityCreateRequest {\n");
        sb.append("    requestInfo: ").append(toIndentedString(requestInfo)).append("\n");
        sb.append("    facilities: ").append(toIndentedString(facility)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

