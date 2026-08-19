package org.egov.amc.web.models;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacilityAmcSummary {
    private String facilityId;
    private String amcNumber;
    private List<Integer> completedAmcNumbers;
    private List<Integer> lapsedAmcNumbers;
}
