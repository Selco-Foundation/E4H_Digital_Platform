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
public class BomSearchCriteria {

    @JsonProperty("ids")
    private @Valid List<String> ids = null;

    @JsonProperty("facilityIds")
    private @Valid List<String> facilityId = null;

    @JsonProperty("statuses")
    private @Valid List<String> name = null;

    @JsonProperty("assignUser")
    private String assignUser = null;

    private String tenantId;

    private boolean isActive;

    private boolean isCountQuery;
}
