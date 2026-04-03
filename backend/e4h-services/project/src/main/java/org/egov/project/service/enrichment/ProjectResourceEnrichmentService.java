package org.egov.project.service.enrichment;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.models.project.ProjectResource;
import org.egov.common.models.project.ProjectResourceBulkRequest;
import org.egov.common.service.IdGenService;
import org.egov.project.config.ProjectConfiguration;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static org.egov.common.utils.CommonUtils.*;

@Component
@Slf4j
public class ProjectResourceEnrichmentService {

    public static final String ENRICHMENT_DONE = "enrichment done";
    private final IdGenService idGenService;

    private final ProjectConfiguration projectConfiguration;

    public ProjectResourceEnrichmentService(IdGenService idGenService, ProjectConfiguration projectConfiguration) {
        this.idGenService = idGenService;
        this.projectConfiguration = projectConfiguration;
    }

    public void create(List<ProjectResource> entities, ProjectResourceBulkRequest request) throws Exception {
        log.trace("Entering create (ProjectResourceEnrichmentService)");
        log.info("Starting enrichment for create project resources");
        log.debug("Enriching {} resources", entities != null ? entities.size() : 0);

        log.debug("Generating IDs using IdGenService");
        List<String> idList = idGenService.getIdList(request.getRequestInfo(),
                getTenantId(entities),
                projectConfiguration.getProjectResourceIdFormat(), "", entities.size());
        log.debug("Generated {} IDs", idList != null ? idList.size() : 0);

        enrichForCreate(entities, idList, request.getRequestInfo());
        log.info("Successfully completed enrichment for create project resources");
        log.trace("Exiting create (ProjectResourceEnrichmentService)");
    }

    public void update(List<ProjectResource> entities, ProjectResourceBulkRequest request) {
        log.trace("Entering update (ProjectResourceEnrichmentService)");
        log.info("Starting enrichment for update project resources");
        log.debug("Enriching {} resources", entities != null ? entities.size() : 0);
        Map<String, ProjectResource> projectResourceMap = getIdToObjMap(entities);
        log.debug("Created resource map with {} entries", projectResourceMap.size());
        enrichForUpdate(projectResourceMap, entities, request);
        log.info("Successfully completed enrichment for update project resources");
        log.trace("Exiting update (ProjectResourceEnrichmentService)");
    }

    public void delete(List<ProjectResource> entities, ProjectResourceBulkRequest request) {
        log.trace("Entering delete (ProjectResourceEnrichmentService)");
        log.info("Starting enrichment for delete project resources");
        log.debug("Enriching {} resources for delete", entities != null ? entities.size() : 0);
        enrichForDelete(entities, request.getRequestInfo(), true);
        log.info("Successfully completed enrichment for delete project resources");
        log.trace("Exiting delete (ProjectResourceEnrichmentService)");
    }
}
