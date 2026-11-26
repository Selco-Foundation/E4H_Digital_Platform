package org.egov.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.request.RequestInfo;
import org.egov.config.Configuration;
import org.egov.repository.ServiceRequestRepository;
import org.egov.tracer.model.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/** The read-modify-write trap: a password set must not blank any other field (MH2-06). */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserUtilPasswordTest {

    @Mock private ServiceRequestRepository repository;
    @Mock private Configuration configs;

    private UserUtil userUtil;

    @BeforeEach
    void setUp() {
        userUtil = new UserUtil(new ObjectMapper(), repository, configs);
        when(configs.getUserHost()).thenReturn("http://user");
        when(configs.getUserSearchEndpoint()).thenReturn("/user/_search");
        when(configs.getUserUpdateEndpoint()).thenReturn("/user/users/_updatenovalidate");
        when(configs.getUserCreateEndpoint()).thenReturn("/user/users/_createnovalidate");
    }

    private static Map<String, Object> stored(String mobile) {
        Map<String, Object> user = new LinkedHashMap<>();
        user.put("uuid", "u1");
        user.put("userName", "ramesh");
        user.put("name", "Ramesh");
        user.put("mobileNumber", mobile);
        user.put("relationship", "FATHER");
        user.put("fatherOrHusbandName", "Suresh");
        user.put("alternatemobilenumber", "9000000001");
        user.put("dob", "1990-02-01");
        return user;
    }

    private void searchReturns(boolean active, Map<String, Object> user) {
        when(repository.fetchResult(argThat(sb -> sb != null && sb.toString().endsWith("/user/_search")),
                argThat(body -> body instanceof Map<?, ?> m && Boolean.valueOf(active).equals(m.get("active")))))
                .thenReturn(user == null ? Map.of("user", List.of()) : new LinkedHashMap<>(Map.of("user", List.of(user))));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> sentUser() {
        ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
        verify(repository).fetchResult(argThat(sb -> sb.toString().endsWith("_updatenovalidate")), body.capture());
        return (Map<String, Object>) ((Map<String, Object>) body.getValue()).get("user");
    }

    @Test
    void everyStoredFieldGoesBackAndOnlyThePasswordAndExpiryChange() {
        searchReturns(true, stored("9876543210"));
        when(repository.fetchResult(argThat(sb -> sb.toString().endsWith("_updatenovalidate")), any()))
                .thenReturn(new LinkedHashMap<>(Map.of("user", List.of())));

        Date expiry = new Date(0);
        userUtil.setPasswordPreservingUser(new RequestInfo(), "u1", "abc1@de", expiry);

        Map<String, Object> sent = sentUser();
        assertEquals("FATHER", sent.get("relationship"));
        assertEquals("Suresh", sent.get("fatherOrHusbandName"));
        assertEquals("9000000001", sent.get("alternatemobilenumber"));
        assertEquals("9876543210", sent.get("mobileNumber"));
        assertEquals("01/02/1990", sent.get("dob"), "dob is converted to the update contract's format");
        assertEquals("abc1@de", sent.get("password"));
        assertEquals("01-01-1970 00:00:00", sent.get("pwdExpiryDate"));
    }

    @Test
    void anInactiveUserIsFoundToo() {
        searchReturns(true, null);
        searchReturns(false, stored("9876543210"));
        when(repository.fetchResult(argThat(sb -> sb.toString().endsWith("_updatenovalidate")), any()))
                .thenReturn(new LinkedHashMap<>(Map.of("user", List.of())));

        userUtil.setPasswordPreservingUser(new RequestInfo(), "u1", "abc1@de", new Date());

        assertEquals("ramesh", sentUser().get("userName"));
    }

    @Test
    void aMaskedRecordIsNeverWrittenBack() {
        searchReturns(true, stored("******3210"));

        CustomException e = assertThrows(CustomException.class,
                () -> userUtil.setPasswordPreservingUser(new RequestInfo(), "u1", "abc1@de", new Date()));
        assertEquals("USER_RECORD_MASKED", e.getCode());
        verify(repository, never()).fetchResult(argThat(sb -> sb.toString().endsWith("_updatenovalidate")), any());
    }

    @Test
    void anUnknownUserIsRefused() {
        searchReturns(true, null);
        searchReturns(false, null);
        assertThrows(CustomException.class,
                () -> userUtil.setPasswordPreservingUser(new RequestInfo(), "u1", "abc1@de", new Date()));
    }
}
