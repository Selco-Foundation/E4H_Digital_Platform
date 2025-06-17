package org.egov.project.web.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.egov.common.models.project.Project;

@Getter
@AllArgsConstructor
public class ProjectStatusWrapper {
    private Project project;
    private String state;
}