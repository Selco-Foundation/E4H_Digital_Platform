package org.egov.project.service.enrichment;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.models.project.ProjectStaff;
import org.egov.common.models.project.ProjectStaffBulkRequest;
import org.egov.common.service.IdGenService;
import org.egov.project.config.ProjectConfiguration;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static org.egov.common.utils.CommonUtils.*;

@Service
@Slf4j
public class ProjectStaffEnrichmentService {

    public static final String ENRICHMENT_DONE = "enrichment done";
    private final IdGenService idGenService;

    private final ProjectConfiguration projectConfiguration;

    public ProjectStaffEnrichmentService(IdGenService idGenService, ProjectConfiguration projectConfiguration) {
        this.idGenService = idGenService;
        this.projectConfiguration = projectConfiguration;
    }

    public void create(List<ProjectStaff> entities, ProjectStaffBulkRequest request) throws Exception {
        log.trace("Entering create (ProjectStaffEnrichmentService)");
        log.info("Starting enrichment for create project staff");
        log.debug("Enriching {} staff", entities != null ? entities.size() : 0);

        log.debug("Generating IDs using IdGenService");
        List<String> idList = idGenService.getIdList(request.getRequestInfo(),
                getTenantId(entities),
                projectConfiguration.getProjectStaffIdFormat(), "", entities.size());
        log.debug("Generated {} IDs", idList != null ? idList.size() : 0);

        enrichForCreate(entities, idList, request.getRequestInfo());
        log.info("Successfully completed enrichment for create project staff");
        log.trace("Exiting create (ProjectStaffEnrichmentService)");
    }

    public void update(List<ProjectStaff> entities, ProjectStaffBulkRequest request) {
        log.trace("Entering update (ProjectStaffEnrichmentService)");
        log.info("Starting enrichment for update project staff");
        log.debug("Enriching {} staff", entities != null ? entities.size() : 0);
        Map<String, ProjectStaff> projectStaffMap = getIdToObjMap(entities);
        log.debug("Created staff map with {} entries", projectStaffMap.size());
        enrichForUpdate(projectStaffMap, entities, request);
        log.info("Successfully completed enrichment for update project staff");
        log.trace("Exiting update (ProjectStaffEnrichmentService)");
    }

    public void delete(List<ProjectStaff> entities, ProjectStaffBulkRequest request) {
        log.trace("Entering delete (ProjectStaffEnrichmentService)");
        log.info("Starting enrichment for delete project staff");
        log.debug("Enriching {} staff for delete", entities != null ? entities.size() : 0);
        enrichForDelete(entities, request.getRequestInfo(), true);
        log.info("Successfully completed enrichment for delete project staff");
        log.trace("Exiting delete (ProjectStaffEnrichmentService)");
    }
}
