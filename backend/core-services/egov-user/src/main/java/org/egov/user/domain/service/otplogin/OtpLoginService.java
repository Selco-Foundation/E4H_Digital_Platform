package org.egov.user.domain.service.otplogin;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.user.domain.model.Role;
import org.egov.user.domain.model.User;
import org.egov.user.domain.model.enums.UserType;
import org.egov.user.domain.service.UserService;
import org.egov.user.domain.service.utils.NotificationUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Login with a registered phone number and an SMS OTP, for facility users and CRM (EMPLOYEE users).
 *
 * <ol>
 *   <li>{@link #sendOtp}: sends a one-time code to the number if at least one active account has it.</li>
 *   <li>{@link #verifyOtp}: checks the code and returns a short-lived login ticket plus the accounts mapped to
 *   the number (numbers are not unique, so the user may have to pick one).</li>
 *   <li>/oauth/token with loginMethod=OTP, username = chosen account, password = ticket:
 *   {@link #consumeLoginTicket} is called from the authentication provider, so the token, roles and
 *   session are exactly the same as with the password login.</li>
 * </ol>
 *
 * All limits are counted per tenant + phone number, separately from the password lockout.
 */
@Service
@Slf4j
public class OtpLoginService {

    private static final String KEY_PREFIX = "otplogin:";
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^[0-9]{10}$");

    private final OtpLoginStore store;
    private final UserService userService;
    private final NotificationUtil notificationUtil;
    private final SecureRandom random = new SecureRandom();

    private final boolean enabled;
    private final int otpLength;
    private final int otpValiditySeconds;
    private final int resendCooldownSeconds;
    private final int maxResends;
    private final int maxWrongAttempts;
    private final int blockSeconds;
    private final int ticketValiditySeconds;
    private final boolean logOtp;
    private final String fixedOtp;

    public OtpLoginService(OtpLoginStore store, UserService userService, NotificationUtil notificationUtil,
                           @Value("${employee.otp.login.enabled:false}") boolean enabled,
                           @Value("${employee.otp.login.otp.length:6}") int otpLength,
                           @Value("${employee.otp.login.otp.validity.seconds:300}") int otpValiditySeconds,
                           @Value("${employee.otp.login.resend.cooldown.seconds:30}") int resendCooldownSeconds,
                           @Value("${employee.otp.login.max.resends:3}") int maxResends,
                           @Value("${employee.otp.login.max.wrong.attempts:5}") int maxWrongAttempts,
                           @Value("${employee.otp.login.block.minutes:15}") int blockMinutes,
                           @Value("${employee.otp.login.ticket.validity.seconds:120}") int ticketValiditySeconds,
                           @Value("${employee.otp.login.log.otp.enabled:false}") boolean logOtp,
                           @Value("${employee.otp.login.fixed.otp.enabled:false}") boolean fixedOtpEnabled,
                           @Value("${employee.otp.login.fixed.otp.value:123456}") String fixedOtpValue) {
        this.store = store;
        this.userService = userService;
        this.notificationUtil = notificationUtil;
        this.enabled = enabled;
        this.otpLength = otpLength;
        this.otpValiditySeconds = otpValiditySeconds;
        this.resendCooldownSeconds = resendCooldownSeconds;
        this.maxResends = maxResends;
        this.maxWrongAttempts = maxWrongAttempts;
        this.blockSeconds = blockMinutes * 60;
        this.ticketValiditySeconds = ticketValiditySeconds;
        this.logOtp = logOtp;
        if (logOtp) {
            log.info("employee.otp.login.log.otp.enabled=true: OTP login codes will be written to the logs. "
                    + "Use only in environments without SMS, never in production");
        }
        if (fixedOtpEnabled) {
            if (fixedOtpValue == null || !fixedOtpValue.matches("^[0-9]{" + otpLength + "}$")) {
                throw new IllegalStateException("employee.otp.login.fixed.otp.value must be " + otpLength + " digits");
            }
            this.fixedOtp = fixedOtpValue;
            log.warn("employee.otp.login.fixed.otp.enabled=true: every OTP login code is the fixed value. "
                    + "Use only in dev/test environments, never in production");
        } else {
            this.fixedOtp = null;
        }
    }

    public int getOtpValiditySeconds() {
        return otpValiditySeconds;
    }

    public int getResendCooldownSeconds() {
        return resendCooldownSeconds;
    }

    /**
     * Sends an OTP if the number belongs to at least one active account. The outcome is the same whether
     * or not the number is registered, so the endpoint cannot be used to find out which numbers exist.
     */
    public void sendOtp(String tenantId, String mobileNumber, RequestInfo requestInfo) {
        ensureEnabled();
        validate(tenantId, mobileNumber);
        String subject = subject(tenantId, mobileNumber);

        ensureNotBlocked(subject);
        if (!store.putIfAbsent(key("cooldown", subject), "1", resendCooldownSeconds)) {
            throw new CustomException("OTP_LOGIN_RESEND_TOO_SOON",
                    "Please wait " + resendCooldownSeconds + " seconds before requesting a new OTP");
        }
        // First send + maxResends resends within the block window.
        long sends = store.increment(key("sends", subject), blockSeconds);
        if (sends > maxResends + 1L) {
            throw new CustomException("OTP_LOGIN_RESEND_LIMIT_REACHED",
                    "Maximum number of OTP resends reached. Please try again later or log in with your ID and password");
        }

        List<User> users = userService.findActiveUsersByMobileNumber(mobileNumber, tenantId, UserType.EMPLOYEE);
        if (users.isEmpty()) {
            log.info("OTP login requested for a number with no active account, no SMS sent");
            return;
        }

        String otp = generateOtp();
        store.put(key("otp", subject), hashOtp(otp, subject), otpValiditySeconds);
        if (logOtp) {
            log.info("[OTP LOGIN - DEV ONLY] tenant={} mobile={} otp={} (valid {}s)",
                    tenantId, mobileNumber, otp, otpValiditySeconds);
        }
        notificationUtil.sendOtpLoginSms(mobileNumber, otp, otpValiditySeconds, users.get(0).getLocale(), requestInfo);
    }

    /**
     * Checks the OTP (single use) and returns a login ticket bound to the accounts mapped to the number.
     */
    public OtpLoginVerification verifyOtp(String tenantId, String mobileNumber, String otp) {
        ensureEnabled();
        validate(tenantId, mobileNumber);
        if (otp == null || !otp.matches("^[0-9]{" + otpLength + "}$")) {
            throw new CustomException("OTP_LOGIN_INVALID_OTP", "OTP must be " + otpLength + " digits");
        }
        String subject = subject(tenantId, mobileNumber);
        ensureNotBlocked(subject);

        String otpKey = key("otp", subject);
        String storedHash = store.get(otpKey);
        if (storedHash == null) {
            throw new CustomException("OTP_LOGIN_OTP_EXPIRED", "OTP has expired or was not requested. Please request a new OTP");
        }

        if (!otpMatches(storedHash, otp, subject)) {
            long attempts = store.increment(key("attempts", subject), blockSeconds);
            if (attempts >= maxWrongAttempts) {
                store.put(key("block", subject), "1", blockSeconds);
                store.delete(otpKey);
                store.delete(key("attempts", subject));
                throw blockedException();
            }
            throw new CustomException("OTP_LOGIN_INVALID_OTP",
                    "Invalid OTP. " + (maxWrongAttempts - attempts) + " attempt(s) left");
        }

        // Single use: only the caller that actually removes the OTP may continue.
        if (!store.delete(otpKey)) {
            throw new CustomException("OTP_LOGIN_OTP_EXPIRED", "OTP has expired or was already used. Please request a new OTP");
        }
        store.delete(key("attempts", subject));
        store.delete(key("sends", subject));
        store.delete(key("cooldown", subject));

        List<User> users = userService.findActiveUsersByMobileNumber(mobileNumber, tenantId, UserType.EMPLOYEE);
        if (users.isEmpty()) {
            throw new CustomException("OTP_LOGIN_NO_ACCOUNT", "No active account is linked to this mobile number");
        }

        String ticket = newTicket();
        String uuids = users.stream().map(User::getUuid).collect(Collectors.joining(","));
        store.put(key("ticket", ticket), tenantId + "|" + uuids, ticketValiditySeconds);

        List<OtpLoginAccount> accounts = users.stream().map(this::toAccount).collect(Collectors.toList());
        return new OtpLoginVerification(ticket, ticketValiditySeconds, accounts);
    }

    /**
     * Called by the authentication provider for loginMethod=OTP. Accepts the ticket only for one of the
     * accounts it was issued for, and consumes it on success. A ticket sent with the wrong account is left
     * intact so the user can still pick the right one.
     */
    public boolean consumeLoginTicket(String ticket, User user, String tenantId) {
        if (!enabled || ticket == null || ticket.isEmpty() || user == null || user.getUuid() == null) {
            return false;
        }
        if (user.getType() != UserType.EMPLOYEE) {
            return false;
        }
        String ticketKey = key("ticket", ticket);
        String value = store.get(ticketKey);
        if (value == null) {
            return false;
        }
        int separator = value.indexOf('|');
        if (separator < 0) {
            return false;
        }
        String ticketTenant = value.substring(0, separator);
        Set<String> uuids = new HashSet<>(Arrays.asList(value.substring(separator + 1).split(",")));
        if (!ticketTenant.equals(tenantId) || !uuids.contains(user.getUuid())) {
            log.warn("OTP login ticket presented for an account it was not issued for");
            return false;
        }
        return store.delete(ticketKey);
    }

    private OtpLoginAccount toAccount(User user) {
        List<String> roleCodes = user.getRoles() == null ? Collections.emptyList()
                : user.getRoles().stream().map(Role::getCode).distinct().sorted().collect(Collectors.toList());
        return OtpLoginAccount.builder()
                .userName(user.getUsername())
                .name(user.getName())
                .tenantId(user.getTenantId())
                .roleCodes(roleCodes)
                .build();
    }

    private void ensureEnabled() {
        if (!enabled) {
            throw new CustomException("OTP_LOGIN_DISABLED", "Login with OTP is not enabled");
        }
    }

    private void ensureNotBlocked(String subject) {
        if (store.get(key("block", subject)) != null) {
            throw blockedException();
        }
    }

    private CustomException blockedException() {
        return new CustomException("OTP_LOGIN_BLOCKED", "Too many wrong OTP attempts. OTP login for this number is blocked for "
                + (blockSeconds / 60) + " minutes. You can still log in with your ID and password");
    }

    private void validate(String tenantId, String mobileNumber) {
        if (tenantId == null || tenantId.trim().isEmpty()) {
            throw new CustomException("TENANT_ID_MANDATORY", "tenantId is mandatory");
        }
        if (mobileNumber == null || !MOBILE_PATTERN.matcher(mobileNumber).matches()) {
            throw new CustomException("OTP_LOGIN_INVALID_MOBILE_NUMBER", "Mobile number must be 10 digits");
        }
    }

    private String generateOtp() {
        // Dev/test only: same code every time, all other rules (expiry, single use, limits) still apply.
        if (fixedOtp != null) {
            return fixedOtp;
        }
        StringBuilder otp = new StringBuilder(otpLength);
        for (int i = 0; i < otpLength; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    private String newTicket() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashOtp(String otp, String subject) {
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(digest(salt, otp, subject));
    }

    private boolean otpMatches(String stored, String otp, String subject) {
        int separator = stored.indexOf(':');
        if (separator < 0) {
            return false;
        }
        byte[] salt = Base64.getDecoder().decode(stored.substring(0, separator));
        byte[] expected = Base64.getDecoder().decode(stored.substring(separator + 1));
        return MessageDigest.isEqual(expected, digest(salt, otp, subject));
    }

    private static byte[] digest(byte[] salt, String otp, String subject) {
        MessageDigest sha256 = sha256();
        sha256.update(salt);
        sha256.update((subject + ":" + otp).getBytes(StandardCharsets.UTF_8));
        return sha256.digest();
    }

    /** Opaque per-number identifier, so phone numbers are not written in clear in Redis keys. */
    private static String subject(String tenantId, String mobileNumber) {
        byte[] hash = sha256().digest((tenantId + "|" + mobileNumber).getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }

    private static String key(String kind, String id) {
        return KEY_PREFIX + kind + ":" + id;
    }

    private static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
