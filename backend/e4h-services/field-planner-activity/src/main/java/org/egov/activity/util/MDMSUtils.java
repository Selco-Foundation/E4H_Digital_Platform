package org.egov.activity.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.http.client.ServiceRequestClient;
import org.egov.activity.web.models.ActivityRequest;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.activity.config.ActivityConfiguration;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.egov.activity.util.ActivityConstants.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class MDMSUtils {

    public static final String FILTER_CODE = "$.*.code";
    public static final String FILTER_NAME = "$.*.name";
    public static final String FILTER_ACTIVE_TRUE = "$.[?(@.active==true)]";
    private final ServiceRequestClient serviceRequestRepository;
    private final ActivityConfiguration config;

    public Object mDMSCall(RequestInfo request, String tenantId) {
        log.trace("mDMSCall method invoked for tenantId: {}", tenantId);
        log.info("Calling MDMS service for tenantId: {}", tenantId);
        RequestInfo requestInfo = request;
        MdmsCriteriaReq mdmsCriteriaReq = getMDMSRequest(requestInfo, tenantId);
        Object result = null;
        try {
            log.debug("Fetching MDMS data from URL: {}", getMdmsSearchUrl());
            result = serviceRequestRepository.fetchResult(getMdmsSearchUrl(), mdmsCriteriaReq, LinkedHashMap.class);
            log.debug("Successfully retrieved MDMS data for tenantId: {}", tenantId);
        } catch (Exception e) {
            log.error("Error while calling MDMS service for tenantId: {}", tenantId, e);
            throw new CustomException("MDMS_ERROR", "error while calling mdms");
        }
        return result;
    }

    public MdmsCriteriaReq getMDMSRequest(RequestInfo requestInfo, String tenantId) {

        ModuleDetail activitiesMDMSModuleDetail = getActivitiesModuleRequestData();
        ModuleDetail stateInfoModuleDetail = getStateModuleRequestData();
        ModuleDetail tenantModuleDetail = getTenantModuleRequestData();
        ModuleDetail bOMModuleDetail = getBOMModuleRequestData();

        List<ModuleDetail> moduleDetails = new LinkedList<>();
        moduleDetails.add(activitiesMDMSModuleDetail);
        moduleDetails.add(stateInfoModuleDetail);
        moduleDetails.add(tenantModuleDetail);
        moduleDetails.add(bOMModuleDetail);

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

    /**
     * Fetches the two USER_ANALYTICS masters the analytics flow needs — FIELD_PLANNER for the
     * action -> event_type mapping and USER_TYPE for the role fields — in a single MDMS call, keyed
     * by master name. Returns an empty map when the call fails: the analytics caller is best-effort
     * and must never break the workflow transition it hangs off, so this does not throw the way
     * {@link #mDMSCall} does.
     * <p>
     * The tenant is reduced to its state prefix, as MDMS masters are state-level.
     */
    @SuppressWarnings("unchecked")
    public Map<String, List<Map<String, Object>>> fetchUserAnalyticsMasters(RequestInfo requestInfo, String tenantId) {
        try {
            List<MasterDetail> masterDetails = new ArrayList<>();
            masterDetails.add(MasterDetail.builder().name(MDMS_MASTER_FIELD_PLANNER).build());
            masterDetails.add(MasterDetail.builder().name(MDMS_MASTER_USER_TYPE).build());

            ModuleDetail moduleDetail = ModuleDetail.builder().masterDetails(masterDetails)
                    .moduleName(USER_ANALYTICS_MODULE).build();
            MdmsCriteria mdmsCriteria = MdmsCriteria.builder()
                    .tenantId(tenantId.split("\\.")[0])
                    .moduleDetails(Collections.singletonList(moduleDetail))
                    .build();
            MdmsCriteriaReq mdmsCriteriaReq = MdmsCriteriaReq.builder()
                    .mdmsCriteria(mdmsCriteria)
                    .requestInfo(requestInfo)
                    .build();

            Map<String, Object> response = serviceRequestRepository.fetchResult(
                    getMdmsSearchUrl(), mdmsCriteriaReq, LinkedHashMap.class);
            Object mdmsRes = (response == null) ? null : response.get(MDMS_RESPONSE);
            if (!(mdmsRes instanceof Map)) {
                return Collections.emptyMap();
            }
            Object module = ((Map<String, Object>) mdmsRes).get(USER_ANALYTICS_MODULE);
            if (!(module instanceof Map)) {
                return Collections.emptyMap();
            }

            Map<String, List<Map<String, Object>>> masters = new LinkedHashMap<>();
            for (String masterName : List.of(MDMS_MASTER_FIELD_PLANNER, MDMS_MASTER_USER_TYPE)) {
                masters.put(masterName, asRecordList(((Map<String, Object>) module).get(masterName)));
            }
            return masters;
        } catch (Exception e) {
            log.warn("Failed to fetch MDMS masters {}.{}/{} for tenant {}: {}", USER_ANALYTICS_MODULE,
                    MDMS_MASTER_FIELD_PLANNER, MDMS_MASTER_USER_TYPE, tenantId, e.getMessage());
            return Collections.emptyMap();
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> asRecordList(Object master) {
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
    }

    public Map<String, String> fetchInstallationImageDescriptions(RequestInfo requestInfo, String tenantId) {
        log.trace("fetchInstallationImageDescriptions method invoked for tenantId: {}", tenantId);
        log.info("Fetching InstallationImages MDMS master for tenantId: {}", tenantId);

        MdmsCriteriaReq mdmsCriteriaReq = MdmsCriteriaReq.builder()
                .requestInfo(requestInfo)
                .mdmsCriteria(MdmsCriteria.builder()
                        .tenantId(tenantId)
                        .moduleDetails(Collections.singletonList(getInstallationImageModuleRequestData()))
                        .build())
                .build();

        Map<String, String> codeToDescription = new LinkedHashMap<>();
        try {
            Map response = serviceRequestRepository.fetchResult(getMdmsSearchUrl(), mdmsCriteriaReq, Map.class);
            Map<String, Object> mdmsRes = (Map<String, Object>) response.get("MdmsRes");
            Map<String, Object> commonMasters = mdmsRes != null
                    ? (Map<String, Object>) mdmsRes.get(MDMS_COMMON_MASTERS_MODULE_NAME)
                    : null;
            List<Map<String, Object>> installationImageMasters = commonMasters != null
                    ? (List<Map<String, Object>>) commonMasters.get(MASTER_INSTALLATION_IMAGES)
                    : null;
            if (installationImageMasters != null && !installationImageMasters.isEmpty()) {
                List<Map<String, Object>> installationImages =
                        (List<Map<String, Object>>) installationImageMasters.get(0).get(INSTALLATION_IMAGE_FIELD);
                if (installationImages != null) {
                    for (Map<String, Object> image : installationImages) {
                        Object code = image.get("code");
                        Object description = image.get("description");
                        if (code != null && description != null) {
                            codeToDescription.put(String.valueOf(code), String.valueOf(description));
                        }
                    }
                }
            }
            log.debug("Fetched {} InstallationImages descriptions for tenantId: {}", codeToDescription.size(), tenantId);
        } catch (Exception e) {
            log.error("Error while fetching InstallationImages MDMS master for tenantId: {}", tenantId, e);
            throw new CustomException("MDMS_ERROR", "error while calling mdms for InstallationImages master");
        }
        return codeToDescription;
    }

    private ModuleDetail getInstallationImageModuleRequestData() {
        MasterDetail installationImageMasterDetail = MasterDetail.builder().name(MASTER_INSTALLATION_IMAGES).build();
        return ModuleDetail.builder()
                .masterDetails(Collections.singletonList(installationImageMasterDetail))
                .moduleName(MDMS_COMMON_MASTERS_MODULE_NAME).build();
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

    private ModuleDetail getBOMModuleRequestData() {
        List<MasterDetail> tenantMasterDetails = new ArrayList<>();

        MasterDetail tenantMasterDetail = MasterDetail.builder().name(BOM_FORM)
                .filter(FILTER_NAME).build();

        tenantMasterDetails.add(tenantMasterDetail);

        ModuleDetail tenantModuleDetail = ModuleDetail.builder().masterDetails(tenantMasterDetails)
                .moduleName(MDMS_COMMON_MASTERS_MODULE_NAME).build();

        return tenantModuleDetail;
    }

}