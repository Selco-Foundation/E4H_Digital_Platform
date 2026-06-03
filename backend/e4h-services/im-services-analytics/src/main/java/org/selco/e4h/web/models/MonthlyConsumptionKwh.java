package org.selco.e4h.web.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyConsumptionKwh {
    private double solarKwh;
    private double gridKwh;
    private double totalKwh;
    private String source;
}
