package org.egov.im.web.models.vendor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrgUserSearchResponse {

    @JsonProperty("OrgUsers")
    private List<OrgUser> orgUsers = null;

    @JsonProperty("TotalCount")
    private Integer totalCount = 0;
}
