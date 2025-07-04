package org.egov.project.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.egov.common.contract.workflow.ProcessInstance;
import org.egov.common.models.project.Project;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectStatusWrapper {

    private Project project;

    private String status;

    private List<Transaction> transactions;

    @JsonProperty("workflow")
    private ProcessInstance processInstance;
}
