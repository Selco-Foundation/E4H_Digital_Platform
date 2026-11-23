package org.egov.util;

import org.egov.tracer.model.CustomException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class PasswordPolicyTest {

    /** The egov-user pattern this class must agree with (egov.user.pwd.pattern). */
    private static final Pattern EGOV_USER_PATTERN = Pattern.compile("((?=.*\\d)(?=.*[^A-Za-z0-9\\s])(?=\\S+$).*$)");

    @ParameterizedTest
    @ValueSource(strings = {"abc1@de", "Abcdef1@", "1234567!", "a1-b2_c3", "abcdefghijklm1#"})
    void acceptsPasswordsMeetingTheRule(String password) {
        assertDoesNotThrow(() -> PasswordPolicy.validate(password));
        assertTrue(EGOV_USER_PATTERN.matcher(password).find(), "egov-user would reject " + password);
    }

    @ParameterizedTest
    @ValueSource(strings = {"abcd@ef", "abcdef1", "ab c1@de", "A b1@Cz", "abc\t1@de"})
    void rejectsPasswordsMissingADigitASpecialOrContainingWhitespace(String password) {
        CustomException e = assertThrows(CustomException.class, () -> PasswordPolicy.validate(password));
        assertEquals("INVALID_PWD_PATTERN", e.getCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "ab1@de", "abcdefghijklm1#x"})
    void rejectsPasswordsOutsideTheLengthBounds(String password) {
        CustomException e = assertThrows(CustomException.class, () -> PasswordPolicy.validate(password));
        assertEquals("INVALID_PWD_LENGTH", e.getCode());
    }

    @Test
    void rejectsNull() {
        assertThrows(CustomException.class, () -> PasswordPolicy.validate(null));
    }

    @ParameterizedTest
    @CsvSource({
            "Ramesh Kumar, 9876543210, Rame@3210",
            "Ram, 9876543210, Ram@3210",
            "Ra, 9876543210, Ra@3210",
            "R, 9876543210, R@43210",
            "रमेश, 9876543210, @543210",
            "'J. o-1 Lee', 9876543210, JoLe@3210",
            "rAMesh, 9876543210, rAMe@3210"
    })
    void derivesTheInitialPasswordFromNameAndMobile(String name, String mobile, String expected) {
        String derived = PasswordPolicy.derive(name, mobile);
        assertEquals(expected, derived);
        assertTrue(EGOV_USER_PATTERN.matcher(derived).find());
    }

    @Test
    void derivationFailsLoudlyWhenTheMobileIsTooShort() {
        CustomException e = assertThrows(CustomException.class, () -> PasswordPolicy.derive("R", "1234"));
        assertEquals("PWD_DERIVATION_FAILED", e.getCode());
    }
}
