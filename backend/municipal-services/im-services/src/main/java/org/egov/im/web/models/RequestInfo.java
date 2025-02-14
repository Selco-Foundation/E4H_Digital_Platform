package org.egov.im.web.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.PlainAccessRequest;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class RequestInfo {
    private String apiId;
    private String ver;
    private Long ts;
    private String action;
    private String did;
    private String key;
    private String msgId;
    private String authToken;
    private String correlationId;
    private PlainAccessRequest plainAccessRequest;
    private User userInfo;
}