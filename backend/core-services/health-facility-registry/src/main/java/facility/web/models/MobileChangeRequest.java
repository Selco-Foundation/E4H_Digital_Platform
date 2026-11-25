package facility.web.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A facility POC user's request to change their registered mobile number, and the CRM decision
 * on it. Mobile numbers are plaintext here; they are encrypted only at rest.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MobileChangeRequest {

    @JsonProperty("id")
    private String id;

    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("userUuid")
    private String userUuid;

    @JsonProperty("userName")
    private String userName;

    @JsonProperty("facilityId")
    private String facilityId;

    @JsonProperty("facilityName")
    private String facilityName;

    @JsonProperty("boundaryCode")
    private String boundaryCode;

    @JsonProperty("currentMobileNumber")
    private String currentMobileNumber;

    @JsonProperty("newMobileNumber")
    private String newMobileNumber;

    @JsonProperty("status")
    private MobileChangeRequestStatus status;

    /** Optional remark by whoever closed the request (e.g. the CRM's rejection reason). */
    @JsonProperty("comment")
    private String comment;

    @JsonProperty("actionBy")
    private String actionBy;

    @JsonProperty("actionTime")
    private Long actionTime;

    @JsonProperty("auditDetails")
    private AuditDetails auditDetails;
}
