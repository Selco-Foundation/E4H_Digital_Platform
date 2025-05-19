package facility.web.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

/**
 * These are health facility specific attributes. This needs to be converted into a JSON schema and added to MDMS. All facilities of type \&quot;Health\&quot; to be verified against this schema.
 */
@Schema(description = "These are health facility specific attributes. This needs to be converted into a JSON schema and added to MDMS. All facilities of type \"Health\" to be verified against this schema.")
@Validated
@Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2025-05-14T17:15:00.238919256+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
//@NoArgsConstructor
@Builder
public class HealthFacilityDetails {

}
