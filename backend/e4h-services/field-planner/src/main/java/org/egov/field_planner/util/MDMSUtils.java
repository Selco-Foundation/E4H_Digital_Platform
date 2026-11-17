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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
        RequestInfo requestInfo = request.getRequestInfo();
        MdmsCriteriaReq mdmsCriteriaReq = getMDMSRequest(requestInfo, tenantId);
        Object result = null;
        try {
            result = serviceRequestRepository.fetchResult(getMdmsSearchUrl(), mdmsCriteriaReq, LinkedHashMap.class);
        } catch (Exception e) {
            log.error("error while calling mdms", ExceptionUtils.getStackTrace(e));
            throw new CustomException("MDMS_ERROR", "error while calling mdms");
        }
        return result;
    }

    public MdmsCriteriaReq getMDMSRequest(RequestInfo requestInfo, String tenantId) {

        ModuleDetail activitiesMDMSModuleDetail = getActivitiesModuleRequestData();
        ModuleDetail stateInfoModuleDetail = getStateModuleRequestData();
        ModuleDetail tenantModuleDetail = getTenantModuleRequestData();

        List<ModuleDetail> moduleDetails = new LinkedList<>();
        moduleDetails.add(activitiesMDMSModuleDetail);
        moduleDetails.add(stateInfoModuleDetail);
        moduleDetails.add(tenantModuleDetail);

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

    /**
     * Fetches one MDMS master as a plain record list, for callers that only need the rows and must
     * not blow up when MDMS is unreachable — {@code FieldPlannerAnalyticsService} reads
     * USER_ANALYTICS.USER_TYPE through this. Unlike {@link #mDMSCall} it returns an empty list
     * instead of throwing, and it does not go through JsonPath.
     * <p>
     * The tenant is reduced to its state prefix, as MDMS masters are state-level.
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> fetchMasterData(RequestInfo requestInfo, String tenantId,
                                                     String moduleName, String masterName) {
        try {
            MasterDetail masterDetail = MasterDetail.builder().name(masterName).build();
            ModuleDetail moduleDetail = ModuleDetail.builder()
                    .moduleName(moduleName)
                    .masterDetails(Collections.singletonList(masterDetail))
                    .build();
            MdmsCriteria mdmsCriteria = MdmsCriteria.builder()
                    .tenantId(tenantId.split("\\.")[0])
                    .moduleDetails(Collections.singletonList(moduleDetail))
                    .build();
            MdmsCriteriaReq mdmsCriteriaReq = MdmsCriteriaReq.builder()
                    .mdmsCriteria(mdmsCriteria)
                    .requestInfo(requestInfo)
                    .build();

            Object response = serviceRequestRepository.fetchResult(
                    getMdmsSearchUrl(), mdmsCriteriaReq, LinkedHashMap.class);
            Object mdmsRes = (response instanceof Map)
                    ? ((Map<String, Object>) response).get("MdmsRes") : null;
            if (!(mdmsRes instanceof Map)) {
                return Collections.emptyList();
            }
            Object module = ((Map<String, Object>) mdmsRes).get(moduleName);
            if (!(module instanceof Map)) {
                return Collections.emptyList();
            }
            Object master = ((Map<String, Object>) module).get(masterName);
            if (!(master instanceof List)) {
                return Collections.emptyList();
            }
            List<Map<String, Object>> records = new ArrayList<>();
            for (Object entry : (List<?>) master) {
                if (entry instanceof Map) {
                    records.add((Map<String, Object>) entry);
                }
            }
            return records;
        } catch (Exception e) {
            log.warn("Failed to fetch MDMS master {}.{} for tenant {}: {}", moduleName, masterName, tenantId,
                    e.getMessage());
            return Collections.emptyList();
        }
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

}