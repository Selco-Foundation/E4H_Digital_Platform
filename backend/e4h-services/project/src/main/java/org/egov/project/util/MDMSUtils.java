package org.egov.project.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.http.client.ServiceRequestClient;
import org.egov.common.models.project.Project;
import org.egov.common.models.project.ProjectRequest;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.project.config.ProjectConfiguration;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.egov.project.util.ProjectConstants.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class MDMSUtils {

    public static final String FILTER_CODE = "$.*.code";
    public static final String FILTER_ACTIVE_TRUE = "$.[?(@.active==true)]";
    private final ServiceRequestClient serviceRequestRepository;
    private final ProjectConfiguration config;

    public Object mDMSCall(ProjectRequest request, String tenantId) {
        RequestInfo requestInfo = request.getRequestInfo();
        MdmsCriteriaReq mdmsCriteriaReq = getMDMSRequest(requestInfo, tenantId, request);
        Object result = null;
        try {
            result = serviceRequestRepository.fetchResult(getMdmsSearchUrl(), mdmsCriteriaReq, LinkedHashMap.class);
        } catch (Exception e) {
            log.error("error while calling mdms", ExceptionUtils.getStackTrace(e));
            throw new CustomException("MDMS_ERROR", "error while calling mdms");
        }
        return result;
    }

    /**
     * Generic MDMS v1 fetch for a single module. Returns the records of {@code masterName} as a
     * list of raw maps, or an empty list when the master is missing or the call fails — callers
     * (analytics) are best-effort and must not break the flow they hang off.
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

    public MdmsCriteriaReq getMDMSRequest(RequestInfo requestInfo, String tenantId, ProjectRequest request) {

        ModuleDetail projectMDMSModuleDetail = getMDMSModuleRequestData(request);
        ModuleDetail projectDepartmentModuleDetail = getDepartmentModuleRequestData(request);
        ModuleDetail projectTypeModuleDetail = getProjectTypeModuleRequestData(request);
        ModuleDetail stateInfoModuleDetail = getStateInfoModuleRequestData(request);
        ModuleDetail projectTenantModuleDetail = getTenantModuleRequestData(request);
        ModuleDetail attendanceModuleDetail = getAttendanceModuleRequestData(request);

        List<ModuleDetail> moduleDetails = new LinkedList<>();
        moduleDetails.add(projectMDMSModuleDetail);
        moduleDetails.add(projectDepartmentModuleDetail);
        moduleDetails.add(projectTypeModuleDetail);
        moduleDetails.add(stateInfoModuleDetail);
        moduleDetails.add(projectTenantModuleDetail);
        moduleDetails.add(attendanceModuleDetail);

        MdmsCriteria mdmsCriteria = MdmsCriteria.builder().moduleDetails(moduleDetails).tenantId(tenantId)
                .build();

        MdmsCriteriaReq mdmsCriteriaReq = MdmsCriteriaReq.builder().mdmsCriteria(mdmsCriteria)
                .requestInfo(requestInfo).build();
        return mdmsCriteriaReq;
    }

    private ModuleDetail getMDMSModuleRequestData(ProjectRequest request) {
        List<MasterDetail> projectMDMSMasterDetails = new ArrayList<>();

        MasterDetail projectTypeMasterDetails = MasterDetail.builder().name(MASTER_PROJECTTYPE)
                .filter(FILTER_ACTIVE_TRUE)
                .build();
        MasterDetail natureOfWorkMasterDetails = MasterDetail.builder().name(MASTER_NATUREOFWORK)
                .filter(FILTER_ACTIVE_TRUE)
                .build();
        projectMDMSMasterDetails.add(projectTypeMasterDetails);
        projectMDMSMasterDetails.add(natureOfWorkMasterDetails);


        ModuleDetail projectMDMSModuleDetail = ModuleDetail.builder().masterDetails(projectMDMSMasterDetails)
                .moduleName(config.getMdmsModule()).build();

        return projectMDMSModuleDetail;
    }

    private ModuleDetail getDepartmentModuleRequestData(ProjectRequest request) {
        List<Project> projects = request.getProjects();
        List<MasterDetail> projectDepartmentMasterDetails = new ArrayList<>();

        MasterDetail departmentMasterDetails = MasterDetail.builder().name(MASTER_DEPARTMENT)
                .filter(FILTER_ACTIVE_TRUE).build();
        projectDepartmentMasterDetails.add(departmentMasterDetails);

        ModuleDetail projectDepartmentModuleDetail = ModuleDetail.builder().masterDetails(projectDepartmentMasterDetails)
                .moduleName(MDMS_COMMON_MASTERS_MODULE_NAME).build();

        return projectDepartmentModuleDetail;
    }

    public StringBuilder getMdmsSearchUrl() {
        return new StringBuilder().append(config.getMdmsHost()).append(config.getMdmsEndPoint());
    }

    private ModuleDetail getTenantModuleRequestData(ProjectRequest request) {
        List<MasterDetail> tenantMasterDetails = new ArrayList<>();

        MasterDetail tenantMasterDetail = MasterDetail.builder().name(MASTER_TENANTS)
                .filter(FILTER_CODE).build();

        tenantMasterDetails.add(tenantMasterDetail);

        ModuleDetail tenantModuleDetail = ModuleDetail.builder().masterDetails(tenantMasterDetails)
                .moduleName(MDMS_TENANT_MODULE_NAME).build();

        return tenantModuleDetail;
    }

    private ModuleDetail getAttendanceModuleRequestData(ProjectRequest request) {
        List<MasterDetail> attendanceMasterDetails = new ArrayList<>();

        MasterDetail attendanceSessionsMasterDetails = MasterDetail.builder().name(MASTER_ATTENDANCE_SESSION)
                .filter(FILTER_CODE)
                .build();

        attendanceMasterDetails.add(attendanceSessionsMasterDetails);

        ModuleDetail attendanceModuleDetail = ModuleDetail.builder().masterDetails(attendanceMasterDetails)
                .moduleName(MDMS_HCM_ATTENDANCE_MODULE_NAME).build();

        return attendanceModuleDetail;
    }

    private ModuleDetail getProjectTypeModuleRequestData(ProjectRequest request) {
        List<Project> projects = request.getProjects();
        List<MasterDetail> projectDepartmentMasterDetails = new ArrayList<>();

        MasterDetail departmentMasterDetails = MasterDetail.builder().name(MASTER_PROJECTTYPE)
                .filter(FILTER_ACTIVE_TRUE).build();
        projectDepartmentMasterDetails.add(departmentMasterDetails);

        ModuleDetail projectDepartmentModuleDetail = ModuleDetail.builder().masterDetails(projectDepartmentMasterDetails)
                .moduleName(MDMS_COMMON_MASTERS_MODULE_NAME).build();

        return projectDepartmentModuleDetail;
    }

    private ModuleDetail getStateInfoModuleRequestData(ProjectRequest request) {
        List<Project> projects = request.getProjects();
        List<MasterDetail> projectDepartmentMasterDetails = new ArrayList<>();

        MasterDetail departmentMasterDetails = MasterDetail.builder().name(MASTER_STATEINFO)
                .filter(FILTER_ACTIVE_TRUE).build();
        projectDepartmentMasterDetails.add(departmentMasterDetails);

        ModuleDetail projectDepartmentModuleDetail = ModuleDetail.builder().masterDetails(projectDepartmentMasterDetails)
                .moduleName(MDMS_COMMON_MASTERS_MODULE_NAME).build();

        return projectDepartmentModuleDetail;
    }

}