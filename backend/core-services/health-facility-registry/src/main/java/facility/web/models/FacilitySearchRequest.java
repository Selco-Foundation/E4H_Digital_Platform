package facility.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacilitySearchRequest {
    private String tenantId;
    private String facilityId;
    private String facilityName;
    private String facilityPocName;
    private String facilityPocPhone;
    private String facilityPocEmail;
    private String facilityStatus;
    private String userId;
    private String hfrId;
    private String ninId;
    private String boundaryCode;
    private Integer limit = 10;
    private Integer offset = 0;
    private Boolean isOnmReady;
}
