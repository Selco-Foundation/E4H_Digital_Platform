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
public class ActivitySearchCriteria {

    @JsonProperty("ids")
    private @Valid List<String> ids = null;

    @JsonProperty("codes")
    private @Valid List<String> code = null;
}
