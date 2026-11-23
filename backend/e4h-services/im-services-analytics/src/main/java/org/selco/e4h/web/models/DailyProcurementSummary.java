package org.selco.e4h.web.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DailyProcurementSummary {
    private String recipientName;
    private String asOfDate;
    private String dashboardUrl;

    @Builder.Default
    private List<StateDailyBreachSection> newBreachesByState = new ArrayList<>();

    @Builder.Default
    private List<VendorStateCountRow> previouslyOpen = new ArrayList<>();
}
