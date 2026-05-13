package facility.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacilitySearchRequest {
    private String tenantId;
    private String facilityId;
    private String facilityCategory;
    private String facilityName;
    private String facilityPocName;
    private String facilityPocPhone;
    private String facilityPocEmail;
    private String facilityPocUsername;
    private String facilityStatus;
    private String userId;
    private String hfrId;
    private String ninId;
    private String boundaryCode;
    private Integer limit = 10;
    private Integer offset = 0;
    private Boolean isOnmReady;
}
