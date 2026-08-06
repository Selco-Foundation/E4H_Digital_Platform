package org.egov.im.util;

import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.im.config.IMConfiguration;
import org.egov.im.repository.ServiceRequestRepository;
import org.egov.im.web.models.IncidentRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.egov.im.util.IMConstants.MDMS_MASTER_SEM;
import static org.egov.im.util.IMConstants.MDMS_MASTER_USER_TYPE;
import static org.egov.im.util.IMConstants.MDMS_MODULE_NAME;
import static org.egov.im.util.IMConstants.MDMS_SERVICEDEF;
import static org.egov.im.util.IMConstants.USER_ANALYTICS_MODULE;

@Component
public class MDMSUtils {



    private IMConfiguration config;

    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    public MDMSUtils(IMConfiguration config, ServiceRequestRepository serviceRequestRepository) {
        this.config = config;
        this.serviceRequestRepository = serviceRequestRepository;
    }

    /**
     * Calls MDMS service to fetch im master data
     * @param request
     * @return
     */
    public Object mDMSCall(IncidentRequest request){
        RequestInfo requestInfo = request.getRequestInfo();
        String tenantId = request.getIncident().getTenantId();
        MdmsCriteriaReq mdmsCriteriaReq = getMDMSRequest(requestInfo,tenantId);
        Object result = serviceRequestRepository.fetchResult(getMdmsSearchUrl(), mdmsCriteriaReq);
        return result;
    }


    /**
     * Calls MDMS service to fetch the USER_ANALYTICS master data (SEM and USER_TYPE)
     * used to build SEM analytics events.
     * @param requestInfo
     * @param tenantId
     * @return raw MDMS response
     */
    public Object getUserAnalyticsMDMSData(RequestInfo requestInfo, String tenantId) {
        log.trace("MDMSUtils::getUserAnalyticsMDMSData method invoked");
        MdmsCriteriaReq mdmsCriteriaReq = getUserAnalyticsMDMSRequest(requestInfo, tenantId);
        return serviceRequestRepository.fetchResult(getMdmsSearchUrl(), mdmsCriteriaReq);
    }

    /**
     * Builds the MDMS search criteria for the USER_ANALYTICS module (SEM and USER_TYPE masters).
     * @param requestInfo
     * @param tenantId
     * @return
     */
    public MdmsCriteriaReq getUserAnalyticsMDMSRequest(RequestInfo requestInfo, String tenantId) {
        log.trace("MDMSUtils::getUserAnalyticsMDMSRequest method invoked");
        List<MasterDetail> masterDetails = new ArrayList<>();
        masterDetails.add(MasterDetail.builder().name(MDMS_MASTER_SEM).build());
        masterDetails.add(MasterDetail.builder().name(MDMS_MASTER_USER_TYPE).build());

        ModuleDetail moduleDetail = ModuleDetail.builder().masterDetails(masterDetails)
                .moduleName(USER_ANALYTICS_MODULE).build();

        MdmsCriteria mdmsCriteria = MdmsCriteria.builder()
                .moduleDetails(Collections.singletonList(moduleDetail)).tenantId(tenantId)
                .build();

        return MdmsCriteriaReq.builder().mdmsCriteria(mdmsCriteria)
                .requestInfo(requestInfo).build();
    }

    /**
     * Returns mdms search criteria based on the tenantId
     * @param requestInfo
     * @param tenantId
     * @return
     */
    public MdmsCriteriaReq getMDMSRequest(RequestInfo requestInfo,String tenantId){
        List<ModuleDetail> pgrModuleRequest = getIMModuleRequest();

        List<ModuleDetail> moduleDetails = new LinkedList<>();
        moduleDetails.addAll(pgrModuleRequest);

        MdmsCriteria mdmsCriteria = MdmsCriteria.builder().moduleDetails(moduleDetails).tenantId(tenantId)
                .build();

        MdmsCriteriaReq mdmsCriteriaReq = MdmsCriteriaReq.builder().mdmsCriteria(mdmsCriteria)
                .requestInfo(requestInfo).build();
        return mdmsCriteriaReq;
    }


    /**
     * Creates request to search serviceDef from MDMS
     * @return request to search UOM from MDMS
     */
    private List<ModuleDetail> getIMModuleRequest() {

        // master details for TL module
        List<MasterDetail> pgrMasterDetails = new ArrayList<>();

        // filter to only get code field from master data
        final String filterCode = "$.[?(@.active==true)]";

        pgrMasterDetails.add(MasterDetail.builder().name(MDMS_SERVICEDEF).filter(filterCode).build());

        ModuleDetail pgrModuleDtls = ModuleDetail.builder().masterDetails(pgrMasterDetails)
                .moduleName(MDMS_MODULE_NAME).build();


        return Collections.singletonList(pgrModuleDtls);

    }


    /**
     * Returns the url for mdms search endpoint
     *
     * @return url for mdms search endpoint
     */
    public StringBuilder getMdmsSearchUrl() {
        return new StringBuilder().append(config.getMdmsHost()).append(config.getMdmsEndPoint());
    }

    public Object fetchMDMSData(RequestInfo requestInfo, String tenantId, String moduleName, List<String> masterNames, String filter) {
        List<MasterDetail> masterDetails = masterNames.stream()
                .map(name -> {
                    MasterDetail.MasterDetailBuilder builder = MasterDetail.builder().name(name);
                    if (filter != null && !filter.isEmpty()) builder.filter(filter);
                    return builder.build();
                })
                .toList();

        ModuleDetail moduleDetail = ModuleDetail.builder()
                .moduleName(moduleName)
                .masterDetails(masterDetails)
                .build();

        MdmsCriteria mdmsCriteria = MdmsCriteria.builder()
                .tenantId(tenantId.split("\\.")[0]) // ensures 'pg' instead of 'pg.dummy'
                .moduleDetails(Collections.singletonList(moduleDetail))
                .build();

        MdmsCriteriaReq mdmsRequest = MdmsCriteriaReq.builder()
                .requestInfo(requestInfo)
                .mdmsCriteria(mdmsCriteria)
                .build();

        return serviceRequestRepository.fetchResult(getMdmsSearchUrl(), mdmsRequest);
    }



}
