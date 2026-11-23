package org.egov.amc.web.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.response.ResponseInfo;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReindexResponse {
    private ResponseInfo responseInfo;
    private Integer totalVisitsIndexed;
}
