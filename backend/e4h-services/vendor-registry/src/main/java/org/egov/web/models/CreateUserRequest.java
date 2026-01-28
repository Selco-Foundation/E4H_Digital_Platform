package org.egov.web.models;

import lombok.*;
import org.egov.common.contract.request.RequestInfo;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class CreateUserRequest {
    private RequestInfo requestInfo;

    @NotNull
    @Valid
    private UserRequest user;

}


