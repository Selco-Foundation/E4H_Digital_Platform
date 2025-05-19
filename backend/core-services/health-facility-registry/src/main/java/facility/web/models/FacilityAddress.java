package facility.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import lombok.*;

@Schema(
        name = "Facility_address",
        description = "Representation of an address. Individual APIs may choose to extend from this using allOf if more details are needed."
)
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
    @Schema(description = "Unique Identifier of the tenant to which user primarily belongs")
    private String tenantId;

    @JsonProperty("latitude")
    @Schema(description = "Latitude of the address")
    private Double latitude;

    @JsonProperty("longitude")
    @Schema(description = "Longitude of the address")
    private Double longitude;

    @JsonProperty("addressId")
    @Schema(description = "System generated ID for the address", accessMode = Schema.AccessMode.READ_ONLY)
    private String addressId;

    @JsonProperty("addressNumber")
    @Schema(description = "House, Door, Building number in the address")
    private String addressNumber;

    @JsonProperty("addressLine1")
    @Schema(description = "Apartment, Block, Street of the address")
    private String addressLine1;

    @JsonProperty("addressLine2")
    @Schema(description = "Locality, Area, Zone, Ward of the address")
    private String addressLine2;

    @JsonProperty("landmark")
    @Schema(description = "Additional landmark to help locate the address")
    private String landmark;

    @JsonProperty("city")
    @Schema(description = "City of the address. Can be represented by the tenantId itself")
    private String city;

    @JsonProperty("pincode")
    @Schema(description = "PIN code of the address. Indian pincodes will usually be all numbers.")
    private String pincode;

    @JsonProperty("detail")
    @Schema(description = "More address detail as may be needed")
    private String detail;

    @JsonProperty("state")
    @Schema(description = "State of the address")
    private String state;

    @JsonProperty("district")
    @Schema(description = "District of the address")
    private String district;

    @JsonProperty("block")
    @Schema(description = "Block of the address")
    private String block;
}
