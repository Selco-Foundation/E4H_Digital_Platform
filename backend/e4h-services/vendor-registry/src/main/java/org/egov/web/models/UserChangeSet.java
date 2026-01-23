package org.egov.web.models;

import lombok.Data;

@Data
public class UserChangeSet {

    private boolean mobileChanged;
    private boolean nameChanged;
    private boolean emailChanged;
    private boolean rolesChanged;
    private boolean jurisdictionChanged;

    public boolean shouldUpdateHRMS() {
        return mobileChanged ||
                nameChanged ||
                emailChanged ||
                rolesChanged ||
                jurisdictionChanged;
    }
}

