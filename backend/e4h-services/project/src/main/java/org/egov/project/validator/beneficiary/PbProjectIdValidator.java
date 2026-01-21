package org.egov.project.validator.beneficiary;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.models.Error;
import org.egov.common.models.project.BeneficiaryBulkRequest;
import org.egov.common.models.project.ProjectBeneficiary;
import org.egov.common.validator.Validator;
import org.egov.project.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.egov.common.utils.CommonUtils.*;
import static org.egov.common.utils.ValidatorUtils.getErrorForNonExistentEntity;

@Component
@Order(value = 3)
@Slf4j
public class PbProjectIdValidator implements Validator<BeneficiaryBulkRequest, ProjectBeneficiary> {

    private final ProjectRepository projectRepository;

    @Autowired
    public PbProjectIdValidator(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }


    @Override
    public Map<ProjectBeneficiary, List<Error>> validate(BeneficiaryBulkRequest request) {
        log.trace("Entering validate (PbProjectIdValidator)");
        log.info("Validating project ID");
        Map<ProjectBeneficiary, List<Error>> errorDetailsMap = new HashMap<>();
        List<ProjectBeneficiary> entities = request.getProjectBeneficiaries();
        log.debug("Validating {} beneficiaries for project ID", entities != null ? entities.size() : 0);
        Class<?> objClass = getObjClass(entities);
        Method idMethod = getMethod("getProjectId", objClass);
        Map<String, ProjectBeneficiary> eMap = getIdToObjMap(entities
                .stream().filter(notHavingErrors()).toList(), idMethod);
        if (!eMap.isEmpty()) {
            List<String> entityIds = new ArrayList<>(eMap.keySet());
            log.debug("Validating {} project IDs against repository", entityIds.size());
            List<String> existingProjectIds = projectRepository.validateIds(entityIds,
                    getIdFieldName(idMethod));
            log.debug("Found {} existing project IDs", existingProjectIds != null ? existingProjectIds.size() : 0);
            List<ProjectBeneficiary> invalidEntities = entities.stream().filter(notHavingErrors()).filter(entity ->
                            !existingProjectIds.contains(entity.getProjectId()))
                    .toList();
            if (!invalidEntities.isEmpty()) {
                log.warn("Found {} beneficiaries with invalid project IDs", invalidEntities.size());
            }
            invalidEntities.forEach(projectBeneficiary -> {
                Error error = getErrorForNonExistentEntity();
                populateErrorDetails(projectBeneficiary, error, errorDetailsMap);
            });
        }

        log.debug("Project ID validation completed - found {} errors", errorDetailsMap.size());
        log.trace("Exiting validate (PbProjectIdValidator)");
        return errorDetailsMap;
    }
}