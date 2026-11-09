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
        log.info("➡️ Validating search criteria for tenantId={} and module={}",
                inboxRequest.getInbox().getTenantId(),
                inboxRequest.getInbox().getProcessSearchCriteria().getModuleName());

        InboxQueryConfiguration config = mdmsUtil.getConfigFromMDMS(
                inboxRequest.getInbox().getTenantId(),
                inboxRequest.getInbox().getProcessSearchCriteria().getModuleName());

        Map<String, Boolean> isMandatoryMap = new HashMap<>();

        config.getAllowedSearchCriteria().forEach(
                searchParam -> {
                    isMandatoryMap.put(searchParam.getName(),
                            ObjectUtils.isEmpty(searchParam.getIsMandatory()) ? Boolean.FALSE : searchParam.getIsMandatory());
                }
        );
        log.debug("📄 Allowed search criteria with mandatory flags: {}", isMandatoryMap);

        HashMap<String, Object> moduleSearchCriteria = inboxRequest.getInbox().getModuleSearchCriteria();

        // Check if all mandatory fields exist in search criteria
        Set<String> mandatoryTrueFields = isMandatoryMap.entrySet().stream()
                .filter(entry -> Boolean.TRUE.equals(entry.getValue()))
                .map(Entry::getKey)
                .collect(Collectors.toSet());
        log.debug("✅ Mandatory fields required: {}", mandatoryTrueFields);

        if (!mandatoryTrueFields.isEmpty() && !moduleSearchCriteria.keySet().containsAll(mandatoryTrueFields)) {
            log.error("❌ Missing mandatory fields in moduleSearchCriteria. Provided={}, Required={}",
                    moduleSearchCriteria.keySet(), mandatoryTrueFields);
            throw new CustomException("INVALID_SEARCH_CRITERIA",
                    "Mandatory fields are missing in the moduleSearchCriteria");
        }

        Map<String, String> errorMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : moduleSearchCriteria.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (!(key.equals(SORT_ORDER_CONSTANT) || key.equals(SORT_BY_CONSTANT))) {
                if (isMandatoryMap.get(key)) {
                    if (ObjectUtils.isEmpty(value)) {
                        log.warn("⚠️ Field {} is mandatory but value is null/empty", key);
                        errorMap.put("INVALID_SEARCH_CRITERIA", "Field cannot be null or empty: " + key);
                    }
                }
            }
        }

        if (!CollectionUtils.isEmpty(errorMap)) {
            log.error("❌ Validation failed with errorMap={}", errorMap);
            throw new CustomException(errorMap);
        }

        log.info("✅ Validation successful for search criteria");
    }


    public void validateSearchCriteria(String tenantId, String moduleName, Map<String, Object> moduleSearchCriteria) {
        log.info("➡️ Validating search criteria for tenantId={} and module={}", tenantId, moduleName);

        InboxQueryConfiguration config = mdmsUtil.getConfigFromMDMS(tenantId, moduleName);
        log.debug("📄 Retrieved search configuration: {}", config);

        Map<String, Boolean> isMandatoryMap = new HashMap<>();
        config.getAllowedSearchCriteria().forEach(searchParam -> {
            isMandatoryMap.put(searchParam.getName(),
                    ObjectUtils.isEmpty(searchParam.getIsMandatory()) ? Boolean.FALSE : searchParam.getIsMandatory());
        });
        log.debug("✅ Mandatory fields map: {}", isMandatoryMap);

        Map<String, String> errorMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : moduleSearchCriteria.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (!(key.equals(SORT_ORDER_CONSTANT) || key.equals(SORT_BY_CONSTANT))) {
                if (Boolean.TRUE.equals(isMandatoryMap.get(key))) {
                    if (ObjectUtils.isEmpty(value)) {
                        log.warn("⚠️ Field '{}' is mandatory but value is null or empty", key);
                        errorMap.put("INVALID_SEARCH_CRITERIA", "Field cannot be null or empty: " + key);
                    }
                }
            }
        }

        if (!CollectionUtils.isEmpty(errorMap)) {
            log.error("❌ Validation failed with errors: {}", errorMap);
            throw new CustomException(errorMap);
        }

        log.info("✅ Validation successful for moduleSearchCriteria");
    }






}
