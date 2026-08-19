package digit.util;

import digit.config.ApplicationProperties;
import digit.service.ServiceRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static digit.constants.BoundaryConstants.MASTER_STATE_INFO;
import static digit.constants.BoundaryConstants.MDMS_COMMON_MASTERS_MODULE_NAME;

@Component
@Slf4j
@RequiredArgsConstructor
public class MDMSUtils {

    public static final String FILTER_CODE = "$.*.code";
    public static final String FILTER_ACTIVE_TRUE = "$.[?(@.active==true)]";
    private final ServiceRequestRepository serviceRequestRepository;
    private final ApplicationProperties config;

    public Object mDMSCall(RequestInfo requestInfo, String tenantId) {
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

            Map<String, Object> response = serviceRequestRepository.fetchResult(
                    getMdmsSearchUrl(), mdmsCriteriaReq, LinkedHashMap.class);
            Object mdmsRes = (response == null) ? null : response.get("MdmsRes");
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

    public MdmsCriteriaReq getMDMSRequest(RequestInfo requestInfo, String tenantId) {

        ModuleDetail stateInfoModuleDetail = getStateModuleRequestData();
        List<ModuleDetail> moduleDetails = new LinkedList<>();
        moduleDetails.add(stateInfoModuleDetail);

        MdmsCriteria mdmsCriteria = MdmsCriteria.builder().moduleDetails(moduleDetails).tenantId(tenantId)
                .build();
        MdmsCriteriaReq mdmsCriteriaReq = MdmsCriteriaReq.builder().mdmsCriteria(mdmsCriteria)
                .requestInfo(requestInfo).build();
        return mdmsCriteriaReq;
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

}