package org.egov.project.web.models;

import lombok.Getter;
import lombok.Setter;
import org.egov.common.models.project.ProjectSearch;

import java.util.List;

public class ExtendedProjectSearch extends ProjectSearch {
    @Getter
    @Setter
    private List<String> workflowStatus;

}
