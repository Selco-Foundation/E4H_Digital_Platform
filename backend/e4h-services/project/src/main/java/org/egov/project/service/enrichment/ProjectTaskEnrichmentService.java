package org.egov.project.service.enrichment;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.models.project.Address;
import org.egov.common.models.project.Task;
import org.egov.common.models.project.TaskBulkRequest;
import org.egov.common.models.project.TaskResource;
import org.egov.common.service.IdGenService;
import org.egov.project.config.ProjectConfiguration;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.egov.common.utils.CommonUtils.*;

@Service
@Slf4j
public class ProjectTaskEnrichmentService {

    public static final String ENRICHING_RESOURCES = "enriching resources";
    public static final String ENRICHMENT_DONE = "enrichment done";
    private final IdGenService idGenService;

    private final ProjectConfiguration projectConfiguration;

    public ProjectTaskEnrichmentService(IdGenService idGenService, ProjectConfiguration projectConfiguration) {
        this.idGenService = idGenService;
        this.projectConfiguration = projectConfiguration;
    }

    private static void updateAuditDetailsForTask(TaskBulkRequest request, Task task) {
        AuditDetails existingAuditDetails = task.getAuditDetails();
        AuditDetails auditDetails = getAuditDetailsForUpdate(existingAuditDetails,
                request.getRequestInfo().getUserInfo().getUuid());
        task.setAuditDetails(auditDetails);
    }

    private static void updateAuditDetailsForResource(TaskBulkRequest request, TaskResource resource) {
        AuditDetails existingAuditDetails = resource.getAuditDetails();
        AuditDetails auditDetails = getAuditDetailsForUpdate(existingAuditDetails,
                request.getRequestInfo().getUserInfo().getUuid());
        resource.setAuditDetails(auditDetails);
    }

    private static void enrichResourcesForUpdate(TaskBulkRequest request, List<Task> tasks) {
        log.info(ENRICHING_RESOURCES);
        for (Task task : tasks) {
            if (CollectionUtils.isEmpty(task.getResources())) continue;
            List<TaskResource> resourcesToCreate = task.getResources().stream()
                    .filter(r -> r.getId() == null).toList();
            List<TaskResource> resourcesToUpdate = task.getResources().stream()
                    .filter(r -> r.getId() != null).toList();

            if (!resourcesToCreate.isEmpty()) {
                enrichResourcesForCreate(request, resourcesToCreate, task.getId());
            }
            for (TaskResource resource : resourcesToUpdate) {
                updateAuditDetailsForResource(request, resource);
            }
        }
    }

    private static void enrichAddressesForUpdate(List<Task> validTasks) {
        List<Address> addressesToCreate = validTasks.stream()
                .filter(ad1 -> ad1.getAddress() != null && ad1.getAddress().getId() == null)
                .map(Task::getAddress).toList();

        if (!addressesToCreate.isEmpty()) {
            log.info("enriching addresses to create");
            List<String> addressIdList = uuidSupplier().apply(addressesToCreate.size());
            enrichId(addressesToCreate, addressIdList);
        }
    }

    private static void enrichAddressesForCreate(List<Task> validTasks) {
        List<Address> addresses = validTasks.stream().map(Task::getAddress)
                .toList();
        if (!addresses.isEmpty()) {
            log.info("enriching addresses");
            List<String> addressIdList = uuidSupplier().apply(addresses.size());
            enrichId(addresses, addressIdList);
        }
    }

    private static void enrichResourcesForCreate(TaskBulkRequest request,
                                                 List<Task> validTasks) {
        for (Task task : validTasks) {
            log.info(ENRICHING_RESOURCES);
            List<TaskResource> resources = task.getResources();
            if (CollectionUtils.isEmpty(resources))
                continue;
            enrichResourcesForCreate(request, resources, task.getId());
        }
    }

