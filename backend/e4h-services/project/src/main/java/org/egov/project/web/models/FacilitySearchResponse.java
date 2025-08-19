package org.egov.project.web.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FacilitySearchResponse {
    private List<Facility> facilities;
    private int totalCount;
}
