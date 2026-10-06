package org.egov.amc.validator;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.amc.web.models.AmcPlan;
import org.egov.amc.web.models.AmcPlanRequest;
import org.egov.amc.web.models.AmcPlanSearchCriteria;
import org.egov.amc.web.models.AmcPlanSearchRequest;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class AmcPlanValidator {

    public static final String TENANT_ID_IS_MANDATORY_IN_AMC_PLAN_REQUEST_BODY = "Tenant ID is mandatory in AmcPlan request body";

    @Autowired
    public AmcPlanValidator() {
    }

    private void validateRequestInfo(RequestInfo requestInfo) {
        if (requestInfo == null) {
            throw new CustomException("REQUEST_INFO", "Request info is mandatory");
        }
        if (requestInfo.getUserInfo() == null || StringUtils.isBlank(requestInfo.getUserInfo().getUuid())) {
            throw new CustomException("USERINFO_UUID", "UUID is mandatory in UserInfo");
        }
    }

    public void validateCreateAmcPlanRequest(AmcPlanRequest request) {
        log.trace("Entering validateCreateAmcPlanRequest method");
        validateRequestInfo(request.getRequestInfo());
        validateMultipleTenantIds(request);

        for (AmcPlan amcPlan : request.getAmcPlans()) {
            Map<String, String> errorMap = new HashMap<>();
            if (StringUtils.isBlank(amcPlan.getTenantId())) {
                errorMap.put("TENANT_ID", TENANT_ID_IS_MANDATORY_IN_AMC_PLAN_REQUEST_BODY);
            }
            if (StringUtils.isBlank(amcPlan.getProjectId())) {
                errorMap.put("PROJECT_ID", "projectId is mandatory in AmcPlan request body");
            }
            if (amcPlan.getHealthFacilityNumber() == null || amcPlan.getHealthFacilityNumber() <= 0) {
                errorMap.put("HEALTH_FACILITY_NUMBER", "healthFacilityNumber must be a positive integer");
            }
            if (amcPlan.getStartDate() == null || amcPlan.getStartDate() <= 0) {
                errorMap.put("START_DATE", "startDate is mandatory in AmcPlan request body");
            }
            if (amcPlan.getEndDate() == null || amcPlan.getEndDate() <= 0) {
                errorMap.put("END_DATE", "endDate is mandatory in AmcPlan request body");
            }
            if (amcPlan.getStartDate() != null && amcPlan.getEndDate() != null && amcPlan.getStartDate() >= amcPlan.getEndDate()) {
                errorMap.put("INVALID_DATE_RANGE", "startDate should be less than endDate");
            }
            if (CollectionUtils.isEmpty(amcPlan.getGeographyScope())) {
                errorMap.put("GEOGRAPHY_SCOPE", "geographyScope is mandatory in AmcPlan request body");
            }
            if (!errorMap.isEmpty()) {
                throw new CustomException(errorMap);
            }
        }
        log.debug("Create AMC plan request validation completed successfully");
    }

    public void validateUpdateRequestIdentifiers(AmcPlanRequest request) {
        log.trace("Entering validateUpdateRequestIdentifiers method");
        validateRequestInfo(request.getRequestInfo());
        if (CollectionUtils.isEmpty(request.getAmcPlans())) {
            throw new CustomException("AmcPlan", "AMC plans are mandatory");
        }
        for (AmcPlan amcPlan : request.getAmcPlans()) {
            if (StringUtils.isBlank(amcPlan.getId())) {
                throw new CustomException("UPDATE_AMC_PLAN", "AmcPlan id is mandatory for update");
            }
            if (StringUtils.isBlank(amcPlan.getTenantId())) {
                throw new CustomException("TENANT_ID", TENANT_ID_IS_MANDATORY_IN_AMC_PLAN_REQUEST_BODY);
            }
        }
        validateMultipleTenantIds(request);
    }

    /**
     * Only projectId/tenantId must stay identical between the DB row and the update payload -
     * everything else (healthFacilityNumber, startDate, endDate, geographyScope, selectedActivities,
     * status, additionalDetails) is updatable. Mirrors AmcConfigurationService.isValidCascadingUpdate.
     */
    public void validateUpdateAgainstDB(List<AmcPlan> amcPlansFromRequest, List<AmcPlan> amcPlansFromDB) {
        Map<String, AmcPlan> byId = amcPlansFromDB.stream().collect(Collectors.toMap(AmcPlan::getId, p -> p));
        for (AmcPlan amcPlan : amcPlansFromRequest) {
            AmcPlan fromDB = byId.get(amcPlan.getId());
            if (fromDB == null) {
                throw new CustomException("AMC_PLAN_NOT_FOUND",
                        "AmcPlan with id " + amcPlan.getId() + " that you are trying to update does not exist");
            }
            if (!Objects.equals(fromDB.getTenantId(), amcPlan.getTenantId())
                    || !Objects.equals(fromDB.getProjectId(), amcPlan.getProjectId())) {
                throw new CustomException("AMC_PLAN_UPDATE_ERROR",
                        "Cannot change tenantId or projectId of an existing AmcPlan");
            }
        }
    }

    public void validateSearchAmcPlanRequest(AmcPlanSearchRequest request, Integer limit, Integer offset, String tenantId) {
        validateRequestInfo(request.getRequestInfo());
        if (limit == null) {
            throw new CustomException("SEARCH_AMC_PLAN.LIMIT", "limit is mandatory for AmcPlan search");
        }
        if (offset == null) {
            throw new CustomException("SEARCH_AMC_PLAN.OFFSET", "offset is mandatory for AmcPlan search");
        }
        if (StringUtils.isBlank(tenantId)) {
            throw new CustomException("SEARCH_AMC_PLAN.TENANT_ID", "tenantId is mandatory for AmcPlan search");
        }
        AmcPlanSearchCriteria criteria = request.getSearchCriteria();
        if (criteria != null && StringUtils.isNotBlank(criteria.getTenantId())
                && !criteria.getTenantId().equals(tenantId) && !tenantId.startsWith(criteria.getTenantId())) {
            throw new CustomException("SEARCH_AMC_PLAN.TENANT_ID", "tenantId in criteria does not match URL tenantId");
        }
    }

    private void validateMultipleTenantIds(AmcPlanRequest request) {
        Set<String> tenantIds = request.getAmcPlans().stream().map(AmcPlan::getTenantId).collect(Collectors.toSet());
        if (tenantIds.size() > 1) {
            throw new CustomException("MULTIPLE_TENANT_IDS", "AmcPlan(s) with multiple tenant IDs cannot be created/updated together");
        }
    }
}
