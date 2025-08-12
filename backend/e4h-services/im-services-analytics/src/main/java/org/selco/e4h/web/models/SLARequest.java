package org.selco.e4h.web.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SLARequest {
    private RequestInfo requestInfo;
    private String tenantId;
}
