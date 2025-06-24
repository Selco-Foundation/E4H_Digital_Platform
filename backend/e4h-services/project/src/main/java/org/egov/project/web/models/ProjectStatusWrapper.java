package org.egov.project.web.models;

import lombok.*;
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
}
