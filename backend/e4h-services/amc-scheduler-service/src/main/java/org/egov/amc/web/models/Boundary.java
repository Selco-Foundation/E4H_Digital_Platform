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
  @JsonProperty("code")
  private String state = null;

  @JsonProperty("message")
  private String district = null;

  @JsonProperty("message")
  private String block = null;
}
