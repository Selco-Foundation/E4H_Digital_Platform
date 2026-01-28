package org.egov.field_planner.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.http.client.ServiceRequestClient;
import org.egov.common.models.project.Project;
import org.egov.common.models.project.ProjectRequest;
import org.egov.field_planner.web.models.FieldPlanRequest;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.field_planner.config.FieldPlannerConfiguration;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import static org.egov.field_planner.util.FieldPlannerConstants.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class MDMSUtils {

    public static final String FILTER_CODE = "$.*.code";
    public static final String FILTER_ACTIVE_TRUE = "$.[?(@.active==true)]";
    private final ServiceRequestClient serviceRequestRepository;
    private final FieldPlannerConfiguration config;

    public Object mDMSCall(FieldPlanRequest request, String tenantId) {
        log.trace("Entering mDMSCall method for tenant: {}", tenantId);
        log.debug("Calling MDMS service for tenant: {}", tenantId);
        
        RequestInfo requestInfo = request.getRequestInfo();
        MdmsCriteriaReq mdmsCriteriaReq = getMDMSRequest(requestInfo, tenantId);
        Object result = null;
        try {
            result = serviceRequestRepository.fetchResult(getMdmsSearchUrl(), mdmsCriteriaReq, LinkedHashMap.class);
            log.debug("MDMS service call completed successfully for tenant: {}", tenantId);
        } catch (Exception e) {
            log.error("Error while calling MDMS service for tenant: {}", tenantId, e);
            throw new CustomException("MDMS_ERROR", "error while calling mdms");
        }
        log.trace("Exiting mDMSCall method");
        return result;
    }

    public MdmsCriteriaReq getMDMSRequest(RequestInfo requestInfo, String tenantId) {
        log.trace("Entering getMDMSRequest method for tenant: {}", tenantId);
        log.debug("Building MDMS request criteria");

        ModuleDetail activitiesMDMSModuleDetail = getActivitiesModuleRequestData();
        ModuleDetail stateInfoModuleDetail = getStateModuleRequestData();
        ModuleDetail tenantModuleDetail = getTenantModuleRequestData();

        List<ModuleDetail> moduleDetails = new LinkedList<>();
        moduleDetails.add(activitiesMDMSModuleDetail);
        moduleDetails.add(stateInfoModuleDetail);
        moduleDetails.add(tenantModuleDetail);
        log.debug("Added {} module details to MDMS request", moduleDetails.size());

        MdmsCriteria mdmsCriteria = MdmsCriteria.builder().moduleDetails(moduleDetails).tenantId(tenantId)
                .build();

        MdmsCriteriaReq mdmsCriteriaReq = MdmsCriteriaReq.builder().mdmsCriteria(mdmsCriteria)
                .requestInfo(requestInfo).build();
        log.trace("Exiting getMDMSRequest method");
        return mdmsCriteriaReq;
    }

    private ModuleDetail getActivitiesModuleRequestData() {
        log.trace("Entering getActivitiesModuleRequestData method");
        List<MasterDetail> projectActivitiesMasterDetails = new ArrayList<>();

        MasterDetail departmentMasterDetails = MasterDetail.builder().name(MASTER_ACTIVITIES)
                .filter(FILTER_ACTIVE_TRUE).build();
        projectActivitiesMasterDetails.add(departmentMasterDetails);

        ModuleDetail projectDepartmentModuleDetail = ModuleDetail.builder().masterDetails(projectActivitiesMasterDetails)
                .moduleName(MDMS_COMMON_MASTERS_MODULE_NAME).build();
        log.debug("Created activities module detail");
        log.trace("Exiting getActivitiesModuleRequestData method");
        return projectDepartmentModuleDetail;
    }

    private ModuleDetail getStateModuleRequestData() {
        log.trace("Entering getStateModuleRequestData method");
        List<MasterDetail> projectStateInfoMasterDetails = new ArrayList<>();

        MasterDetail departmentMasterDetails = MasterDetail.builder().name(MASTER_STATE_INFO)
                .filter(FILTER_ACTIVE_TRUE).build();
        projectStateInfoMasterDetails.add(departmentMasterDetails);

        ModuleDetail projectDepartmentModuleDetail = ModuleDetail.builder().masterDetails(projectStateInfoMasterDetails)
                .moduleName(MDMS_COMMON_MASTERS_MODULE_NAME).build();
        log.debug("Created state info module detail");
        log.trace("Exiting getStateModuleRequestData method");
        return projectDepartmentModuleDetail;
    }

    public StringBuilder getMdmsSearchUrl() {
        log.trace("Entering getMdmsSearchUrl method");
        StringBuilder url = new StringBuilder().append(config.getMdmsHost()).append(config.getMdmsEndPoint());
        log.debug("MDMS search URL: {}", url);
        log.trace("Exiting getMdmsSearchUrl method");
        return url;
    }

    private ModuleDetail getTenantModuleRequestData() {
        log.trace("Entering getTenantModuleRequestData method");
        List<MasterDetail> tenantMasterDetails = new ArrayList<>();

        MasterDetail tenantMasterDetail = MasterDetail.builder().name(MASTER_TENANTS)
                .filter(FILTER_CODE).build();

        tenantMasterDetails.add(tenantMasterDetail);

        ModuleDetail tenantModuleDetail = ModuleDetail.builder().masterDetails(tenantMasterDetails)
                .moduleName(MDMS_TENANT_MODULE_NAME).build();
        log.debug("Created tenant module detail");
        log.trace("Exiting getTenantModuleRequestData method");
        return tenantModuleDetail;
    }

}