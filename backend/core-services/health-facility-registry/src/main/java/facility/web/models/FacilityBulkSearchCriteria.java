package facility.web.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    @Builder.Default
    private Boolean sendNonPaginatedResponse = false;
    @Builder.Default
    private Integer limit = 10;
    @Builder.Default
    private Integer offset = 0;
    private Boolean isOnmReady;
}
