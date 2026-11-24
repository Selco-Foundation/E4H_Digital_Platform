package facility.util;

import org.egov.tracer.model.CustomException;

import java.util.regex.Pattern;

/**
 * The password rule for the facility PoC's HRMS user.
 *
 * <p>A copy of vendor-registry's {@code org.egov.util.PasswordPolicy}: there is no shared library,
 * and both must derive the same password from the same name and mobile so support can reconstruct
 * either. {@code PasswordPolicyTest} pins the derivation to vendor-registry's cases; change both
 * together.
 *
 * <p>egov-user applies its own policy ({@code egov.user.pwd.pattern}) and cannot be bypassed from
 * here, so this must stay identical to it. "Special character" means any non-alphanumeric,
 * non-whitespace character.
 */
public final class PasswordPolicy {

    public static final int MIN_LENGTH = 7;
    public static final int MAX_LENGTH = 15;

    private static final Pattern DIGIT = Pattern.compile("\\d");
    private static final Pattern SPECIAL = Pattern.compile("[^A-Za-z0-9\\s]");
    private static final Pattern WHITESPACE = Pattern.compile("\\s");

    private static final int DERIVED_LETTERS = 4;
    private static final int DERIVED_MIN_DIGITS = 4;

    private PasswordPolicy() {
    }

    /**
     * The first 4 ASCII letters of the name, {@code @}, then the last {@code max(4, 6 - letters)}
     * digits of the mobile number. {@code Ramesh Kumar} / {@code 9876543210} gives {@code Rame@3210}.
     *
     * <p>Derived rather than random so support can reconstruct it. It is therefore not a secret:
     * anyone who knows the PoC's name and mobile number can compute it.
     */
    public static String derive(String name, String mobileNumber) {
        String letters = (name == null ? "" : name.replaceAll("[^A-Za-z]", ""));
        letters = letters.substring(0, Math.min(DERIVED_LETTERS, letters.length()));
        String digits = (mobileNumber == null ? "" : mobileNumber.replaceAll("\\D", ""));
        int digitCount = Math.max(DERIVED_MIN_DIGITS, MIN_LENGTH - 1 - letters.length());
        if (digits.length() < digitCount) {
            throw new CustomException("PWD_DERIVATION_FAILED",
                    "Mobile number has too few digits to derive an initial password");
        }
        String password = letters + "@" + digits.substring(digits.length() - digitCount);
        // Asserted rather than assumed: the policy is config on the egov-user side and can move.
        validate(password);
        return password;
    }

    public static void validate(String password) {
        if (password == null || password.length() < MIN_LENGTH || password.length() > MAX_LENGTH) {
            throw new CustomException("INVALID_PWD_LENGTH",
                    "Password must be between " + MIN_LENGTH + " and " + MAX_LENGTH + " characters");
        }
        if (WHITESPACE.matcher(password).find()
                || !DIGIT.matcher(password).find()
                || !SPECIAL.matcher(password).find()) {
            throw new CustomException("INVALID_PWD_PATTERN",
                    "Password must contain at least one digit and one special character, and no spaces");
        }
    }
}
