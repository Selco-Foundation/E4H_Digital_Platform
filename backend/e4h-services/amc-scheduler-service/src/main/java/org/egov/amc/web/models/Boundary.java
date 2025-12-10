package org.egov.amc.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Validated
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Boundary {
  @JsonProperty("state")
  private String state = null;

  @JsonProperty("district")
  private String district = null;

  @JsonProperty("block")
  private String block = null;
}
