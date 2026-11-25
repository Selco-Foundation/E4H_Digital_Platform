package org.selco.e4h.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.selco.e4h.service.UserCredentialsEmailService;
import org.selco.e4h.web.models.UserCredentialsNotification;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * Emails users their login details, as published by vendor-registry on user creation and admin
 * password reset.
 *
 * The record holds a plaintext password, so unlike the other listeners here it is never logged -
 * not even on failure. A failed email is logged by user name and dropped: the account exists
 * either way, and the offset must not stall on it.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserCredentialsListener {

    private final UserCredentialsEmailService emailService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${kafka.topics.user-credentials-notification:user-credentials-notification}",
            groupId = "${spring.kafka.consumer.group-id}-user-credentials")
    public void listen(final HashMap<String, Object> record) {
        String userName = record == null ? null : String.valueOf(record.get("userName"));
        try {
            emailService.send(objectMapper.convertValue(record, UserCredentialsNotification.class));
        } catch (Exception e) {
            // e is safe to log: nothing in the send path puts the record in an exception message.
            log.error("Credentials email for {} could not be sent", userName, e);
        }
    }
}
