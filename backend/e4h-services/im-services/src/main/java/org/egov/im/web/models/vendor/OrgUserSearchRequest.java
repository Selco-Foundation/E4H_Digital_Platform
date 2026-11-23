package org.egov.im.web.models.vendor;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrgUserSearchRequest {

    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo = null;

    @JsonProperty("OrgUser")
    private OrgUserSearchCriteria criteria = null;
}
