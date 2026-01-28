package org.egov.util;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.config.Configuration;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.repository.ServiceRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.egov.util.OrganisationConstant.*;

@Slf4j
@Component
public class MDMSUtil {

    public static final String CODE_FILTER = "$.*.code";
    public static final String ACTIVE_CODE_FILTER = "$.[?(@.active==true)].code";
    public static final String ACTIVE_TYPE_FILTER = "$.[?(@.active==true)].type";

    private final Configuration config;

    private final ServiceRequestRepository serviceRequestRepository;

    @Autowired
    public MDMSUtil(Configuration config, ServiceRequestRepository serviceRequestRepository) {
        this.config = config;
        this.serviceRequestRepository = serviceRequestRepository;
    }

    /**
     * Calls MDMS service to fetch works master data
     *
     * @param requestInfo
     * @param tenantId
     * @return
     */
    public Object mDMSCall(RequestInfo requestInfo, String tenantId) {
        log.trace("MDMSUtil::mDMSCall entry");
        log.info("Calling MDMS service for tenant: {}", tenantId);
        MdmsCriteriaReq mdmsCriteriaReq = prepareMDMSRequest(requestInfo, tenantId);
        Object result = serviceRequestRepository.fetchResult(getMdmsSearchUrl(), mdmsCriteriaReq);
        log.debug("MDMS call completed successfully");
        return result;
    }

    /**
     * Prepares the mdms search request
     * @param requestInfo
     * @param tenantId
     * @return the mdms search request
     */
    public MdmsCriteriaReq prepareMDMSRequest(RequestInfo requestInfo, String tenantId) {
        log.trace("MDMSUtil::prepareMDMSRequest entry");
        ModuleDetail commonMasterModuleDetails = prepareCommonMasterModuleDetails();
        ModuleDetail organizationModuleDetails = prepareOrganizationModuleDetails();
        ModuleDetail tenantModuleDetail = getTenantModuleRequestData();

        List<ModuleDetail> moduleDetails = new LinkedList<>();
        moduleDetails.add(commonMasterModuleDetails);
        moduleDetails.add(organizationModuleDetails);
        moduleDetails.add(tenantModuleDetail);
        log.debug("Prepared MDMS request with {} module details", moduleDetails.size());

        MdmsCriteria mdmsCriteria = MdmsCriteria.builder().moduleDetails(moduleDetails).tenantId(tenantId)
                .build();
        return MdmsCriteriaReq.builder().mdmsCriteria(mdmsCriteria)
                .requestInfo(requestInfo).build();
    }

    /**
     * Prepares the mdms search request for common master module
     *
     * @return the mdms search request for common master module
     */
    private ModuleDetail prepareCommonMasterModuleDetails(){
        log.trace("MDMSUtil::prepareCommonMasterModuleDetails entry");
        List<MasterDetail> commonMasterModulesDetails = new ArrayList<>();
        MasterDetail orgFuncMaster = MasterDetail.builder().name(MASTER_ORG_FUNC_CLASS)
                .filter(ACTIVE_CODE_FILTER).build();
        MasterDetail orgTaxIdentifierMaster = MasterDetail.builder().name(MASTER_ORG_TAX_IDENTIFIER)
                .filter(ACTIVE_CODE_FILTER).build();
//        MasterDetail orgFunCategoryMaster = MasterDetail.builder().name(MASTER_ORG_FUNC_CATEGORY)
//                .filter(ACTIVE_CODE_FILTER).build();
        MasterDetail orgTypeMaster = MasterDetail.builder().name(MASTER_ORG_TYPE)
                .filter(ACTIVE_CODE_FILTER).build();
//        commonMasterModulesDetails.add(orgFuncMaster);
        commonMasterModulesDetails.add(orgTaxIdentifierMaster);
//        commonMasterModulesDetails.add(orgFunCategoryMaster);
//        commonMasterModulesDetails.add(orgTypeMaster);
        return ModuleDetail.builder().masterDetails(commonMasterModulesDetails)
                .moduleName(MDMS_COMMON_MASTERS_MODULE_NAME).build();
    }

    /**
     * Prepares the mdms search request for organization module
     *
     * @return the mdms search request for organization module
     */
    private ModuleDetail prepareOrganizationModuleDetails(){
        log.trace("MDMSUtil::prepareOrganizationModuleDetails entry");
        List<MasterDetail> organizationModulesDetails = new ArrayList<>();
        MasterDetail orgTypeMaster = MasterDetail.builder().name(MASTER_ORG_TYPE).filter(CODE_FILTER).build();
        MasterDetail orgSubTypeMaster = MasterDetail.builder().name(MASTER_ORG_SUB_TYPE).filter(CODE_FILTER).build();
        MasterDetail orgStatusMaster = MasterDetail.builder().name(MASTER_ORG_STATUS).filter(CODE_FILTER).build();
        MasterDetail orgRolesMaster = MasterDetail.builder().name(MASTER_ORG_ROLES).build();
        organizationModulesDetails.add(orgTypeMaster);
        organizationModulesDetails.add(orgSubTypeMaster);
        organizationModulesDetails.add(orgStatusMaster);
        organizationModulesDetails.add(orgRolesMaster);
        return ModuleDetail.builder().masterDetails(organizationModulesDetails)
                .moduleName(MDMS_ORGANIZATION_MODULE_NAME).build();
    }

    /**
     * Prepares the mdms search request for tenant module
     *
     * @return the mdms search request for tenant module
     */
    private ModuleDetail getTenantModuleRequestData() {
        log.trace("MDMSUtil::getTenantModuleRequestData entry");
        List<MasterDetail> orgTenantMasterDetails = new ArrayList<>();

        MasterDetail tenantMasterDetails = MasterDetail.builder().name(MASTER_TENANTS)
                .filter(CODE_FILTER).build();

        orgTenantMasterDetails.add(tenantMasterDetails);

        return ModuleDetail.builder().masterDetails(orgTenantMasterDetails)
                .moduleName(MDMS_TENANT_MODULE_NAME).build();
    }

    /**
     * Returns the url for mdms search endpoint
     *
     * @return url for mdms search endpoint
     */
    public StringBuilder getMdmsSearchUrl() {
        return new StringBuilder().append(config.getMdmsHost()).append(config.getMdmsEndPoint());
    }

}