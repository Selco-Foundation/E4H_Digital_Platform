package org.egov.inbox.web.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Address {
    String id = null;
    private String tenantId = null;
    private String locationAccuracy = null;
    String clientReferenceId = null;
    String doorNo = null;
    private String type = null;
    String addressLine1 = null;
    String addressLine2 = null;
    String landmark = null;
    String city = null;
    String pincode = null;
    Double latitude = null;
    Double longitude = null;
    String buildingName = null;
    String street = null;
    private String boundaryType = null;
    private String boundary = null;
    private Boundary locality = null;
}