    private static void enrichResourcesForCreate(TaskBulkRequest request,
                                                 List<TaskResource> resources, String taskId) {
        log.info(ENRICHING_RESOURCES);
        List<String> ids = uuidSupplier().apply(resources.size());
        enrichForCreate(resources, ids, request.getRequestInfo(), false);
        resources.forEach(taskResource -> taskResource.setTaskId(taskId));
    }

    public void create(List<Task> validTasks, TaskBulkRequest request) throws Exception {
        log.trace("Entering create (ProjectTaskEnrichmentService)");
        log.info("Starting enrichment for create tasks");
        log.debug("Enriching {} tasks", validTasks != null ? validTasks.size() : 0);

        log.debug("Generating IDs for tasks");
        List<String> taskIdList = idGenService.getIdList(request.getRequestInfo(),
                getTenantId(request.getTasks()),
                projectConfiguration.getProjectTaskIdFormat(),
                "", request.getTasks().size());
        log.debug("Generated {} task IDs", taskIdList != null ? taskIdList.size() : 0);
        log.debug("Enriching tasks with IDs and audit details");
        enrichForCreate(validTasks, taskIdList, request.getRequestInfo());
        log.debug("Enriching task addresses");
        enrichAddressesForCreate(validTasks);
        log.debug("Enriching task resources");
        enrichResourcesForCreate(request, validTasks);
        log.info("Successfully completed enrichment for create tasks");
        log.trace("Exiting create (ProjectTaskEnrichmentService)");
    }

    public void update(List<Task> validTasks, TaskBulkRequest request) throws Exception {
        log.trace("Entering update (ProjectTaskEnrichmentService)");
        log.info("Starting enrichment for update tasks");
        log.debug("Enriching {} tasks", validTasks != null ? validTasks.size() : 0);
        log.debug("Enriching task addresses for update");
        enrichAddressesForUpdate(validTasks);
        log.debug("Enriching task resources for update");
        enrichResourcesForUpdate(request, validTasks);
        Map<String, Task> iMap = getIdToObjMap(validTasks);
        log.debug("Created task map with {} entries", iMap.size());
        enrichForUpdate(iMap, request);
        log.info("Successfully completed enrichment for update tasks");
        log.trace("Exiting update (ProjectTaskEnrichmentService)");
    }

    public void delete(List<Task> validTasks, TaskBulkRequest request) throws Exception {
        log.trace("Entering delete (ProjectTaskEnrichmentService)");
        log.info("Starting enrichment for delete tasks");
        log.debug("Enriching {} tasks for delete", validTasks != null ? validTasks.size() : 0);
        for (Task task : validTasks) {
            if (task.getIsDeleted()) {
                log.debug("Task is marked as deleted, enriching all resources");
                if (!CollectionUtils.isEmpty(task.getResources())) {
                    log.debug("Enriching {} resources for delete", task.getResources().size());
                    for (TaskResource resource : task.getResources()) {
                        resource.setIsDeleted(true);
                        updateAuditDetailsForResource(request, resource);
                    }
                }
                updateAuditDetailsForTask(request, task);
                task.setRowVersion(task.getRowVersion() + 1);
            } else {
                int previousRowVersion = task.getRowVersion();
                log.debug("Task not deleted, enriching only deleted resources");
                if (!CollectionUtils.isEmpty(task.getResources())) {
                    long deletedResourceCount = task.getResources().stream().filter(TaskResource::getIsDeleted).count();
                    log.debug("Found {} deleted resources to enrich", deletedResourceCount);
                    task.getResources().stream().filter(TaskResource::getIsDeleted).forEach(resource -> {
                        updateAuditDetailsForResource(request, resource);
                        updateAuditDetailsForTask(request, task);
                        task.setRowVersion(previousRowVersion + 1);
                    });
                }
            }
        }
        log.info("Successfully completed enrichment for delete tasks");
        log.trace("Exiting delete (ProjectTaskEnrichmentService)");
    }
}
