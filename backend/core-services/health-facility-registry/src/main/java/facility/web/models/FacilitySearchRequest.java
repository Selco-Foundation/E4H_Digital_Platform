package facility.web.models;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacilitySearchRequest {
    private String tenantId;
    private String facilityId;
    private String facilityName;
    private String hfrId;
    private String ninId;
    private String boundaryCode;
    private Integer limit = 10;
    private Integer offset = 0;
    private Boolean isOnmReady;
}
