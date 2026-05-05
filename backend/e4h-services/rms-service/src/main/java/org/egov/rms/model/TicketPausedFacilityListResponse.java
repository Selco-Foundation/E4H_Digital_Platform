package org.egov.rms.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketPausedFacilityListResponse {

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("totalCount")
    private long totalCount;

    @JsonProperty("pausedFacilities")
    private List<PausedFacilityItem> pausedFacilities;

    @JsonProperty("error")
    private TicketPauseResponse.ErrorPayload error;

    public static TicketPausedFacilityListResponse success(long totalCount, List<PausedFacilityItem> pausedFacilities) {
        return TicketPausedFacilityListResponse.builder()
                .success(true)
                .totalCount(totalCount)
                .pausedFacilities(pausedFacilities)
                .build();
    }

    public static TicketPausedFacilityListResponse error(String code, String message) {
        return TicketPausedFacilityListResponse.builder()
                .success(false)
                .totalCount(0)
                .pausedFacilities(Collections.emptyList())
                .error(TicketPauseResponse.ErrorPayload.builder().code(code).message(message).build())
                .build();
    }
}

