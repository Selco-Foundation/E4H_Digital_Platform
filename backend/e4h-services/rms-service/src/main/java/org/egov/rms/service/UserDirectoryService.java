package org.egov.rms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.rms.config.RMSConfiguration;
import org.egov.rms.repository.ServiceRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDirectoryService {

    private final RMSConfiguration config;
    private final ServiceRequestRepository serviceRequestRepository;

    public Map<String, String> getDisplayNamesByUuids(RequestInfo requestInfo, Set<String> uuids, String tenantId) {
        if (uuids == null || uuids.isEmpty()) {
            return Collections.emptyMap();
        }
        List<String> validUuids = new ArrayList<>();
        for (String uuid : uuids) {
            if (StringUtils.hasText(uuid)) {
                validUuids.add(uuid.trim());
            }
        }
        if (validUuids.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            StringBuilder uri = new StringBuilder()
                    .append(config.getUserHost())
                    .append(config.getUserSearchEndpoint());

            Map<String, Object> request = new LinkedHashMap<>();
            request.put("RequestInfo", requestInfo);
            request.put("uuid", validUuids);
            if (StringUtils.hasText(tenantId)) {
                request.put("tenantId", tenantId.trim());
            }

            Object responseObj = serviceRequestRepository.postForObject(uri, request);
            if (!(responseObj instanceof Map<?, ?> responseMap)) {
                return Collections.emptyMap();
            }

            List<?> users = extractUsers(responseMap);
            Map<String, String> resolved = new HashMap<>();
            for (Object userObj : users) {
                if (!(userObj instanceof Map<?, ?> user)) {
                    continue;
                }
                Object uuidObj = user.get("uuid");
                if (!(uuidObj instanceof String userUuid) || !StringUtils.hasText(userUuid)) {
                    continue;
                }
                String displayName = resolveDisplayName(user);
                if (StringUtils.hasText(displayName)) {
                    resolved.put(userUuid.trim(), displayName.trim());
                }
            }
            return resolved;
        } catch (Exception e) {
            log.warn("Failed to resolve pausedBy user details from user-service; falling back to UUID", e);
            return Collections.emptyMap();
        }
    }

    private List<?> extractUsers(Map<?, ?> responseMap) {
        Object usersObj = responseMap.get("user");
        if (usersObj instanceof List<?>) {
            return (List<?>) usersObj;
        }
        usersObj = responseMap.get("users");
        if (usersObj instanceof List<?>) {
            return (List<?>) usersObj;
        }
        usersObj = responseMap.get("User");
        if (usersObj instanceof List<?>) {
            return (List<?>) usersObj;
        }
        return Collections.emptyList();
    }

    private String resolveDisplayName(Map<?, ?> user) {
        Object nameObj = user.get("name");
        if (nameObj instanceof String name && StringUtils.hasText(name)) {
            return name;
        }
        Object userNameObj = user.get("userName");
        if (userNameObj instanceof String userName && StringUtils.hasText(userName)) {
            return userName;
        }
        return null;
    }
}

