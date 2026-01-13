package org.egov.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.config.Configuration;
import org.egov.util.EncryptionDecryptionUtil;
import org.egov.web.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
@Slf4j
public class EncryptionService {
    private EncryptionDecryptionUtil encryptionDecryptionUtil;
    private Configuration config;

    @Autowired
    public EncryptionService(EncryptionDecryptionUtil encryptionDecryptionUtil, Configuration config) {
        this.encryptionDecryptionUtil = encryptionDecryptionUtil;
        this.config = config;

    }

    public OrgRequest encryptDetails(OrgRequest orgRequest,String key){
        log.trace("EncryptionService::encryptDetails entry for OrgRequest");
        List<Organisation> organisationList = orgRequest.getOrganisations();
        String tenantId = organisationList != null && !organisationList.isEmpty() 
                ? organisationList.get(0).getTenantId() : "unknown";
        log.info("Starting encryption of organisation details for tenant: {}", tenantId);
        
        int encryptedCount = 0;
        for(Organisation organisation: organisationList){
            if (!CollectionUtils.isEmpty(organisation.getContactDetails())) {
                log.debug("Encrypting contact details for organisation ID: {}", organisation.getId());
                List<ContactDetails> encryptedContactDetails = (List<ContactDetails>) encryptionDecryptionUtil
                        .encryptObject(organisation.getContactDetails(), config.getStateLevelTenantId(), key, ContactDetails.class);
                organisation.setContactDetails(encryptedContactDetails);
                encryptedCount++;
            }
        }
        log.info("Encryption completed for {} organisations", encryptedCount);
        return orgRequest;
    }

    public OrgSearchRequest encryptDetails(OrgSearchRequest orgSearchRequest,String key){
        log.trace("EncryptionService::encryptDetails entry for OrgSearchRequest");
        String tenantId = orgSearchRequest.getSearchCriteria() != null 
                ? orgSearchRequest.getSearchCriteria().getTenantId() : "unknown";
        log.info("Starting encryption of search criteria for tenant: {}", tenantId);
        
        OrgSearchCriteria searchCriteria = orgSearchRequest.getSearchCriteria();
        OrgSearchCriteria encryptedSearchCriteria = encryptionDecryptionUtil
                .encryptObject(searchCriteria, config.getStateLevelTenantId(), key, OrgSearchCriteria.class);

        orgSearchRequest.setSearchCriteria(encryptedSearchCriteria);
        log.debug("Search criteria encryption completed");
        return orgSearchRequest;
    }

    public List<Organisation> decrypt(List<Organisation> organisationList, String key,OrgSearchRequest orgSearchRequest){
        log.trace("EncryptionService::decrypt entry");
        log.info("Starting decryption of {} organisations", organisationList != null ? organisationList.size() : 0);
        
        for(Organisation organisation: organisationList){
            List<ContactDetails> contactDetailsList = organisation.getContactDetails();
            if (!CollectionUtils.isEmpty(contactDetailsList)) {
                log.debug("Decrypting contact details for organisation ID: {}", organisation.getId());
                List<ContactDetails> decryptContactDetails = encryptionDecryptionUtil.decryptObject(contactDetailsList, key, ContactDetails.class, orgSearchRequest.getRequestInfo());
                organisation.setContactDetails(decryptContactDetails);
            }
        }
        log.info("Decryption completed for {} organisations", organisationList != null ? organisationList.size() : 0);
        return organisationList;
    }

}