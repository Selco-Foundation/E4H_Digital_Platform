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
public class DailySeniorProgramManagerSummary {
    private String recipientName;
    private String stateListLabel;
    private String asOfDate;
    private String dashboardUrl;

    @Builder.Default
    private List<StateDailyBreachSection> stateSections = new ArrayList<>();

    @Builder.Default
    private List<StatePreviouslyOpenRow> previouslyOpenByState = new ArrayList<>();
}
