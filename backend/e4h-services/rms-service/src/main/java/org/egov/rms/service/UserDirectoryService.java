package org.egov.rms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.rms.config.RMSConfiguration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

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
    private final RestTemplate restTemplate;

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

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Object> entity = new HttpEntity<>(request, headers);
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    uri.toString(),
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            Object responseObj = response.getBody();
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

