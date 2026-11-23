package org.egov.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.util.List;


/**
 * Encapsulates all parameters for building a project search query.
 */
@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrgUserSearchCriteria {

    @JsonProperty("ids")
    private @Valid List<String> id = null;

    @JsonProperty("userIds")
    private @Valid List<String> userId = null;

    @JsonProperty("organizationIds")
    private @Valid List<String> organizationId = null;

    @JsonProperty("tenantId")
    private String tenantId;

    /**
     * Partial, case-insensitive match on the user's name. The name lives on the HRMS user, not in
     * eg_org_user, so it is resolved against HRMS before paging - see OrganisationUserService.
     */
    @JsonProperty("name")
    private String name;

    /** Role codes; a user matches when they hold any of them. Resolved against HRMS before paging. */
    @JsonProperty("roles")
    private List<String> roles;

}
