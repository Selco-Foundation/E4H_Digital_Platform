package org.egov.project.web.models;

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
public class BoundaryV2 {
  @JsonProperty("code")
  private String state = null;

  @JsonProperty("message")
  private String district = null;
}
