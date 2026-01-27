package org.egov.inbox.service.V2.validator;


import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.egov.inbox.util.MDMSUtil;
import org.egov.inbox.web.model.InboxRequest;
import org.egov.inbox.web.model.V2.InboxQueryConfiguration;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.Map;

import static org.egov.inbox.util.InboxConstants.SORT_BY_CONSTANT;
import static org.egov.inbox.util.InboxConstants.SORT_ORDER_CONSTANT;
@Slf4j
@Component
public class ValidatorDefaultImplementation implements SearchCriteriaValidatorInterface {


    @Autowired
    private MDMSUtil mdmsUtil;

    @Override
    public void validateSearchCriteria(InboxRequest inboxRequest) {
        log.trace("Method invoked: validateSearchCriteria");
        String tenantId = inboxRequest.getInbox().getTenantId();
        String moduleName = inboxRequest.getInbox().getProcessSearchCriteria().getModuleName();
        
        log.info("Validating search criteria - tenantId: {}, module: {}", tenantId, moduleName);

        InboxQueryConfiguration config = fetchInboxQueryConfiguration(inboxRequest);
        Map<String, Boolean> isMandatoryMap = buildMandatoryFieldsMap(config);
        HashMap<String, Object> moduleSearchCriteria = inboxRequest.getInbox().getModuleSearchCriteria();
        
        validateMandatoryFieldsPresent(moduleSearchCriteria, isMandatoryMap);
        validateMandatoryFieldsNotEmpty(moduleSearchCriteria, isMandatoryMap);

        log.info("Validation successful for search criteria");
    }

    private InboxQueryConfiguration fetchInboxQueryConfiguration(InboxRequest inboxRequest) {
        log.trace("Method invoked: fetchInboxQueryConfiguration");
        log.debug("Fetching inbox query configuration from MDMS");
        InboxQueryConfiguration config = mdmsUtil.getConfigFromMDMS(
                inboxRequest.getInbox().getTenantId(),
                inboxRequest.getInbox().getProcessSearchCriteria().getModuleName());
        log.debug("InboxQueryConfiguration fetched");
        return config;
    }

    private Map<String, Boolean> buildMandatoryFieldsMap(InboxQueryConfiguration config) {
        log.trace("Method invoked: buildMandatoryFieldsMap");
        Map<String, Boolean> isMandatoryMap = new HashMap<>();
        config.getAllowedSearchCriteria().forEach(
                searchParam -> {
                    isMandatoryMap.put(searchParam.getName(),
                            ObjectUtils.isEmpty(searchParam.getIsMandatory()) ? Boolean.FALSE : searchParam.getIsMandatory());
                }
        );
        log.debug("Allowed search criteria with mandatory flags - totalCriteria: {}", isMandatoryMap.size());
        return isMandatoryMap;
    }

    private void validateMandatoryFieldsPresent(HashMap<String, Object> moduleSearchCriteria, Map<String, Boolean> isMandatoryMap) {
        log.trace("Method invoked: validateMandatoryFieldsPresent");
        Set<String> mandatoryTrueFields = isMandatoryMap.entrySet().stream()
                .filter(entry -> Boolean.TRUE.equals(entry.getValue()))
                .map(Entry::getKey)
                .collect(Collectors.toSet());
        log.debug("Mandatory fields identified - count: {}", mandatoryTrueFields.size());

        if (!mandatoryTrueFields.isEmpty() && !moduleSearchCriteria.keySet().containsAll(mandatoryTrueFields)) {
            log.error("Missing mandatory fields in moduleSearchCriteria - provided: {}, required: {}",
                    moduleSearchCriteria.keySet().size(), mandatoryTrueFields.size());
            throw new CustomException("INVALID_SEARCH_CRITERIA",
                    "Mandatory fields are missing in the moduleSearchCriteria");
        }
    }

