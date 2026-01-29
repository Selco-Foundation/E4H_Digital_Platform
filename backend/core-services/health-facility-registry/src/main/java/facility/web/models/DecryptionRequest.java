package facility.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DecryptionRequest {

    @NotNull
    @JsonProperty("decryptionRequests")
    private List<Object> decryptionRequests;

}
