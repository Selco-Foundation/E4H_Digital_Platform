package org.egov.hrms.web.contract;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * Request object for boundary-relationships search API
 * Matches the boundary-service BoundaryRelationshipSearchCriteria
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class BoundarySearchRequest {

    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("hierarchyType")
    private String hierarchyType;

    @JsonProperty("boundaryType")
    private String boundaryType;

    @JsonProperty("codes")
    private java.util.List<String> codes;

    @JsonProperty("parent")
    private String parent;

    @JsonProperty("includeChildren")
    @Builder.Default
    private Boolean includeChildren = Boolean.FALSE;

    @JsonProperty("includeParents")
    @Builder.Default
    private Boolean includeParents = Boolean.FALSE;
}

