package org.egov.amc.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.amc.config.AMCServiceConfiguration;
import org.egov.amc.web.models.AmcConfigurationRequest;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.http.client.ServiceRequestClient;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.tracer.model.CustomException;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import static org.egov.amc.util.AmcConstants.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class MDMSUtils {

    public static final String FILTER_CODE = "$.*.code";
    public static final String FILTER_ACTIVE_TRUE = "$.[?(@.active==true)]";
    public static final String FILTER_STATUS_ACTIVE = "$.[?(@.status=='active')]";
    private final ServiceRequestClient serviceRequestRepository;
    private final AMCServiceConfiguration config;

    public Object mDMSCall(AmcConfigurationRequest request, String tenantId) {
        RequestInfo requestInfo = request.getRequestInfo();
        MdmsCriteriaReq mdmsCriteriaReq = getMDMSRequest(requestInfo, tenantId);
        Object result = null;
        try {
            result = serviceRequestRepository.fetchResult(getMdmsSearchUrl(), mdmsCriteriaReq, LinkedHashMap.class);
        } catch (ServiceCallException e) {
            log.error("Error while calling MDMS service", e);
            throw new CustomException("MDMS_ERROR", "Error while calling MDMS: " + e.getMessage());
        }
        return result;
    }

    public MdmsCriteriaReq getMDMSRequest(RequestInfo requestInfo, String tenantId) {

        ModuleDetail activitiesMDMSModuleDetail = getActivitiesModuleRequestData();
        ModuleDetail stateInfoModuleDetail = getStateModuleRequestData();
        ModuleDetail tenantModuleDetail = getTenantModuleRequestData();
        ModuleDetail amcThresholdDetail = getAMCThresholdsModuleRequestData();

        List<ModuleDetail> moduleDetails = new LinkedList<>();
        moduleDetails.add(activitiesMDMSModuleDetail);
        moduleDetails.add(stateInfoModuleDetail);
        moduleDetails.add(tenantModuleDetail);
        moduleDetails.add(amcThresholdDetail);

        MdmsCriteria mdmsCriteria = MdmsCriteria.builder().moduleDetails(moduleDetails).tenantId(tenantId)
                .build();

        MdmsCriteriaReq mdmsCriteriaReq = MdmsCriteriaReq.builder().mdmsCriteria(mdmsCriteria)
                .requestInfo(requestInfo).build();
        return mdmsCriteriaReq;
    }

    private ModuleDetail getActivitiesModuleRequestData() {
        List<MasterDetail> projectActivitiesMasterDetails = new ArrayList<>();

        MasterDetail departmentMasterDetails = MasterDetail.builder().name(MASTER_ACTIVITIES)
                .filter(FILTER_ACTIVE_TRUE).build();
        projectActivitiesMasterDetails.add(departmentMasterDetails);

        ModuleDetail projectDepartmentModuleDetail = ModuleDetail.builder().masterDetails(projectActivitiesMasterDetails)
                .moduleName(MDMS_COMMON_MASTERS_MODULE_NAME).build();

        return projectDepartmentModuleDetail;
    }

    private ModuleDetail getStateModuleRequestData() {
        List<MasterDetail> projectStateInfoMasterDetails = new ArrayList<>();

        MasterDetail departmentMasterDetails = MasterDetail.builder().name(MASTER_STATE_INFO)
                .filter(FILTER_ACTIVE_TRUE).build();
        projectStateInfoMasterDetails.add(departmentMasterDetails);

        ModuleDetail projectDepartmentModuleDetail = ModuleDetail.builder().masterDetails(projectStateInfoMasterDetails)
                .moduleName(MDMS_COMMON_MASTERS_MODULE_NAME).build();

        return projectDepartmentModuleDetail;
    }

    public StringBuilder getMdmsSearchUrl() {
        return new StringBuilder().append(config.getMdmsHost()).append(config.getMdmsEndPoint());
    }

    private ModuleDetail getTenantModuleRequestData() {
        List<MasterDetail> tenantMasterDetails = new ArrayList<>();

        MasterDetail tenantMasterDetail = MasterDetail.builder().name(MASTER_TENANTS)
                .filter(FILTER_CODE).build();

        tenantMasterDetails.add(tenantMasterDetail);

        ModuleDetail tenantModuleDetail = ModuleDetail.builder().masterDetails(tenantMasterDetails)
                .moduleName(MDMS_TENANT_MODULE_NAME).build();

        return tenantModuleDetail;
    }

    private ModuleDetail getAMCThresholdsModuleRequestData() {
        List<MasterDetail> amcThresholdsMasterDetails = new ArrayList<>();

        MasterDetail amcThresholdsMasterDetail = MasterDetail.builder()
                .name(MDMS_AMC_THRESHOLD_MODULE_NAME)
                .filter(FILTER_STATUS_ACTIVE)
                .build();
        amcThresholdsMasterDetails.add(amcThresholdsMasterDetail);

        ModuleDetail amcThresholdsModuleDetail = ModuleDetail.builder()
                .masterDetails(amcThresholdsMasterDetails)
                .moduleName(MDMS_AMC_MODULE_NAME)
                .build();

        return amcThresholdsModuleDetail;
    }


}