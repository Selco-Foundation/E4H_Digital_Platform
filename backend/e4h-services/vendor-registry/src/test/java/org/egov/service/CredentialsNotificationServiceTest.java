package org.egov.service;

import org.egov.config.Configuration;
import org.egov.kafka.OrganizationProducer;
import org.egov.web.models.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CredentialsNotificationServiceTest {

    @Mock private OrganizationProducer producer;
    @Mock private Configuration config;

    @InjectMocks
    private CredentialsNotificationService service;

    private final CredentialsNotificationService.Credentials created = new CredentialsNotificationService.Credentials(
            "r@x.org", "Ramesh", "Sunrise Solar", "Vendor Administrator", "ramesh", "Rame@3210", false);

    @Test
    void publishesTheEventImServicesAnalyticsReads() {
        when(config.getIsCredentialsNotificationEnabled()).thenReturn(true);
        when(config.getCredentialsNotificationTopic()).thenReturn("user-credentials-notification");

        assertTrue(service.send(created));

        ArgumentCaptor<Object> sent = ArgumentCaptor.forClass(Object.class);
        verify(producer).push(eq("user-credentials-notification"), sent.capture());
        assertEquals(Map.of(
                "type", "ACCOUNT_CREATED",
                "email", "r@x.org",
                "name", "Ramesh",
                "organisationName", "Sunrise Solar",
                "role", "Vendor Administrator",
                "userName", "ramesh",
                "password", "Rame@3210"), sent.getValue());
    }

    @Test
    @SuppressWarnings("unchecked")
    void aResetIsTypedAsOne() {
        when(config.getIsCredentialsNotificationEnabled()).thenReturn(true);
        when(config.getCredentialsNotificationTopic()).thenReturn("topic");

        service.send(new CredentialsNotificationService.Credentials(
                "r@x.org", "Ramesh", null, null, "ramesh", "newpass1!", true));

        ArgumentCaptor<Object> sent = ArgumentCaptor.forClass(Object.class);
        verify(producer).push(eq("topic"), sent.capture());
        assertEquals("PASSWORD_RESET", ((Map<String, Object>) sent.getValue()).get("type"));
    }

    @Test
    void aPublishingFailureIsReportedNotThrown() {
        when(config.getIsCredentialsNotificationEnabled()).thenReturn(true);
        when(config.getCredentialsNotificationTopic()).thenReturn("topic");
        doThrow(new RuntimeException("broker down")).when(producer).push(any(), any());

        assertFalse(service.send(created));
    }

    @Test
    void noAddressOrDisabledPublishesNothing() {
        when(config.getIsCredentialsNotificationEnabled()).thenReturn(false);
        assertFalse(service.send(created));
        when(config.getIsCredentialsNotificationEnabled()).thenReturn(true);
        assertFalse(service.send(new CredentialsNotificationService.Credentials(
                " ", "R", null, null, "ramesh", "Rame@3210", false)));
        verifyNoInteractions(producer);
    }

    @Test
    void thePasswordNeverReachesToString() {
        assertFalse(created.toString().contains("Rame@3210"));
    }

    @Test
    void describesRolesWithoutTheEmployeeRole() {
        assertEquals("Vendor Administrator", CredentialsNotificationService.describeRoles(List.of(
                Role.builder().code("EMPLOYEE").name("Employee").build(),
                Role.builder().code("VENDOR_ADMIN").name("Vendor Administrator").build())));
    }
}
