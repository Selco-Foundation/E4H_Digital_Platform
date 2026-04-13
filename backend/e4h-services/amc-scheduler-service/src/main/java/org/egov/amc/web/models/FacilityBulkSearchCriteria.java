package org.egov.amc.web.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Body of {@code Facility} in {@code POST /facility-service/v2/facility/_bulk-search}
 * (aligned with facility-service bulk search contract).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacilityBulkSearchCriteria {

    private List<String> tenantIds;
    private List<String> facilityIds;
    private List<String> facilityNames;
    private List<String> hfrIds;
    private List<String> ninIds;
    private List<String> facilityPocNames;
    private List<String> facilityPocPhones;
    private List<String> facilityPocEmails;
    private List<String> facilityStatus;
    private List<String> userIds;
    private List<String> boundaryCodes;
    private List<String> state;
    private List<String> district;
    private List<String> block;
    private Boolean sendNonPaginatedResponse;
    private Integer limit;
    private Integer offset;
    private Boolean isOnmReady;

    public static FacilityBulkSearchCriteria forTenantAndFacilityIds(
            List<String> tenantIds,
            List<String> facilityIdsChunk) {
        int limit = Math.max(facilityIdsChunk.size(), 50);
        return FacilityBulkSearchCriteria.builder()
                .tenantIds(new ArrayList<>(tenantIds))
                .facilityIds(new ArrayList<>(facilityIdsChunk))
                .facilityNames(new ArrayList<>())
                .hfrIds(new ArrayList<>())
                .ninIds(new ArrayList<>())
                .facilityPocNames(new ArrayList<>())
                .facilityPocPhones(new ArrayList<>())
                .facilityPocEmails(new ArrayList<>())
                .facilityStatus(new ArrayList<>())
                .userIds(new ArrayList<>())
                .boundaryCodes(new ArrayList<>())
                .state(new ArrayList<>())
                .district(new ArrayList<>())
                .block(new ArrayList<>())
                .sendNonPaginatedResponse(true)
                .limit(limit)
                .offset(0)
                .isOnmReady(null)
                .build();
    }
}
