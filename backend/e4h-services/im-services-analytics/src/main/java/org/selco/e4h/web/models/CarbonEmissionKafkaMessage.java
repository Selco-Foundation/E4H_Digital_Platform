package org.selco.e4h.web.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CarbonEmissionKafkaMessage {
    private String tenantId;
    private Integer month;
    private Integer year;
    private List<String> facilityIds;
}
