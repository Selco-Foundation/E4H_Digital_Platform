package org.selco.e4h.web.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WeeklyTheftCaseRow {
    private String stateName;
    private String healthFacilityName;
    private String district;
    private String filedDateFormatted;
    private String statusLabel;
}
