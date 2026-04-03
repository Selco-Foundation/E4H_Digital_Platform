package org.egov.project.service.enrichment;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.models.project.ProjectFacility;
import org.egov.common.models.project.ProjectFacilityBulkRequest;
import org.egov.common.service.IdGenService;
import org.egov.project.config.ProjectConfiguration;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static org.egov.common.utils.CommonUtils.*;

@Service
@Slf4j
public class ProjectFacilityEnrichmentService {

    public static final String ENRICHMENT_DONE = "enrichment done";
    private final IdGenService idGenService;

    private final ProjectConfiguration projectConfiguration;

    public ProjectFacilityEnrichmentService(IdGenService idGenService, ProjectConfiguration projectConfiguration) {
        this.idGenService = idGenService;
        this.projectConfiguration = projectConfiguration;
    }

    public void create(List<ProjectFacility> entities, ProjectFacilityBulkRequest request) throws Exception {
        log.trace("Entering create (ProjectFacilityEnrichmentService)");
        log.info("Starting enrichment for create project facilities");
        log.debug("Enriching {} facilities", entities != null ? entities.size() : 0);

        log.debug("Generating IDs using IdGenService");
        List<String> idList = idGenService.getIdList(request.getRequestInfo(),
                getTenantId(entities),
                projectConfiguration.getProjectFacilityIdFormat(), "", entities.size());
        log.debug("Generated {} IDs", idList != null ? idList.size() : 0);

        enrichForCreate(entities, idList, request.getRequestInfo());
        log.info("Successfully completed enrichment for create project facilities");
        log.trace("Exiting create (ProjectFacilityEnrichmentService)");
    }

    public void update(List<ProjectFacility> entities, ProjectFacilityBulkRequest request) {
        log.trace("Entering update (ProjectFacilityEnrichmentService)");
        log.info("Starting enrichment for update project facilities");
        log.debug("Enriching {} facilities", entities != null ? entities.size() : 0);
        Map<String, ProjectFacility> projectFacilityMap = getIdToObjMap(entities);
        log.debug("Created facility map with {} entries", projectFacilityMap.size());
        enrichForUpdate(projectFacilityMap, entities, request);
        log.info("Successfully completed enrichment for update project facilities");
        log.trace("Exiting update (ProjectFacilityEnrichmentService)");
    }

    public void delete(List<ProjectFacility> entities, ProjectFacilityBulkRequest request) {
        log.trace("Entering delete (ProjectFacilityEnrichmentService)");
        log.info("Starting enrichment for delete project facilities");
        log.debug("Enriching {} facilities for delete", entities != null ? entities.size() : 0);
        enrichForDelete(entities, request.getRequestInfo(), true);
        log.info("Successfully completed enrichment for delete project facilities");
        log.trace("Exiting delete (ProjectFacilityEnrichmentService)");
    }
}
