package org.egov.wf.service;

import com.jayway.jsonpath.JsonPath;
import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.wf.config.WorkflowConfig;
import org.egov.wf.repository.ServiceRequestRepository;
import org.egov.wf.util.WorkflowConstants;
import org.egov.wf.web.models.ProcessInstanceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.*;
import lombok.extern.slf4j.Slf4j;

import static org.egov.wf.util.WorkflowConstants.*;

@Service
@Slf4j
public class MDMSService {

   private WorkflowConfig config;

   private ServiceRequestRepository serviceRequestRepository;

   private WorkflowConfig workflowConfig;

   private Map<String,Boolean> stateLevelMapping;

    @Autowired
    public MDMSService(WorkflowConfig config, ServiceRequestRepository serviceRequestRepository, WorkflowConfig workflowConfig) {
        this.config = config;
        this.serviceRequestRepository = serviceRequestRepository;
        this.workflowConfig = workflowConfig;
    }


    public Map<String, Boolean> getStateLevelMapping() {
        return this.stateLevelMapping;
    }


    @Bean
    public void stateLevelMapping(){
        log.trace("Entering stateLevelMapping bean method");
        log.info("Initializing state level mapping from MDMS");
        Map<String, Boolean> stateLevelMapping = new HashMap<>();

        Object mdmsData = getBusinessServiceMDMS();
        List<HashMap<String, Object>> configs = JsonPath.read(mdmsData,JSONPATH_BUSINESSSERVICE_STATELEVEL);
        log.debug("Retrieved {} business service state level configuration(s)", configs != null ? configs.size() : 0);

        for (Map map : configs){

            String businessService = (String) map.get("businessService");
            Boolean isStatelevel = Boolean.valueOf((String) map.get("isStatelevel"));

            stateLevelMapping.put(businessService, isStatelevel);
        }

        this.stateLevelMapping = stateLevelMapping;
        log.info("State level mapping initialized with {} business service(s)", stateLevelMapping.size());
        log.trace("Exiting stateLevelMapping bean method");
    }


    /**
     * Calls MDMS service to fetch master data
     * @param requestInfo
     * @return
     */
    public Object mDMSCall(RequestInfo requestInfo) {
        log.trace("Entering mDMSCall method");
        String tenantId = (requestInfo != null && requestInfo.getUserInfo() != null)
                ? requestInfo.getUserInfo().getTenantId()
                : workflowConfig.getStateLevelTenantId();
        log.debug("Fetching MDMS data for tenantId: {}", tenantId);
        MdmsCriteriaReq mdmsCriteriaReq = getMDMSRequest(requestInfo, tenantId);
        Object result = serviceRequestRepository.fetchResult(getMdmsSearchUrl(), mdmsCriteriaReq);
        log.debug("Successfully retrieved MDMS data");
        log.trace("Exiting mDMSCall method");
        return result;
    }

    /**
     * Calls MDMS service to fetch master data
     * @return
     */
    public Object getBusinessServiceMDMS(){
        log.trace("Entering getBusinessServiceMDMS method");
        String tenantId = workflowConfig.getStateLevelTenantId();
        log.debug("Fetching business service MDMS data for state level tenantId: {}", tenantId);
        MdmsCriteriaReq mdmsCriteriaReq = getBusinessServiceMDMSRequest(new RequestInfo(), tenantId);
        Object result = serviceRequestRepository.fetchResult(getMdmsSearchUrl(), mdmsCriteriaReq);
        log.trace("Exiting getBusinessServiceMDMS method");
        return result;
    }


    /**
     * Creates MDMSCriteria
     * @param requestInfo The RequestInfo of the request
     * @param tenantId TenantId of the request
     * @return MDMSCriteria for search call
     */
    private MdmsCriteriaReq getMDMSRequest(RequestInfo requestInfo, String tenantId){
        ModuleDetail escalationDetail = getAutoEscalationConfig();
        ModuleDetail tenantDetail = getTenants();

        List<ModuleDetail> moduleDetails = new LinkedList<>(Arrays.asList(escalationDetail,tenantDetail));

        MdmsCriteria mdmsCriteria = MdmsCriteria.builder().moduleDetails(moduleDetails)
                .tenantId(tenantId)
                .build();

        MdmsCriteriaReq mdmsCriteriaReq = MdmsCriteriaReq.builder().mdmsCriteria(mdmsCriteria)
                .requestInfo(requestInfo).build();
        return mdmsCriteriaReq;
    }

