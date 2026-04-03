package org.egov.project.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.annotation.Generated;
import lombok.*;

@JsonTypeName("Facility_address")
@Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2025-05-14T17:15:00.238919256+05:30[Asia/Kolkata]")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class FacilityAddress {

    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("latitude")
    private Double latitude;

    @JsonProperty("longitude")
    private Double longitude;

    @JsonProperty("addressId")
    private String addressId;

    @JsonProperty("addressNumber")
    private String addressNumber;

    @JsonProperty("addressLine1")
    private String addressLine1;

    @JsonProperty("addressLine2")
    private String addressLine2;

    @JsonProperty("landmark")
    private String landmark;

    @JsonProperty("city")
    private String city;

    @JsonProperty("pincode")
    private String pincode;

    @JsonProperty("detail")
    private String detail;

    @JsonProperty("state")
    private String state;

    @JsonProperty("district")
    private String district;

    @JsonProperty("block")
    private String block;

    @JsonProperty("doorNo")
    private String doorNo;

    @JsonProperty("locationAccuracy")
    private Double locationAccuracy;

    @JsonProperty("type")
    private String type;

    @JsonProperty("buildingName")
    private String buildingName;

    @JsonProperty("street")
    private String street;

    @JsonProperty("localityCode")
    private String localityCode;

}
