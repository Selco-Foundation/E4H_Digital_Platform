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
public class StateDailyBreachSection {
    private String stateName;

    @Builder.Default
    private List<ActorCountRow> statePocBreaches = new ArrayList<>();

    @Builder.Default
    private List<ActorCountRow> vendorBreaches = new ArrayList<>();
}
