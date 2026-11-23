package org.egov.field_planner.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;
import org.egov.field_planner.config.FieldPlannerConfiguration;
import org.egov.field_planner.util.AssessmentConstants;
import org.egov.field_planner.web.models.ProcessInstance;
import org.egov.field_planner.web.models.ProcessInstanceRequest;
import org.egov.field_planner.web.models.ProcessInstanceResponse;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class AssessmentWorkflowService {

    private final ObjectMapper mapper;
    private final FieldPlannerConfiguration configuration;
    private final ServiceRequestRepository repository;

    public AssessmentWorkflowService(
            @Qualifier("objectMapper") ObjectMapper mapper,
            FieldPlannerConfiguration configuration,
            ServiceRequestRepository repository) {
        this.mapper = mapper;
        this.configuration = configuration;
        this.repository = repository;
    }

    public ProcessInstance transitionWorkflow(String businessId, String tenantId, String action,
                                                RequestInfo requestInfo, String comment) {
        log.info("Transitioning assessment workflow for businessId: {}, action: {}", businessId, action);
        ProcessInstance instance = ProcessInstance.builder()
                .businessId(businessId)
                .tenantId(tenantId)
                .moduleName(configuration.getModuleName())
                .businessService(configuration.getAssessmentBusinessService())
                .action(action)
                .comment(comment)
                .build();

        ProcessInstanceRequest wfRequest = ProcessInstanceRequest.builder()
                .requestInfo(requestInfo)
                .processInstances(List.of(instance))
                .build();

        String url = configuration.getWfHost() + configuration.getWfTransitionPath();
        Object response = repository.fetchResult(new StringBuilder(url), wfRequest);
        ProcessInstanceResponse wfResponse = mapper.convertValue(response, ProcessInstanceResponse.class);
        if (wfResponse == null || wfResponse.getProcessInstances() == null || wfResponse.getProcessInstances().isEmpty()) {
            throw new CustomException(AssessmentConstants.WORKFLOW_TRANSITION_FAILED,
                    "Empty response from workflow transition for action: " + action);
        }
        return wfResponse.getProcessInstances().get(0);
    }

    public ProcessInstance transitionSystemWorkflow(String businessId, String tenantId, String action,
                                                      RequestInfo requestInfo, String comment) {
        return transitionWorkflow(businessId, tenantId, action, systemRequestInfo(requestInfo, tenantId), comment);
    }

    private List<Role> normalizeRolesForWorkflowTenant(List<Role> sourceRoles, String workflowTenantId) {
        List<Role> roles = new ArrayList<>();
        if (sourceRoles == null) {
            return roles;
        }
        for (Role role : sourceRoles) {
            if (role == null || role.getCode() == null) {
                continue;
            }
            String roleTenantId = role.getTenantId();
            if (roleTenantId == null || roleTenantId.isBlank()) {
                roleTenantId = workflowTenantId;
            }
            roles.add(Role.builder()
                    .name(role.getName())
                    .code(role.getCode())
                    .tenantId(roleTenantId)
                    .build());
        }
        return roles;
    }

    private void ensureRoleForTenant(List<Role> roles, String roleCode, String roleName, String tenantId) {
        boolean present = roles.stream()
                .anyMatch(role -> roleCode.equals(role.getCode()) && tenantId.equals(role.getTenantId()));
        if (!present) {
            roles.add(Role.builder()
                    .name(roleName)
                    .code(roleCode)
                    .tenantId(tenantId)
                    .build());
        }
    }

    private String resolveStateLevelTenant(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            return "in";
        }
        int dotIndex = tenantId.indexOf('.');
        return dotIndex > 0 ? tenantId.substring(0, dotIndex) : tenantId;
    }

    private RequestInfo systemRequestInfo(RequestInfo requestInfo, String tenantId) {
        if (requestInfo == null || requestInfo.getUserInfo() == null) {
            throw new CustomException(AssessmentConstants.WORKFLOW_TRANSITION_FAILED,
                    "RequestInfo with userInfo is required for system workflow transition");
        }
        User user = requestInfo.getUserInfo();
        String workflowTenantId = resolveStateLevelTenant(tenantId);
        List<Role> roles = normalizeRolesForWorkflowTenant(user.getRoles(), workflowTenantId);
        ensureRoleForTenant(roles, AssessmentConstants.ROLE_SYSTEM_USER, "System User", workflowTenantId);
        User workflowUser = User.builder()
                .uuid(user.getUuid())
                .userName(user.getUserName())
                .name(user.getName())
                .type(user.getType())
                .tenantId(user.getTenantId() != null ? user.getTenantId() : workflowTenantId)
                .mobileNumber(user.getMobileNumber())
                .emailId(user.getEmailId())
                .roles(roles)
                .build();
        return RequestInfo.builder()
                .apiId(requestInfo.getApiId())
                .authToken(requestInfo.getAuthToken())
                .userInfo(workflowUser)
                .build();
    }
}
