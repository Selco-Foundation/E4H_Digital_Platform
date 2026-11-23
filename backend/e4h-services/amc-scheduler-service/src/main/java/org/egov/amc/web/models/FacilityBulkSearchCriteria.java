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
    /** {@code createdAt} | {@code updatedAt}; facility-service falls back to {@code updatedAt}. */
    private String sortBy;
    /** {@code asc} | {@code desc}; facility-service falls back to {@code desc}. */
    private String sortOrder;
    private Boolean isOnmReady;

    /**
     * One page of every facility in a tenant, for jobs that walk the whole registry.
     *
     * <p>Ordered by creation time ascending rather than facility-service's default of last-updated
     * descending: {@code created_at} never changes, so a facility edited midway through a long walk
     * cannot jump between pages and be skipped or indexed twice. Offset paging over a mutable sort
     * key has no such guarantee.
     */
    public static FacilityBulkSearchCriteria forTenantPage(List<String> tenantIds, int limit, int offset) {
        return FacilityBulkSearchCriteria.builder()
                .tenantIds(new ArrayList<>(tenantIds))
                .facilityIds(new ArrayList<>())
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
                .sendNonPaginatedResponse(false)
                .limit(limit)
                .offset(offset)
                .sortBy("createdAt")
                .sortOrder("asc")
                .isOnmReady(null)
                .build();
    }

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