    private void validateMandatoryFieldsNotEmpty(HashMap<String, Object> moduleSearchCriteria, Map<String, Boolean> isMandatoryMap) {
        log.trace("Method invoked: validateMandatoryFieldsNotEmpty");
        Map<String, String> errorMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : moduleSearchCriteria.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (!(key.equals(SORT_ORDER_CONSTANT) || key.equals(SORT_BY_CONSTANT))) {
                if (isMandatoryMap.get(key)) {
                    if (ObjectUtils.isEmpty(value)) {
                        log.warn("Field is mandatory but value is null or empty - field: {}", key);
                        errorMap.put("INVALID_SEARCH_CRITERIA", "Field cannot be null or empty: " + key);
                    }
                }
            }
        }

        if (!CollectionUtils.isEmpty(errorMap)) {
            log.error("Validation failed - errorCount: {}", errorMap.size());
            throw new CustomException(errorMap);
        }
    }


    public void validateSearchCriteria(String tenantId, String moduleName, Map<String, Object> moduleSearchCriteria) {
        log.trace("Method invoked: validateSearchCriteria - tenantId: {}, module: {}", tenantId, moduleName);
        log.info("Validating search criteria - tenantId: {}, module: {}", tenantId, moduleName);

        InboxQueryConfiguration config = fetchInboxQueryConfiguration(tenantId, moduleName);
        Map<String, Boolean> isMandatoryMap = buildMandatoryFieldsMapForSimpleSearch(config);
        validateMandatoryFieldsNotEmptyForSimpleSearch(moduleSearchCriteria, isMandatoryMap);

        log.info("Validation successful for moduleSearchCriteria");
    }

    private InboxQueryConfiguration fetchInboxQueryConfiguration(String tenantId, String moduleName) {
        log.trace("Method invoked: fetchInboxQueryConfiguration");
        log.debug("Fetching inbox query configuration from MDMS");
        InboxQueryConfiguration config = mdmsUtil.getConfigFromMDMS(tenantId, moduleName);
        log.debug("InboxQueryConfiguration retrieved - allowedCriteria: {}", 
                config != null ? config.getAllowedSearchCriteria().size() : 0);
        return config;
    }

    private Map<String, Boolean> buildMandatoryFieldsMapForSimpleSearch(InboxQueryConfiguration config) {
        log.trace("Method invoked: buildMandatoryFieldsMapForSimpleSearch");
        Map<String, Boolean> isMandatoryMap = new HashMap<>();
        config.getAllowedSearchCriteria().forEach(searchParam -> {
            isMandatoryMap.put(searchParam.getName(),
                    ObjectUtils.isEmpty(searchParam.getIsMandatory()) ? Boolean.FALSE : searchParam.getIsMandatory());
        });
        log.debug("Mandatory fields map built - totalFields: {}, mandatoryCount: {}", 
                isMandatoryMap.size(), 
                isMandatoryMap.values().stream().filter(Boolean::booleanValue).count());
        return isMandatoryMap;
    }

    private void validateMandatoryFieldsNotEmptyForSimpleSearch(Map<String, Object> moduleSearchCriteria, Map<String, Boolean> isMandatoryMap) {
        log.trace("Method invoked: validateMandatoryFieldsNotEmptyForSimpleSearch");
        Map<String, String> errorMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : moduleSearchCriteria.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (!(key.equals(SORT_ORDER_CONSTANT) || key.equals(SORT_BY_CONSTANT))) {
                if (Boolean.TRUE.equals(isMandatoryMap.get(key))) {
                    if (ObjectUtils.isEmpty(value)) {
                        log.warn("Field is mandatory but value is null or empty - field: {}", key);
                        errorMap.put("INVALID_SEARCH_CRITERIA", "Field cannot be null or empty: " + key);
                    }
                }
            }
        }

        if (!CollectionUtils.isEmpty(errorMap)) {
            log.error("Validation failed - errorCount: {}", errorMap.size());
            throw new CustomException(errorMap);
        }
    }






}
