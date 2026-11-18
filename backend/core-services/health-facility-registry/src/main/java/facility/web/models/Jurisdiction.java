package facility.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

//@ApiModel(description = "Organisation Jurisdiction")
@Validated

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Jurisdiction {

    @JsonProperty("id")
    private String id = null;

    @JsonProperty("orgId")
    private String orgId = null;

    @JsonProperty("code")
    private String code = null;

    @JsonProperty("additionalDetails")
    private Object additionalDetails = null;

    @Size(min=2, max=100)
    private String hierarchy;

    @Size(min=2, max=100)
    private String boundary;

    @Size(max=256)
    private String boundaryType;

    private String tenantId;

    private AuditDetails auditDetails;

    private Boolean isActive;

    /**
     * Roles this jurisdiction applies to. Empty or absent means every role the user holds,
     * which is what every jurisdiction created before role scoping looks like.
     *
     * This field exists so the HRMS round trip preserves it. egov-hrms deletes and re-inserts
     * the whole employee on update, so a jurisdiction sent back without its roles would have
     * them dropped. The server-side merge in EmployeeService treats an absent list as
     * "unchanged" and restores the stored roles, but only an explicit empty list clears them -
     * so dropping the field here would still have been safe, while silently making this
     * service unable to change scoping at all.
     */
    private List<Role> roles;
}
