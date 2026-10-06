package org.egov.inbox.web.model.V2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.response.ResponseInfo;

import java.util.List;

/**
 * Distinct mapped vendors across the tickets the caller's inbox query matches, meant to populate the
 * vendor filter dropdown.
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MappedVendorResponse {

    @JsonProperty("responseInfo")
    private ResponseInfo responseInfo;

    /** Alphabetically sorted, blank and null vendor names excluded. */
    @JsonProperty("mappedVendors")
    private List<String> mappedVendors;

    /** Number of distinct vendors returned, i.e. the size of the list above. */
    @JsonProperty("totalCount")
    private Integer totalCount;
}
