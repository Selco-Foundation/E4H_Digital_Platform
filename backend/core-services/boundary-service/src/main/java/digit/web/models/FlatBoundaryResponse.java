package digit.web.models;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlatBoundaryResponse {
    private String country;
    private String state;
    private String district;
    private String block;
    private String code; // leaf level boundary code
}
