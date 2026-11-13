package org.egov.user.domain.service.utils;

import java.util.Collections;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.kafka.CustomKafkaTemplate;
import org.egov.user.domain.model.Email;
import org.egov.user.domain.model.EmailRequest;
import org.egov.user.domain.model.SMSRequest;
import org.egov.user.domain.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static org.egov.user.config.UserServiceConstants.DEFAULT_OTP_LOGIN_SMS_MESSAGE;
import static org.egov.user.config.UserServiceConstants.EMAIL_UPDATION_CODE;
import static org.egov.user.config.UserServiceConstants.OTP_LOGIN_SMS_CODE;


@Slf4j
@Component
public class NotificationUtil {

    @Autowired
    private CustomKafkaTemplate<String, Object> kafkaTemplate;
    @Autowired
    private LocalizationUtil localizationUtil;

    @Value("${kafka.topics.notification.mail.name}")
    public String emailNotificationTopic;

    @Value("${kafka.topics.notification.sms.topic.name}")
    public String smsNotificationTopic;

    public void sendEmail(RequestInfo requestInfo, User existingUser, User updatedUser) {
        String oldEmail = existingUser.getEmailId();
        String newEmail = updatedUser.getEmailId();
        String mobileNumber = existingUser.getMobileNumber();
        String locale = existingUser.getLocale();

        String emailUpdationMessage = localizationUtil.getLocalizedMessage(EMAIL_UPDATION_CODE,locale,requestInfo);
        emailUpdationMessage = emailUpdationMessage.replace("{oldEmail}",oldEmail);
        emailUpdationMessage = emailUpdationMessage.replace("{newEmail}",newEmail);

        Email email = new Email();
        email.setEmailTo(Collections.singleton(oldEmail));
        email.setSubject("Email Update Notification");
        email.setBody(emailUpdationMessage);
        email.setHTML(false);

        SMSRequest smsRequest = new SMSRequest();
        smsRequest.setMobileNumber(mobileNumber);
        smsRequest.setMessage(emailUpdationMessage);

        EmailRequest emailRequest = EmailRequest.builder().requestInfo(requestInfo).email(email).build();
        kafkaTemplate.send(emailNotificationTopic,emailRequest);
        kafkaTemplate.send(smsNotificationTopic,smsRequest);
    }

    /**
     * Sends the OTP used to log in with a registered phone number.
     * The template comes from localization (USER_OTP_LOGIN_SMS) with {otp} and {validity} placeholders.
     */
    public void sendOtpLoginSms(String mobileNumber, String otp, int validitySeconds, String locale, RequestInfo requestInfo) {
        String template;
        try {
            template = localizationUtil.getLocalizedMessage(OTP_LOGIN_SMS_CODE, locale,
                    requestInfo != null ? requestInfo : new RequestInfo(), DEFAULT_OTP_LOGIN_SMS_MESSAGE);
        } catch (Exception e) {
            log.warn("Localization lookup failed for {}, using default OTP login message", OTP_LOGIN_SMS_CODE, e);
            template = DEFAULT_OTP_LOGIN_SMS_MESSAGE;
        }
        String message = template.replace("{otp}", otp)
                .replace("{validity}", String.valueOf(Math.max(1, validitySeconds / 60)));

        SMSRequest smsRequest = new SMSRequest();
        smsRequest.setMobileNumber(mobileNumber);
        smsRequest.setMessage(message);
        smsRequest.setCategory("OTP");
        smsRequest.setExpiryTime(System.currentTimeMillis() + validitySeconds * 1000L);
        kafkaTemplate.send(smsNotificationTopic, smsRequest);
    }

}
