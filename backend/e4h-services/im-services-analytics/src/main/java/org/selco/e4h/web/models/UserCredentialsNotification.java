package org.selco.e4h.web.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * A user's login details to email, published by vendor-registry when it creates a user or an
 * administrator resets their password.
 *
 * Carries a plaintext password: never log one of these whole.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserCredentialsNotification {

    public static final String ACCOUNT_CREATED = "ACCOUNT_CREATED";
    public static final String PASSWORD_RESET = "PASSWORD_RESET";

    /** {@link #ACCOUNT_CREATED} or {@link #PASSWORD_RESET}. */
    private String type;

    private String email;

    private String name;

    private String organisationName;

    private String role;

    private String userName;

    @ToString.Exclude
    private String password;

    public boolean isReset() {
        return PASSWORD_RESET.equalsIgnoreCase(type);
    }
}
