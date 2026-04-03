package org.selco.e4h.web.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgeBucketData {
    private int totalLt1Wk;
    private int totalLt1Mo;
    private int totalLt3Mo;
}
