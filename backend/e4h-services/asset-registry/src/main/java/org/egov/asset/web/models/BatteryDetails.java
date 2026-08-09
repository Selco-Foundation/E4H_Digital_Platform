package org.egov.asset.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

/**
 * This describes the master schema needed for a battery. To be created in MDMS and validated against
 */
@Schema(description = "This describes the master schema needed for a battery. To be created in MDMS and validated against")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2025-05-05T14:19:51.673231117+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BatteryDetails {
    @JsonProperty("totalCapacity")
    @NotNull
    private Double totalCapacity = null;

    @JsonProperty("totalCapacityUOM")
    private String totalCapacityUOM = null;

    @JsonProperty("batteryVoltage")
    private Double batteryVoltage = null;

    @JsonProperty("voltageUnit")
    private String voltageUnit = null;

    @JsonProperty("batteryCapacity")
    private Double batteryCapacity;

    @JsonProperty("capacityUnit")
    private String capacityUnit;

    @JsonProperty("batteryType")
    private String batteryType;

}
