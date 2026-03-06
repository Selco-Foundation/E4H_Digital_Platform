package org.egov.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrgUserDeleteErrorResponse {

    @JsonProperty("message")
    private String message;

    @JsonProperty("blockingAssignments")
    private List<ActivityAssignment> blockingAssignments;

}