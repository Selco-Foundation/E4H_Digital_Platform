package facility.web.models;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DecryptionRequestWrapper {

    private List<Map<String, EncryptObject>> decryptionRequests;
}
