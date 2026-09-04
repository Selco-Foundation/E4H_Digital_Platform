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
public class WeeklyTicketOverview {
    private int raisedThisWeek;
    private int resolvedWithinSla;
    private int resolvedAfterBreach;
    private int carriedForward;
}
