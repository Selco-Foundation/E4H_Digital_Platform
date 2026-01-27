package org.egov.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.config.Configuration;
import org.egov.util.IdgenUtil;
import org.egov.util.OrganisationUtil;
import org.egov.web.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class OrganisationEnrichmentService {

    private final OrganisationUtil organisationUtil;

    private final IdgenUtil idgenUtil;

    private final Configuration config;

    @Autowired
    public OrganisationEnrichmentService(OrganisationUtil organisationUtil, IdgenUtil idgenUtil, Configuration config) {
        this.organisationUtil = organisationUtil;
        this.idgenUtil = idgenUtil;
        this.config = config;
    }


    /**
     * Enrich the audit details, id, and custom format number
     *
     * @param orgRequest
     */
    public void enrichCreateOrgRegistryWithoutWorkFlow(OrgRequest orgRequest) {
        log.trace("OrganisationEnrichmentService::enrichCreateOrgRegistryWithoutWorkFlow entry");
        RequestInfo requestInfo = orgRequest.getRequestInfo();
        List<Organisation> organisationList = orgRequest.getOrganisations();
        String tenantId = getTenantId(organisationList);
        log.info("Starting enrichment for organisation creation, tenant: {}, organisation count: {}", 
                tenantId, organisationList != null ? organisationList.size() : 0);

        organisationUtil.setAuditDetailsForOrganisation(requestInfo.getUserInfo().getUuid(), organisationList, Boolean.TRUE);
        log.debug("Audit details set for organisations");

        GeneratedIds generatedIds = generateAllIds(requestInfo, tenantId, organisationList);
        
        enrichOrganisations(organisationList, generatedIds, requestInfo);
        
        log.info("Organisation enrichment completed successfully for tenant: {}", tenantId);
    }

    private String getTenantId(List<Organisation> organisationList) {
        return organisationList != null && !organisationList.isEmpty() 
                ? organisationList.get(0).getTenantId() : "unknown";
    }

    private GeneratedIds generateAllIds(RequestInfo requestInfo, String tenantId, List<Organisation> organisationList) {
        List<String> orgApplicationNumbers = idgenUtil.getIdList(requestInfo, tenantId, 
                config.getOrgApplicationNumberName(), config.getOrgApplicationNumberFormat(), organisationList.size());
        log.debug("Generated {} organisation application numbers", orgApplicationNumbers != null ? orgApplicationNumbers.size() : 0);

        List<String> orgCodes = idgenUtil.getIdList(requestInfo, tenantId, 
                config.getOrgCodeName(), config.getOrgCodeFormat(), organisationList.size());
        log.debug("Generated {} organisation codes", orgCodes != null ? orgCodes.size() : 0);

        long idgenFuncApplicationNumberCount = calculateFunctionApplicationNumberCount(organisationList);
        log.debug("Total function application numbers needed: {}", idgenFuncApplicationNumberCount);

        List<String> orgFunctionApplicationNumbers = idgenUtil.getIdList(requestInfo, tenantId, 
                config.getFunctionApplicationNumberName(), config.getFunctionApplicationNumberFormat(), 
                ((int) idgenFuncApplicationNumberCount));
        log.debug("Generated {} function application numbers", orgFunctionApplicationNumbers != null ? orgFunctionApplicationNumbers.size() : 0);

        return new GeneratedIds(orgApplicationNumbers, orgCodes, orgFunctionApplicationNumbers);
    }

    private long calculateFunctionApplicationNumberCount(List<Organisation> organisationList) {
        return organisationList.stream().mapToInt(org -> {
            if (!CollectionUtils.isEmpty(org.getFunctions())) {
                return org.getFunctions().size();
            }
            return 0;
        }).sum();
    }

    private void enrichOrganisations(List<Organisation> organisationList, GeneratedIds generatedIds, RequestInfo requestInfo) {
        int orgAppNumIdFormatIndex = 0;
        int funcAppNumIdFormatIndex = 0;
        int orgCodeIdFormatIndex = 0;
        
        for (Organisation organisation : organisationList) {
            enrichOrganisationBasicFields(organisation, generatedIds, orgAppNumIdFormatIndex, orgCodeIdFormatIndex);
            funcAppNumIdFormatIndex = enrichOrganisationRelatedEntities(organisation, requestInfo, generatedIds, funcAppNumIdFormatIndex);
            
            orgAppNumIdFormatIndex++;
            orgCodeIdFormatIndex++;
        }
    }

    private void enrichOrganisationBasicFields(Organisation organisation, GeneratedIds generatedIds, 
                                               int orgAppNumIndex, int orgCodeIndex) {
        organisation.setId(UUID.randomUUID().toString());
        organisation.setApplicationNumber(generatedIds.orgApplicationNumbers.get(orgAppNumIndex));
        organisation.setCode(generatedIds.orgCodes.get(orgCodeIndex));
        if (organisation.getIsActive() == null) {
            organisation.setIsActive(Boolean.TRUE);
        }
    }

    private int enrichOrganisationRelatedEntities(Organisation organisation, RequestInfo requestInfo, 
                                                   GeneratedIds generatedIds, int funcAppNumIndex) {
        enrichOrgAddress(organisation.getOrgAddress());
        enrichContactDetails(organisation.getContactDetails());
        enrichOrgDocument(organisation.getDocuments());
        enrichTaxIdentifier(organisation.getIdentifiers());
        int updatedIndex = enrichFunction(requestInfo, organisation.getFunctions(), generatedIds.orgFunctionApplicationNumbers, funcAppNumIndex);
        enrichJurisdiction(organisation.getJurisdiction());
        return updatedIndex;
    }

    private static class GeneratedIds {
        final List<String> orgApplicationNumbers;
        final List<String> orgCodes;
        final List<String> orgFunctionApplicationNumbers;

        GeneratedIds(List<String> orgApplicationNumbers, List<String> orgCodes, List<String> orgFunctionApplicationNumbers) {
            this.orgApplicationNumbers = orgApplicationNumbers;
            this.orgCodes = orgCodes;
            this.orgFunctionApplicationNumbers = orgFunctionApplicationNumbers;
        }
    }

    private void enrichJurisdiction(List<Jurisdiction> jurisdictionList) {
        if (!CollectionUtils.isEmpty(jurisdictionList)) {
            for (Jurisdiction jurisdiction : jurisdictionList) {
                jurisdiction.setId(UUID.randomUUID().toString());
            }
        }
    }

    private int enrichFunction(RequestInfo requestInfo,List<Function> functionList, List<String> orgFunctionApplicationNumbers, int funcAppNumIdFormatIndex) {
        if (!CollectionUtils.isEmpty(functionList)) {

            organisationUtil.setAuditDetailsForFunction(requestInfo.getUserInfo().getUuid(), functionList, Boolean.TRUE);

            for (Function function : functionList) {
                function.setId(UUID.randomUUID().toString());
                function.setApplicationNumber(orgFunctionApplicationNumbers.get(funcAppNumIdFormatIndex));
                if (function.getIsActive() == null) {
                    function.setIsActive(Boolean.TRUE);
                }

                List<Document> documents = function.getDocuments();
                enrichDocuments(documents);
                funcAppNumIdFormatIndex++;

            }
        }
        return funcAppNumIdFormatIndex;
    }

    private void enrichDocuments(List<Document> documents) {
        if (!CollectionUtils.isEmpty(documents)) {
            for (Document funcDocument : documents) {
                funcDocument.setId(UUID.randomUUID().toString());
                if (funcDocument.getIsActive() == null) {
                    funcDocument.setIsActive(Boolean.TRUE);
                }
            }
        }
    }

    private void enrichTaxIdentifier(List<Identifier> identifierList) {
        if (!CollectionUtils.isEmpty(identifierList)) {
            for (Identifier identifier : identifierList) {
                identifier.setId(UUID.randomUUID().toString());
                if (identifier.getIsActive() == null) {
                    identifier.setIsActive(Boolean.TRUE);
                }
            }
        }
    }

    private void enrichOrgDocument(List<Document> documentList) {
        if (!CollectionUtils.isEmpty(documentList)) {
            for (Document document : documentList) {
                document.setId(UUID.randomUUID().toString());
                if (document.getIsActive() == null) {
                    document.setIsActive(Boolean.TRUE);
                }
            }
        }
    }

    private void enrichOrgAddress(List<Address> orgAddressList){
        if (!CollectionUtils.isEmpty(orgAddressList)) {
            for (Address address : orgAddressList) {
                address.setId(UUID.randomUUID().toString());
//                address.getGeoLocation().setId(UUID.randomUUID().toString());
            }
        }
    }
    private void enrichContactDetails(List<ContactDetails> contactDetailsList){
        if (!CollectionUtils.isEmpty(contactDetailsList)) {
            for (ContactDetails contactDetails : contactDetailsList) {
                contactDetails.setId(UUID.randomUUID().toString());
            }
        }
    }

    /**
     * Enrich the update organisation registry with ids,custom id, audit details
     *
     * @param orgRequest
     */
    public void enrichUpdateOrgRegistryWithoutWorkFlow(OrgRequest orgRequest) {
        log.trace("OrganisationEnrichmentService::enrichUpdateOrgRegistryWithoutWorkFlow entry");
        RequestInfo requestInfo = orgRequest.getRequestInfo();
        List<Organisation> organisationList = orgRequest.getOrganisations();
        String tenantId = organisationList != null && !organisationList.isEmpty() 
                ? organisationList.get(0).getTenantId() : "unknown";
        log.info("Starting enrichment for organisation update, tenant: {}, organisation count: {}", 
                tenantId, organisationList != null ? organisationList.size() : 0);

        //set the audit details for organisation
        organisationUtil.setAuditDetailsForOrganisation(requestInfo.getUserInfo().getUuid(), organisationList, Boolean.FALSE);
        log.debug("Audit details set for organisations");

        for (Organisation organisation : organisationList) {
            List<Function> functionList = organisation.getFunctions();
            List<Identifier> identifierList = organisation.getIdentifiers();
            List<Document> documentList = organisation.getDocuments();

            //upsert identifier
            upsertIdentifier(identifierList);

            //upsert org document
            upsertOrgDocument(documentList);

            //upsert function and its document
            upsertFunction(requestInfo, tenantId, organisation, functionList);


        }
        log.info("Organisation enrichment completed successfully for tenant: {}", tenantId);
    }

    /**
     * @param documentList
     */
    private void upsertOrgDocument(List<Document> documentList) {
        if (!CollectionUtils.isEmpty(documentList)) {
            for (Document document : documentList) {
                if (StringUtils.isBlank(document.getId())) {
                    document.setId(UUID.randomUUID().toString());
                    if (document.getIsActive() == null) {
                        document.setIsActive(Boolean.TRUE);
                    }
                }
            }
        }
    }

    /**
     * @param identifierList
     */
    private void upsertIdentifier(List<Identifier> identifierList) {
        if (!CollectionUtils.isEmpty(identifierList)) {
            for (Identifier identifier : identifierList) {
                if (StringUtils.isBlank(identifier.getId())) {
                    identifier.setId(UUID.randomUUID().toString());
                    if (identifier.getIsActive() == null) {
                        identifier.setIsActive(Boolean.TRUE);
                    }
                }
            }
        }
    }

    /**
     * @param requestInfo
     * @param rootTenantId
     * @param organisation
     * @param functionList
     */
    private void upsertFunction(RequestInfo requestInfo, String rootTenantId, Organisation organisation, List<Function> functionList) {
        List<Function> upsertFunctionList = new ArrayList<>();
        List<Function> updateFunctionList = new ArrayList<>();
        List<Function> createFunctionList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(functionList)) {
            for (Function function : functionList) {
                if (StringUtils.isBlank(function.getId())) {
                    function.setId(UUID.randomUUID().toString());
                    function.setIsActive(Boolean.TRUE);
                    createFunctionList.add(function);
                } else {
                    updateFunctionList.add(function);
                }
            }
            //set the audit details for update function
            organisationUtil.setAuditDetailsForFunction(requestInfo.getUserInfo().getUuid(), updateFunctionList, Boolean.FALSE);

            //set the audit details for create function
            organisationUtil.setAuditDetailsForFunction(requestInfo.getUserInfo().getUuid(), createFunctionList, Boolean.TRUE);

            //get the application numbers for new function from Idgen service
            List<String> orgFunctionApplicationNumbers = new ArrayList<>();
            if (!createFunctionList.isEmpty()) {
                orgFunctionApplicationNumbers = idgenUtil.getIdList(requestInfo, rootTenantId, config.getFunctionApplicationNumberName()
                        , config.getFunctionApplicationNumberFormat(), createFunctionList.size());
            }

            //set the application numbers to new function
            int index = 0;
            if (!CollectionUtils.isEmpty(orgFunctionApplicationNumbers)) {
                for (Function function : createFunctionList) {
                    function.setApplicationNumber(orgFunctionApplicationNumbers.get(index));
                    index++;
                }
            }

            upsertFunctionList.addAll(createFunctionList);
            upsertFunctionList.addAll(updateFunctionList);

            //check any new function doc, if yes , set a new UUID
            setUUID(upsertFunctionList);

            organisation.setFunctions(upsertFunctionList);

        }
    }
    private void setUUID(List<Function> upsertFunctionList){
        for (Function function : upsertFunctionList) {
            List<Document> documents = function.getDocuments();
            if (!CollectionUtils.isEmpty(documents)) {
                for (Document document : documents) {
                    if (StringUtils.isBlank(document.getId())) {
                        document.setId(UUID.randomUUID().toString());
                        if (document.getIsActive() == null) {
                            document.setIsActive(Boolean.TRUE);
                        }
                    }
                }
            }
        }
    }
}
