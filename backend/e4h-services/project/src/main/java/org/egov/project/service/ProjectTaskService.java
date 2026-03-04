package org.egov.project.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.egov.common.data.query.exception.QueryBuilderException;
import org.egov.common.ds.Tuple;
import org.egov.common.http.client.ServiceRequestClient;
import org.egov.common.models.ErrorDetails;
import org.egov.common.models.core.SearchResponse;
import org.egov.common.models.project.Task;
import org.egov.common.models.project.TaskBulkRequest;
import org.egov.common.models.project.TaskRequest;
import org.egov.common.models.project.TaskSearch;
import org.egov.common.service.IdGenService;
import org.egov.common.utils.CommonUtils;
import org.egov.common.validator.Validator;
import org.egov.project.config.ProjectConfiguration;
import org.egov.project.repository.ProjectBeneficiaryRepository;
import org.egov.project.repository.ProjectRepository;
import org.egov.project.repository.ProjectTaskRepository;
import org.egov.project.service.enrichment.ProjectTaskEnrichmentService;
import org.egov.project.validator.task.*;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.egov.common.utils.CommonUtils.*;
import static org.egov.project.Constants.SET_TASKS;
import static org.egov.project.Constants.VALIDATION_ERROR;

@Service
@Slf4j
public class ProjectTaskService {

    public static final String CREATING_BULK_REQUEST = "creating bulk request";
    public static final String PROCESSING_VALID_ENTITIES = "processing {} valid entities";

    private final ProjectTaskRepository projectTaskRepository;

    private final ProjectConfiguration projectConfiguration;

    private final ProjectTaskEnrichmentService enrichmentService;

    private final Predicate<Validator<TaskBulkRequest, Task>> isApplicableForCreate = validator ->
            validator.getClass().equals(PtProjectIdValidator.class)
                    || validator.getClass().equals(PtExistentEntityValidator.class)
                    || validator.getClass().equals(PtIsResouceEmptyValidator.class)

                    || validator.getClass().equals(PtProjectBeneficiaryIdValidator.class)
                    || validator.getClass().equals(PtProductVariantIdValidator.class);


    private final Predicate<Validator<TaskBulkRequest, Task>> isApplicableForUpdate = validator ->
            validator.getClass().equals(PtProjectIdValidator.class)
                    || validator.getClass().equals(PtIsResouceEmptyValidator.class)
                    || validator.getClass().equals(PtProjectBeneficiaryIdValidator.class)
                    || validator.getClass().equals(PtProductVariantIdValidator.class)
                    || validator.getClass().equals(PtNullIdValidator.class)
                    || validator.getClass().equals(PtIsDeletedValidator.class)
                    || validator.getClass().equals(PtIsDeletedSubEntityValidator.class)
                    || validator.getClass().equals(PtNonExistentEntityValidator.class)
                    || validator.getClass().equals(PtRowVersionValidator.class)
                    || validator.getClass().equals(PtUniqueEntityValidator.class)
                    || validator.getClass().equals(PtUniqueSubEntityValidator.class);

    private final Predicate<Validator<TaskBulkRequest, Task>> isApplicableForDelete = validator ->
            validator.getClass().equals(PtNullIdValidator.class)
                    || validator.getClass().equals(PtNonExistentEntityValidator.class);

    private final List<Validator<TaskBulkRequest, Task>> validators;

    public ProjectTaskService(IdGenService idGenService, ProjectRepository projectRepository,
                              ServiceRequestClient serviceRequestClient,
                              ProjectTaskRepository projectTaskRepository,
                              ProjectBeneficiaryRepository projectBeneficiaryRepository, ProjectConfiguration projectConfiguration, ProjectTaskEnrichmentService enrichmentService, List<Validator<TaskBulkRequest, Task>> validators) {
        this.projectTaskRepository = projectTaskRepository;
        this.projectConfiguration = projectConfiguration;
        this.enrichmentService = enrichmentService;
        this.validators = validators;
    }

    public Task create(TaskRequest request) {
        log.trace("Entering create (single task)");
        log.info("Received request to create task");
        TaskBulkRequest bulkRequest = TaskBulkRequest.builder().requestInfo(request.getRequestInfo())
                .tasks(Collections.singletonList(request.getTask())).build();
        log.debug(CREATING_BULK_REQUEST);
        List<Task> tasks = create(bulkRequest, false);
        log.trace("Exiting create (single task)");
        return tasks.get(0);
    }

