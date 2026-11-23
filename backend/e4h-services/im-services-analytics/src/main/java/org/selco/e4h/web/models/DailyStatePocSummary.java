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
public class DailyStatePocSummary {
    private String stateName;
    private String asOfDate;
    private String recipientName;
    private String dashboardUrl;

    @Builder.Default
    private List<ActorCountRow> newCrmBreaches = new ArrayList<>();

    @Builder.Default
    private List<ActorCountRow> newTechPocBreaches = new ArrayList<>();

    @Builder.Default
    private List<ActorCountRow> previouslyOpenCrmBreaches = new ArrayList<>();

    @Builder.Default
    private List<ActorCountRow> previouslyOpenTechPocBreaches = new ArrayList<>();
}