    /**
     * Creates MDMSCriteria
     * @param requestInfo The RequestInfo of the request
     * @param tenantId TenantId of the request
     * @return MDMSCriteria for search call
     */
    private MdmsCriteriaReq getBusinessServiceMDMSRequest(RequestInfo requestInfo, String tenantId){
        ModuleDetail wfMasterDetails = getBusinessServiceMasterConfig();


        MdmsCriteria mdmsCriteria = MdmsCriteria.builder().moduleDetails(Collections.singletonList(wfMasterDetails))
                .tenantId(tenantId)
                .build();

        MdmsCriteriaReq mdmsCriteriaReq = MdmsCriteriaReq.builder().mdmsCriteria(mdmsCriteria)
                .requestInfo(requestInfo).build();
        return mdmsCriteriaReq;
    }


    /**
     * Fetches BusinessServiceMasterConfig from MDMS
     * @return ModuleDetail for workflow
     */
    private ModuleDetail getBusinessServiceMasterConfig() {

        // master details for WF module
        List<MasterDetail> wfMasterDetails = new ArrayList<>();

        wfMasterDetails.add(MasterDetail.builder().name(MDMS_BUSINESSSERVICE).build());

        ModuleDetail wfModuleDtls = ModuleDetail.builder().masterDetails(wfMasterDetails)
                .moduleName(MDMS_WORKFLOW).build();

        return wfModuleDtls;
    }

    /**
     * Creates MDMS ModuleDetail object for AutoEscalation
     * @return ModuleDetail for AutoEscalation
     */
    private ModuleDetail getAutoEscalationConfig() {

        // master details for WF module
        List<MasterDetail> masterDetails = new ArrayList<>();

        masterDetails.add(MasterDetail.builder().name(MDMS_AUTOESCALTION).build());

        ModuleDetail wfModuleDtls = ModuleDetail.builder().masterDetails(masterDetails)
                .moduleName(MDMS_WORKFLOW).build();

        return wfModuleDtls;
    }

    /**
     * Creates MDMS ModuleDetail object for tenants
     * @return ModuleDetail for tenants
     */
    private ModuleDetail getTenants() {

        // master details for WF module
        List<MasterDetail> masterDetails = new ArrayList<>();

        masterDetails.add(MasterDetail.builder().name(MDMS_TENANTS).build());

        ModuleDetail wfModuleDtls = ModuleDetail.builder().masterDetails(masterDetails)
                .moduleName(MDMS_MODULE_TENANT).build();

        return wfModuleDtls;
    }





    /**
     * Returns the url for mdms search endpoint
     * @return url for mdms search endpoint
     */
    public StringBuilder getMdmsSearchUrl() {
        return new StringBuilder().append(config.getMdmsHost()).append(config.getMdmsEndPoint());
    }
    
    public Integer fetchSlotPercentageForNearingSla(RequestInfo requestInfo) {
        log.trace("Entering fetchSlotPercentageForNearingSla method");
        // master details for WF SLA module
        List<MasterDetail> masterDetails = new ArrayList<>();

        masterDetails.add(MasterDetail.builder().name(MDMS_WF_SLA_CONFIG).build());

        List<ModuleDetail> wfModuleDtls = Collections.singletonList(ModuleDetail.builder().masterDetails(masterDetails)
                .moduleName(MDMS_COMMON_MASTERS).build());

        MdmsCriteria mdmsCriteria = MdmsCriteria.builder().moduleDetails(wfModuleDtls)
                .tenantId(config.getStateLevelTenantId())
                .build();

        MdmsCriteriaReq mdmsCriteriaReq = MdmsCriteriaReq.builder().mdmsCriteria(mdmsCriteria)
                .requestInfo(requestInfo).build();

        log.debug("Fetching slot percentage for nearing SLA from MDMS");
        Object result = serviceRequestRepository.fetchResult(getMdmsSearchUrl(), mdmsCriteriaReq);
        Integer slotPercentage = JsonPath.read(result, SLOT_PERCENTAGE_PATH);
        log.debug("Retrieved slot percentage for nearing SLA: {}", slotPercentage);
        log.trace("Exiting fetchSlotPercentageForNearingSla method");
        return slotPercentage;

    }







}
