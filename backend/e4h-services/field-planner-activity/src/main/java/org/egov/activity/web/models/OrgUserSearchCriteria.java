package org.egov.activity.web.models;

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

}
