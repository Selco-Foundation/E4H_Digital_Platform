package digit.web.models;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.egov.common.contract.response.ResponseInfo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import jakarta.annotation.Generated;

@Validated
@Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2025-04-23T00:00:00.000+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaginatedBoundaryResponse {

    @JsonProperty("ResponseInfo")
    @Valid
    private ResponseInfo responseInfo = null;

    @JsonProperty("Boundary")
    @Valid
    private List<Boundary> boundary = null;

    /** Zero-based page index */
    @JsonProperty("page")
    private Integer page;

    /** Number of items per page */
    @JsonProperty("size")
    private Integer size;

    /** Total number of elements across all pages */
    @JsonProperty("totalElements")
    private Long totalElements;

    /** Total number of pages */
    @JsonProperty("totalPages")
    private Integer totalPages;

    public PaginatedBoundaryResponse addBoundaryItem(Boundary boundaryItem) {
        if (this.boundary == null) {
            this.boundary = new ArrayList<>();
        }
        this.boundary.add(boundaryItem);
        return this;
    }
}
