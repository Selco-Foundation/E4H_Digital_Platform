package org.egov.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.config.Configuration;
import org.egov.kafka.OrganizationProducer;
import org.egov.web.models.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Asks for a user's login details to be emailed, for every path that creates a user or resets
 * their password.
 *
 * This service only decides that an email is due. im-services-analytics owns the template and
 * the sending: it reads the event published here and hands the rendered email to
 * egov-notification-mail. A failure never fails the operation that triggered it - the user exists
 * either way - so callers report the result instead ("created, but the email was not sent").
 *
 * The event carries a plaintext password, which the email needs. Nothing here may log it.
 */
@Service
@Slf4j
public class CredentialsNotificationService {

    static final String ACCOUNT_CREATED = "ACCOUNT_CREATED";
    static final String PASSWORD_RESET = "PASSWORD_RESET";

    private final OrganizationProducer producer;
    private final Configuration config;

    @Autowired
    public CredentialsNotificationService(OrganizationProducer producer, Configuration config) {
        this.producer = producer;
        this.config = config;
    }

    /** What the email needs to say. `organisationName` and `role` may be blank. */
    public record Credentials(String email, String name, String organisationName, String role,
                              String userName, String password, boolean reset) {
        @Override
        public String toString() {
            return "Credentials[userName=" + userName + "]";
        }
    }

    /**
     * Publishes the event.
     *
     * @return true when it was handed to Kafka; false when there was nowhere to send it,
     *         notifications are disabled, or publishing failed. Delivery of the email itself is
     *         asynchronous and is not confirmed here.
     */
    public boolean send(Credentials credentials) {
        if (!Boolean.TRUE.equals(config.getIsCredentialsNotificationEnabled())) {
            log.info("Credentials notifications disabled; no email for {}", credentials.userName());
            return false;
        }
        if (StringUtils.isBlank(credentials.email())) {
            log.warn("No email address for {}; credentials email not requested", credentials.userName());
            return false;
        }
        try {
            producer.push(config.getCredentialsNotificationTopic(), buildEvent(credentials));
            log.info("Credentials email requested for {}", credentials.userName());
            return true;
        } catch (Exception e) {
            // The exception text never includes the payload, so it is safe to log.
            log.error("Credentials email for {} could not be requested", credentials.userName(), e);
            return false;
        }
    }

    /** The contract im-services-analytics reads (its UserCredentialsNotification). */
    Map<String, Object> buildEvent(Credentials credentials) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("type", credentials.reset() ? PASSWORD_RESET : ACCOUNT_CREATED);
        event.put("email", credentials.email());
        event.put("name", credentials.name());
        event.put("organisationName", credentials.organisationName());
        event.put("role", credentials.role());
        event.put("userName", credentials.userName());
        event.put("password", credentials.password());
        return event;
    }

    /**
     * The roles to name in the email: every role but the EMPLOYEE role every org user is given,
     * by display name where there is one.
     */
    public static String describeRoles(List<Role> roles) {
        if (roles == null) {
            return null;
        }
        return roles.stream()
                .filter(Objects::nonNull)
                .filter(r -> !"EMPLOYEE".equalsIgnoreCase(r.getCode()))
                .map(r -> StringUtils.defaultIfBlank(r.getName(), r.getCode()))
                .distinct()
                .collect(Collectors.joining(", "));
    }
}
