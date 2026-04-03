package org.egov.activity.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.models.project.Project;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FacilityStatusWrapper {

    private ActivityFacility activityFacility;

    private String status;

    private List<Transaction> transactions;

    @JsonProperty("workflow")
    private List<ProcessInstance> processInstances;
}
