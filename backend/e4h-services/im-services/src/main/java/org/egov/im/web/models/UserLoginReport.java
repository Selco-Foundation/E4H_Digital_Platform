package org.egov.im.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.validation.annotation.Validated;

@Validated
@AllArgsConstructor
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Setter
@ToString
@Builder
public class UserLoginReport {

    @JsonProperty("id")
    private String id;

    @JsonProperty("userName")
    private String userName;

    @JsonProperty("userRole")
    private String userRole;

    @JsonProperty("currentOwnerName")
    private String currentOwnerName;

    @JsonProperty("lastLoginDateTime")
    private String lastLoginDateTime;

    @JsonProperty("healthFacilityName")
    private String healthFacilityName;

    @JsonProperty("block")
    private String block;

    @JsonProperty("district")
    private String district;

    @JsonProperty("state")
    private String state;
}
