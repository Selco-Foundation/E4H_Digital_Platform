package org.egov.asset.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

/**
 * This describes the master schema needed for an inverter asset. To be created in MDMS and validated against
 */
@Schema(description = "This describes the master schema needed for an inverter asset. To be created in MDMS and validated against")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2025-05-05T14:19:51.673231117+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InverterDetails {
    @JsonProperty("currentUnit")
    private String currentUnit;

    @JsonProperty("voltageUnit")
    private String voltageUnit;

    @JsonProperty("chargeControllerCurrent")
    private Double chargeControllerCurrent;

    @JsonProperty("chargeControllerVoltage")
    private Double chargeControllerVoltage;

    @JsonProperty("totalCapacity")
    private Double totalCapacity;

    @JsonProperty("totalCapacityUOM")
    private String totalCapacityUOM;

    @JsonProperty("inverterCapacity")
    private String invertorCapacity;

    @JsonProperty("invertorCapacityUnit")
    private String invertorCapacityUnit;

    @JsonProperty("outputPhase")
    private String outputPhase;

}
