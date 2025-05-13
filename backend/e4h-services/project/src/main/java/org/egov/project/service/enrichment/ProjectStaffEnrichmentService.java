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
        log.info("starting the enrichment for create project staff");

        log.info("generating IDs using IdGenService");
        List<String> idList = idGenService.getIdList(request.getRequestInfo(),
                getTenantId(entities),
                projectConfiguration.getProjectStaffIdFormat(), "", entities.size());

        enrichForCreate(entities, idList, request.getRequestInfo());
        log.info(ENRICHMENT_DONE);
    }

    public void update(List<ProjectStaff> entities, ProjectStaffBulkRequest request) {
        log.info("starting the enrichment for update project staff");
        Map<String, ProjectStaff> projectStaffMap = getIdToObjMap(entities);
        enrichForUpdate(projectStaffMap, entities, request);
        log.info(ENRICHMENT_DONE);
    }

    public void delete(List<ProjectStaff> entities, ProjectStaffBulkRequest request) {
        log.info("starting the enrichment for delete project staff");
        enrichForDelete(entities, request.getRequestInfo(), true);
        log.info(ENRICHMENT_DONE);
    }
}
