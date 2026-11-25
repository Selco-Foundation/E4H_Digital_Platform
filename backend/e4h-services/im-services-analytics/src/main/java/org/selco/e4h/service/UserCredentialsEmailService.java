package org.selco.e4h.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.selco.e4h.config.ConsumerConfiguration;
import org.selco.e4h.util.CommonUtility;
import org.selco.e4h.util.EscalationEmailTemplateHelper;
import org.selco.e4h.web.models.UserCredentialsNotification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The E4H Management Hub login-details email: sent when a user is created, or when an
 * administrator resets their password.
 *
 * vendor-registry decides when to send it and publishes a {@link UserCredentialsNotification};
 * this renders it and hands it to egov-notification-mail, like every other email here.
 *
 * The body carries a plaintext password, as requested. It is never logged, and the user is
 * prompted to change it at their next web sign-in.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserCredentialsEmailService {

    static final String TEMPLATE_PATH = "templates/user_credentials_email.html";
    static final String CREATED_SUBJECT = "Your E4H Management Hub login details";
    static final String RESET_SUBJECT = "Your password has been reset";
    private static final String DEFAULT_LOGIN = "E4H Management Hub";

    private final CommonUtility commonUtility;
    private final ConsumerConfiguration consumerConfiguration;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /** Where the email tells users to sign in; blank shows the app's name instead of a link. */
    @Value("${user.credentials.email.login.url:}")
    private String loginUrl;

    /** Renders and publishes the email. Throws when it cannot, so the caller can log the failure. */
    public void send(UserCredentialsNotification notification) {
        if (StringUtils.isBlank(notification.getEmail())) {
            log.warn("Credentials email for {} has no address; not sent", notification.getUserName());
            return;
        }
        Map<String, Object> email = new HashMap<>();
        email.put("emailTo", Set.of(notification.getEmail()));
        email.put("subject", notification.isReset() ? RESET_SUBJECT : CREATED_SUBJECT);
        email.put("body", renderBody(notification));
        email.put("isHTML", true);

        Map<String, Object> emailRequest = new HashMap<>();
        emailRequest.put("requestInfo", new HashMap<>());
        emailRequest.put("email", email);

        kafkaTemplate.send(consumerConfiguration.getNotificationEmailTopic(), emailRequest);
        log.info("Credentials email ({}) published for {}", notification.getType(), notification.getUserName());
    }

    String renderBody(UserCredentialsNotification notification) {
        String template;
        try {
            template = EscalationEmailTemplateHelper.loadTemplate(TEMPLATE_PATH);
        } catch (Exception e) {
            throw new IllegalStateException("Credentials email template could not be read", e);
        }
        String recipient = StringUtils.defaultIfBlank(notification.getName(), notification.getUserName());
        Map<String, String> variables = EscalationEmailTemplateHelper.baseBrandingVariables(commonUtility, recipient);
        variables.put("TITLE", notification.isReset() ? "Password reset" : "Your login details");
        variables.put("OPENING", notification.isReset()
                ? "Your password has been reset by an administrator."
                : "An account has been created for you on the E4H Management Hub.");
        variables.put("DETAIL_ROWS", renderDetailRows(notification));
        return EscalationEmailTemplateHelper.render(template, variables);
    }

    /** Organisation and role are left out when not known, rather than shown empty. */
    private String renderDetailRows(UserCredentialsNotification notification) {
        StringBuilder rows = new StringBuilder();
        appendRow(rows, "Organization", notification.getOrganisationName());
        appendRow(rows, "Role", notification.getRole());
        appendRow(rows, "Username", notification.getUserName());
        appendRow(rows, "Password", notification.getPassword());
        if (StringUtils.isNotBlank(loginUrl)) {
            String url = commonUtility.escapeHtml(loginUrl);
            rows.append(row("Login", "<a href=\"" + url + "\" target=\"_blank\" rel=\"noopener\" style=\"color:#f08400\">" + url + "</a>"));
        } else {
            appendRow(rows, "Login", DEFAULT_LOGIN);
        }
        return rows.toString();
    }

    private void appendRow(StringBuilder rows, String label, String value) {
        if (StringUtils.isNotBlank(value)) {
            rows.append(row(label, commonUtility.escapeHtml(value)));
        }
    }

    private static String row(String label, String valueHtml) {
        return "<tr><td style=\"padding:8px 12px 8px 0;font-size:14px;color:#6b7280;white-space:nowrap;\">" + label
                + "</td><td style=\"padding:8px 0;font-size:14px;color:#1f2937;font-weight:700;\">" + valueHtml + "</td></tr>";
    }
}
