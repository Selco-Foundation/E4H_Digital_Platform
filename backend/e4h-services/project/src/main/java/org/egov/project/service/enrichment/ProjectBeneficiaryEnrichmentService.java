package org.egov.project.service.enrichment;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.models.project.BeneficiaryBulkRequest;
import org.egov.common.models.project.ProjectBeneficiary;
import org.egov.common.service.IdGenService;
import org.egov.project.config.ProjectConfiguration;
import org.egov.project.repository.ProjectBeneficiaryRepository;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.egov.common.utils.CommonUtils.*;

@Service
@Slf4j
public class ProjectBeneficiaryEnrichmentService {

    public static final String ENRICHMENT_DONE = "enrichment done";
    private final IdGenService idGenService;

    private final ProjectConfiguration projectConfiguration;

    private final ProjectBeneficiaryRepository projectBeneficiaryRepository;

    public ProjectBeneficiaryEnrichmentService(IdGenService idGenService,
                                               ProjectConfiguration projectConfiguration,
                                               ProjectBeneficiaryRepository projectBeneficiaryRepository) {
        this.idGenService = idGenService;
        this.projectConfiguration = projectConfiguration;
        this.projectBeneficiaryRepository = projectBeneficiaryRepository;
    }


    public void create(List<ProjectBeneficiary> validProjectBeneficiaries,
                       BeneficiaryBulkRequest beneficiaryRequest) throws Exception {
        log.trace("Entering create (ProjectBeneficiaryEnrichmentService)");
        log.info("Starting enrichment for create project beneficiaries");
        log.debug("Enriching {} beneficiaries", validProjectBeneficiaries != null ? validProjectBeneficiaries.size() : 0);

        log.debug("Extracting tenant ID");
        String tenantId = getTenantId(validProjectBeneficiaries);
        log.debug("Tenant ID: {}", tenantId);

        log.debug("Generating IDs using IdGenService");
        List<String> idList = idGenService.getIdList(beneficiaryRequest.getRequestInfo(),
                tenantId,
                projectConfiguration.getProjectBeneficiaryIdFormat(),
                "",
                validProjectBeneficiaries.size());
        log.debug("Generated {} IDs", idList != null ? idList.size() : 0);

        enrichForCreate(validProjectBeneficiaries, idList, beneficiaryRequest.getRequestInfo());
        log.info("Successfully completed enrichment for create project beneficiaries");
        log.trace("Exiting create (ProjectBeneficiaryEnrichmentService)");
    }

    public void update(List<ProjectBeneficiary> validProjectBeneficiaries,
                       BeneficiaryBulkRequest beneficiaryRequest) {
        log.trace("Entering update (ProjectBeneficiaryEnrichmentService)");
        log.info("Starting enrichment for update project beneficiaries");
        log.debug("Enriching {} beneficiaries", validProjectBeneficiaries != null ? validProjectBeneficiaries.size() : 0);
        Method idMethod = getIdMethod(validProjectBeneficiaries);
        Map<String, ProjectBeneficiary> projectBeneficiaryMap = getIdToObjMap(validProjectBeneficiaries, idMethod);
        List<String> projectBeneficiaryIds = new ArrayList<>(projectBeneficiaryMap.keySet());
        log.debug("Fetching {} existing beneficiaries from repository", projectBeneficiaryIds.size());
        List<ProjectBeneficiary> existingProjectBeneficiaryIds = projectBeneficiaryRepository.findById(
                projectBeneficiaryIds,
                getIdFieldName(idMethod),
                false
        ).getResponse();
        log.debug("Found {} existing beneficiaries", existingProjectBeneficiaryIds != null ? existingProjectBeneficiaryIds.size() : 0);

        log.debug("Updating IDs from existing entities");
        enrichIdsFromExistingEntities(projectBeneficiaryMap, existingProjectBeneficiaryIds, idMethod);

        log.debug("Updating lastModifiedTime and lastModifiedBy");
        enrichForUpdate(projectBeneficiaryMap, existingProjectBeneficiaryIds, beneficiaryRequest, idMethod);

        log.info("Successfully completed enrichment for update project beneficiaries");
        log.trace("Exiting update (ProjectBeneficiaryEnrichmentService)");
    }

    public void delete(List<ProjectBeneficiary> validProjectBeneficiaries,
                       BeneficiaryBulkRequest beneficiaryRequest) {
        log.trace("Entering delete (ProjectBeneficiaryEnrichmentService)");
        log.info("Starting enrichment for delete project beneficiaries");
        log.debug("Enriching {} beneficiaries for delete", validProjectBeneficiaries != null ? validProjectBeneficiaries.size() : 0);
        enrichForDelete(validProjectBeneficiaries, beneficiaryRequest.getRequestInfo(), true);
        log.info("Successfully completed enrichment for delete project beneficiaries");
        log.trace("Exiting delete (ProjectBeneficiaryEnrichmentService)");
    }
}
