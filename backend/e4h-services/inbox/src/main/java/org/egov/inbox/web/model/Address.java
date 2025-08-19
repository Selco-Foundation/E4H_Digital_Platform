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
    private String id = null;
    private String tenantId = null;
    private String locationAccuracy = null;
    private String clientReferenceId = null;
    private String doorNo = null;
    private String type = null;
    private String addressLine1 = null;
    private String addressLine2 = null;
    private String landmark = null;
    private String city = null;
    private String pincode = null;
    private Double latitude = null;
    private Double longitude = null;
    private String buildingName = null;
    private String street = null;
    private String boundaryType = null;
    private String boundary = null;
    private Boundary locality = null;
}