    public List<Task> create(TaskBulkRequest request, boolean isBulk) {
        log.trace("Entering create (bulk tasks)");
        log.info("Received request to create bulk project tasks");
        Tuple<List<Task>, Map<Task, ErrorDetails>> tuple = validate(validators,
                isApplicableForCreate, request,
                isBulk);
        Map<Task, ErrorDetails> errorDetailsMap = tuple.getY();
        List<Task> validTasks = tuple.getX();
        log.debug("Validation completed - {} valid tasks, {} errors", validTasks.size(), errorDetailsMap.size());
        try {
            if (!validTasks.isEmpty()) {
                log.info(PROCESSING_VALID_ENTITIES, validTasks.size());
                log.debug("Enriching tasks before save");
                enrichmentService.create(validTasks, request);
                log.debug("Saving tasks to repository");
                projectTaskRepository.save(validTasks, projectConfiguration.getCreateProjectTaskTopic());
                log.info("Successfully created {} project tasks", validTasks.size());
            } else {
                log.warn("No valid tasks to create after validation");
            }
        } catch (Exception exception) {
            log.error("Error occurred while creating project tasks", exception);
            populateErrorDetails(request, errorDetailsMap, validTasks, exception, SET_TASKS);
        }

        handleErrors(errorDetailsMap, isBulk, VALIDATION_ERROR);
        log.trace("Exiting create (bulk tasks)");
        return validTasks;
    }

    public Task update(TaskRequest request) {
        log.trace("Entering update (single task)");
        log.info("Received request to update project task");
        TaskBulkRequest bulkRequest = TaskBulkRequest.builder().requestInfo(request.getRequestInfo())
                .tasks(Collections.singletonList(request.getTask())).build();
        log.debug(CREATING_BULK_REQUEST);
        Task result = update(bulkRequest, false).get(0);
        log.trace("Exiting update (single task)");
        return result;
    }

    public List<Task> update(TaskBulkRequest request, boolean isBulk) {
        log.trace("Entering update (bulk tasks)");
        log.info("Received request to update bulk project tasks");
        Tuple<List<Task>, Map<Task, ErrorDetails>> tuple = validate(validators,
                isApplicableForUpdate, request,
                isBulk);
        Map<Task, ErrorDetails> errorDetailsMap = tuple.getY();
        List<Task> validTasks = tuple.getX();
        log.debug("Validation completed - {} valid tasks, {} errors", validTasks.size(), errorDetailsMap.size());
        try {
            if (!validTasks.isEmpty()) {
                log.info(PROCESSING_VALID_ENTITIES, validTasks.size());
                log.debug("Enriching tasks before update");
                enrichmentService.update(validTasks, request);
                log.debug("Saving updated tasks to repository");
                projectTaskRepository.save(validTasks, projectConfiguration.getUpdateProjectTaskTopic());
                log.info("Successfully updated {} project tasks", validTasks.size());
            } else {
                log.warn("No valid tasks to update after validation");
            }
        } catch (Exception exception) {
            log.error("Error occurred while updating project tasks", exception);
            populateErrorDetails(request, errorDetailsMap, validTasks, exception, SET_TASKS);
        }

        handleErrors(errorDetailsMap, isBulk, VALIDATION_ERROR);
        log.trace("Exiting update (bulk tasks)");
        return validTasks;
    }

    public Task delete(TaskRequest request) {
        log.trace("Entering delete (single task)");
        log.info("Received request to delete a project task");
        TaskBulkRequest bulkRequest = TaskBulkRequest.builder().requestInfo(request.getRequestInfo())
                .tasks(Collections.singletonList(request.getTask())).build();
        log.debug(CREATING_BULK_REQUEST);
        Task result = delete(bulkRequest, false).get(0);
        log.trace("Exiting delete (single task)");
        return result;
    }

