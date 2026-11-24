package org.selco.e4h.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.selco.e4h.config.ConsumerConfiguration;
import org.selco.e4h.util.CommonUtility;
import org.selco.e4h.web.models.UserCredentialsNotification;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCredentialsEmailServiceTest {

    @Mock private CommonUtility commonUtility;
    @Mock private ConsumerConfiguration consumerConfiguration;
    @Mock private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private UserCredentialsEmailService service;

    private UserCredentialsNotification created;

    @BeforeEach
    void setUp() {
        lenient().when(commonUtility.escapeHtml(anyString())).thenAnswer(i -> ((String) i.getArgument(0)).replace("<", "&lt;"));
        lenient().when(commonUtility.getSelcoLogoUrl()).thenReturn("https://files/selco.png");
        lenient().when(commonUtility.getSauraLogoUrl()).thenReturn("https://files/saura.png");
        lenient().when(consumerConfiguration.getNotificationEmailTopic()).thenReturn("egov.core.notification.email");
        created = UserCredentialsNotification.builder()
                .type(UserCredentialsNotification.ACCOUNT_CREATED)
                .email("r@x.org").name("Ramesh").organisationName("Sunrise Solar")
                .role("Vendor Administrator").userName("ramesh").password("Rame@3210").build();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> sentEmail() {
        ArgumentCaptor<Object> sent = ArgumentCaptor.forClass(Object.class);
        verify(kafkaTemplate).send(eq("egov.core.notification.email"), sent.capture());
        return (Map<String, Object>) ((Map<String, Object>) sent.getValue()).get("email");
    }

    @Test
    void publishesTheCreatedEmailWithEveryDetail() {
        ReflectionTestUtils.setField(service, "loginUrl", "https://hub.example.org");

        service.send(created);

        Map<String, Object> email = sentEmail();
        assertEquals(Set.of("r@x.org"), email.get("emailTo"));
        assertEquals("Your E4H Management Hub login details", email.get("subject"));
        assertEquals(true, email.get("isHTML"));
        String body = (String) email.get("body");
        assertTrue(body.contains("Hello Ramesh,"));
        assertTrue(body.contains("An account has been created for you on the E4H Management Hub."));
        for (String value : new String[]{"Sunrise Solar", "Vendor Administrator", "ramesh", "Rame@3210", "https://hub.example.org"}) {
            assertTrue(body.contains(value), "missing " + value);
        }
        assertTrue(body.contains("Web users: please log in and set a new password when prompted."));
        assertFalse(body.contains("${"), "every placeholder is filled");
    }

    @Test
    void aResetUsesTheResetWordingAndLeavesOutUnknownRows() {
        UserCredentialsNotification reset = UserCredentialsNotification.builder()
                .type(UserCredentialsNotification.PASSWORD_RESET)
                .email("r@x.org").name("Ramesh").userName("ramesh").password("newpass1!").build();

        service.send(reset);

        Map<String, Object> email = sentEmail();
        assertEquals("Your password has been reset", email.get("subject"));
        String body = (String) email.get("body");
        assertTrue(body.contains("Your password has been reset by an administrator."));
        assertFalse(body.contains("Organization"));
        assertTrue(body.contains("E4H Management Hub"), "no login URL configured falls back to the app name");
    }

    @Test
    void valuesAreHtmlEscaped() {
        created.setName("<b>Ramesh</b>");
        service.send(created);
        assertFalse(((String) sentEmail().get("body")).contains("<b>Ramesh"));
    }

    @Test
    void noAddressSendsNothing() {
        created.setEmail(" ");
        service.send(created);
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void thePasswordNeverReachesToString() {
        assertFalse(created.toString().contains("Rame@3210"));
    }
}
