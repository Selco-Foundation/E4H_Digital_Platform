package org.egov.web.models;

import lombok.Data;

@Data
public class UserChangeSet {

    private boolean mobileChanged;
    private boolean nameChanged;
    private boolean usernameChanged;
    private boolean emailChanged;
    private boolean rolesChanged;
    private boolean jurisdictionChanged;

    public boolean shouldUpdateHRMS() {
        return mobileChanged ||
                nameChanged ||
                usernameChanged ||
                emailChanged ||
                rolesChanged ||
                jurisdictionChanged;
    }
}