    public List<Task> delete(TaskBulkRequest request, boolean isBulk) {
        log.trace("Entering delete (bulk tasks)");
        log.info("Received request to delete bulk project tasks");
        Tuple<List<Task>, Map<Task, ErrorDetails>> tuple = validate(validators,
                isApplicableForDelete, request,
                isBulk);
        Map<Task, ErrorDetails> errorDetailsMap = tuple.getY();
        List<Task> validTasks = tuple.getX();
        log.debug("Validation completed - {} valid tasks, {} errors", validTasks.size(), errorDetailsMap.size());
        try {
            if (!validTasks.isEmpty()) {
                log.info(PROCESSING_VALID_ENTITIES, validTasks.size());
                log.debug("Enriching tasks before delete");
                enrichmentService.delete(validTasks, request);
                log.debug("Saving deleted tasks to repository");
                projectTaskRepository.save(validTasks, projectConfiguration.getDeleteProjectTaskTopic());
                log.info("Successfully deleted {} project tasks", validTasks.size());
            } else {
                log.warn("No valid tasks to delete after validation");
            }
        } catch (Exception exception) {
            log.error("Error occurred while deleting project tasks", exception);
            populateErrorDetails(request, errorDetailsMap, validTasks, exception, SET_TASKS);
        }

        handleErrors(errorDetailsMap, isBulk, VALIDATION_ERROR);
        log.trace("Exiting delete (bulk tasks)");
        return validTasks;
    }

    private Tuple<List<Task>, Map<Task, ErrorDetails>> validate(List<Validator<TaskBulkRequest, Task>> validators,
                                                                Predicate<Validator<TaskBulkRequest, Task>> applicableValidators,
                                                                TaskBulkRequest request, boolean isBulk) {
        log.trace("Entering validate for {} tasks", request.getTasks() != null ? request.getTasks().size() : 0);
        log.debug("Validating request with {} validators", validators.size());
        Map<Task, ErrorDetails> errorDetailsMap = new HashMap<>();
        if (!errorDetailsMap.isEmpty() && !isBulk) {
            log.error("Validation errors found: {}", errorDetailsMap.values());
            throw new CustomException(VALIDATION_ERROR, errorDetailsMap.values().toString());
        }
        List<Task> validTasks = request.getTasks().stream()
                .filter(notHavingErrors()).toList();
        log.debug("Validation completed - {} valid tasks out of {}", validTasks.size(), request.getTasks().size());
        log.trace("Exiting validate");
        return new Tuple<>(validTasks, errorDetailsMap);
    }

    public SearchResponse<Task> search(TaskSearch taskSearch, Integer limit, Integer offset, String tenantId,
                                       Long lastChangedSince, Boolean includeDeleted) {
        log.trace("Entering search");
        log.info("Received request to search project tasks");

        String idFieldName = getIdFieldName(taskSearch);
        if (isSearchByIdOnly(taskSearch, idFieldName)) {
            log.info("Searching project tasks by ID");
            List<String> ids = (List<String>) ReflectionUtils.invokeMethod(getIdMethod(Collections
                            .singletonList(taskSearch)),
                    taskSearch);
            log.debug("Fetching project tasks with {} IDs", ids != null ? ids.size() : 0);
            SearchResponse<Task> searchResponse = projectTaskRepository.findById(ids,
                    idFieldName, includeDeleted);
            log.debug("Found {} tasks before filtering", searchResponse.getResponse() != null ? searchResponse.getResponse().size() : 0);
            SearchResponse<Task> result = SearchResponse.<Task>builder().response(searchResponse.getResponse().stream()
                    .filter(lastChangedSince(lastChangedSince))
                    .filter(havingTenantId(tenantId))
                    .filter(includeDeleted(includeDeleted))
                    .toList()).totalCount(searchResponse.getTotalCount()).build();
            log.info("Search by ID completed - found {} tasks", result.getResponse() != null ? result.getResponse().size() : 0);
            log.trace("Exiting search");
            return result;
        }

        try {
            log.info("Searching project tasks using criteria");
            log.debug("Search parameters - limit: {}, offset: {}, tenantId: {}", limit, offset, tenantId);
            SearchResponse<Task> result = projectTaskRepository.find(taskSearch, limit, offset,
                    tenantId, lastChangedSince, includeDeleted);
            log.info("Search by criteria completed - found {} tasks", result.getResponse() != null ? result.getResponse().size() : 0);
            log.trace("Exiting search");
            return result;
        } catch (QueryBuilderException e) {
            log.error("Error in building query", e);
            throw new CustomException("ERROR_IN_QUERY", e.getMessage());
        }
    }

    public void putInCache(List<Task> tasks) {
        log.trace("Entering putInCache for {} tasks", tasks != null ? tasks.size() : 0);
        log.info("Putting {} project tasks in cache", tasks != null ? tasks.size() : 0);
        projectTaskRepository.putInCache(tasks);
        log.info("Successfully put project tasks in cache");
        log.trace("Exiting putInCache");
    }
}
