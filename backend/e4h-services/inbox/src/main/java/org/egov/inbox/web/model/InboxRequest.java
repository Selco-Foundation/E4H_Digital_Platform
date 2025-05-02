package org.egov.inbox.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.*;
import org.egov.common.contract.request.RequestInfo;

;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class InboxRequest   {
  @JsonProperty("RequestInfo")
  private RequestInfo RequestInfo;

  @Valid
  @JsonProperty("inbox")
  private InboxSearchCriteria inbox ;



}