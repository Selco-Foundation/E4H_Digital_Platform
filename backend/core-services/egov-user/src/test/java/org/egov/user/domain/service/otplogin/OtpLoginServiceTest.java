package org.egov.user.domain.service.otplogin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.user.domain.model.Role;
import org.egov.user.domain.model.User;
import org.egov.user.domain.model.enums.UserType;
import org.egov.user.domain.service.UserService;
import org.egov.user.domain.service.utils.NotificationUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class OtpLoginServiceTest {

    private static final String TENANT = "rj";
    private static final String MOBILE = "9876543210";

    private FakeStore store;
    private UserService userService;
    private NotificationUtil notificationUtil;
    private OtpLoginService service;

    private final User facilityA = employee("uuid-a", "FAC_A_USER");
    private final User facilityB = employee("uuid-b", "FAC_B_USER");

    @Before
    public void setUp() {
        store = new FakeStore();
        userService = mock(UserService.class);
        notificationUtil = mock(NotificationUtil.class);
        service = new OtpLoginService(store, userService, notificationUtil,
                true, 6, 300, 30, 3, 5, 15, 120);
    }

    @Test
    public void unknownNumberGetsNoSmsAndNoError() {
        when(userService.findActiveUsersByMobileNumber(MOBILE, TENANT, UserType.EMPLOYEE))
                .thenReturn(Collections.emptyList());

        service.sendOtp(TENANT, MOBILE, new RequestInfo());

        verify(notificationUtil, never()).sendOtpLoginSms(anyString(), anyString(), anyInt(), anyString(), any());
    }

    @Test
    public void singleAccountLogsInWithTicketOnce() {
        mapNumberTo(facilityA);
        String otp = sendAndCaptureOtp();
        assertEquals(6, otp.length());

        OtpLoginVerification verification = service.verifyOtp(TENANT, MOBILE, otp);

        assertNotNull(verification.getLoginTicket());
        assertEquals(1, verification.getAccounts().size());
        assertEquals("FAC_A_USER", verification.getAccounts().get(0).getUserName());
        assertTrue(service.consumeLoginTicket(verification.getLoginTicket(), facilityA, TENANT));
        assertFalse("ticket is single use", service.consumeLoginTicket(verification.getLoginTicket(), facilityA, TENANT));
    }

    @Test
    public void sharedNumberListsAccountsAndTicketOnlyWorksForThem() {
        mapNumberTo(facilityA, facilityB);
        String otp = sendAndCaptureOtp();

        OtpLoginVerification verification = service.verifyOtp(TENANT, MOBILE, otp);
        String ticket = verification.getLoginTicket();

        assertEquals(2, verification.getAccounts().size());
        User stranger = employee("uuid-x", "OTHER");
        assertFalse(service.consumeLoginTicket(ticket, stranger, TENANT));
        assertFalse("wrong tenant", service.consumeLoginTicket(ticket, facilityB, "other"));
        assertTrue("ticket kept after a mismatch", service.consumeLoginTicket(ticket, facilityB, TENANT));
        assertFalse(service.consumeLoginTicket(ticket, facilityA, TENANT));
    }

    @Test
    public void ticketRejectedForCitizen() {
        User citizen = employee("uuid-a", "FAC_A_USER");
        citizen.setType(UserType.CITIZEN);
        mapNumberTo(facilityA);
        OtpLoginVerification verification = service.verifyOtp(TENANT, MOBILE, sendAndCaptureOtp());

        assertFalse(service.consumeLoginTicket(verification.getLoginTicket(), citizen, TENANT));
    }

    @Test
    public void otpIsSingleUse() {
        mapNumberTo(facilityA);
        String otp = sendAndCaptureOtp();
        service.verifyOtp(TENANT, MOBILE, otp);

        expectError("OTP_LOGIN_OTP_EXPIRED", () -> service.verifyOtp(TENANT, MOBILE, otp));
    }

    @Test
    public void otpExpiresAfterFiveMinutes() {
        mapNumberTo(facilityA);
        String otp = sendAndCaptureOtp();
        store.advanceSeconds(301);

        expectError("OTP_LOGIN_OTP_EXPIRED", () -> service.verifyOtp(TENANT, MOBILE, otp));
    }

    @Test
    public void resendWaitsThirtySecondsAndIsLimitedToThree() {
        mapNumberTo(facilityA);
        service.sendOtp(TENANT, MOBILE, null);
        expectError("OTP_LOGIN_RESEND_TOO_SOON", () -> service.sendOtp(TENANT, MOBILE, null));

        for (int resend = 1; resend <= 3; resend++) {
            store.advanceSeconds(31);
            service.sendOtp(TENANT, MOBILE, null);
        }
        store.advanceSeconds(31);
        expectError("OTP_LOGIN_RESEND_LIMIT_REACHED", () -> service.sendOtp(TENANT, MOBILE, null));

        store.advanceSeconds(15 * 60);
        service.sendOtp(TENANT, MOBILE, null);
    }

    @Test
    public void fiveWrongAttemptsBlockOtpLoginForFifteenMinutes() {
        mapNumberTo(facilityA);
        String otp = sendAndCaptureOtp();
        String wrong = otp.equals("000000") ? "111111" : "000000";

        for (int i = 0; i < 4; i++) {
            expectError("OTP_LOGIN_INVALID_OTP", () -> service.verifyOtp(TENANT, MOBILE, wrong));
        }
        expectError("OTP_LOGIN_BLOCKED", () -> service.verifyOtp(TENANT, MOBILE, wrong));
        expectError("OTP_LOGIN_BLOCKED", () -> service.verifyOtp(TENANT, MOBILE, otp));
        store.advanceSeconds(31);
        expectError("OTP_LOGIN_BLOCKED", () -> service.sendOtp(TENANT, MOBILE, null));

        store.advanceSeconds(15 * 60);
        service.verifyOtp(TENANT, MOBILE, sendAndCaptureOtp());
    }

    @Test
    public void disabledFeatureRejectsEverything() {
        service = new OtpLoginService(store, userService, notificationUtil, false, 6, 300, 30, 3, 5, 15, 120);

        expectError("OTP_LOGIN_DISABLED", () -> service.sendOtp(TENANT, MOBILE, null));
        assertFalse(service.consumeLoginTicket("anything", facilityA, TENANT));
    }

    @Test
    public void invalidMobileNumberRejected() {
        expectError("OTP_LOGIN_INVALID_MOBILE_NUMBER", () -> service.sendOtp(TENANT, "12345", null));
    }

    private void mapNumberTo(User... users) {
        when(userService.findActiveUsersByMobileNumber(MOBILE, TENANT, UserType.EMPLOYEE))
                .thenReturn(Arrays.asList(users));
    }

    private int sends = 0;

    private String sendAndCaptureOtp() {
        service.sendOtp(TENANT, MOBILE, new RequestInfo());
        sends++;
        ArgumentCaptor<String> otp = ArgumentCaptor.forClass(String.class);
        verify(notificationUtil, org.mockito.Mockito.times(sends))
                .sendOtpLoginSms(eq(MOBILE), otp.capture(), eq(300), any(), any());
        return otp.getValue();
    }

    private static void expectError(String code, Runnable call) {
        try {
            call.run();
            fail("Expected " + code);
        } catch (CustomException e) {
            assertEquals(code, e.getCode());
        }
    }

    private static User employee(String uuid, String userName) {
        return User.builder().uuid(uuid).username(userName).name(userName).tenantId(TENANT)
                .type(UserType.EMPLOYEE).active(true)
                .roles(new HashSet<>(Collections.singletonList(Role.builder().code("FACILITY_USER").build())))
                .build();
    }

    /** In-memory store with a manual clock so expiry can be tested. */
    private static class FakeStore implements OtpLoginStore {
        private final Map<String, String> values = new HashMap<>();
        private final Map<String, Long> expiries = new HashMap<>();
        private long now = 0;

        void advanceSeconds(long seconds) {
            now += seconds * 1000;
        }

        private void evict(String key) {
            Long expiry = expiries.get(key);
            if (expiry != null && expiry <= now) {
                values.remove(key);
                expiries.remove(key);
            }
        }

        @Override
        public String get(String key) {
            evict(key);
            return values.get(key);
        }

        @Override
        public void put(String key, String value, long ttlSeconds) {
            values.put(key, value);
            expiries.put(key, now + ttlSeconds * 1000);
        }

        @Override
        public boolean putIfAbsent(String key, String value, long ttlSeconds) {
            if (get(key) != null) {
                return false;
            }
            put(key, value, ttlSeconds);
            return true;
        }

        @Override
        public long increment(String key, long ttlSeconds) {
            String current = get(key);
            if (current == null) {
                put(key, "1", ttlSeconds);
                return 1;
            }
            long next = Long.parseLong(current) + 1;
            values.put(key, String.valueOf(next));
            return next;
        }

        @Override
        public boolean delete(String key) {
            evict(key);
            expiries.remove(key);
            return values.remove(key) != null;
        }
    }
}
