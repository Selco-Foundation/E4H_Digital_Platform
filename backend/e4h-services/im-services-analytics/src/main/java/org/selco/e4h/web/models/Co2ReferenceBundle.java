package org.selco.e4h.web.models;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
@Builder
public class Co2ReferenceBundle {
  private Map<String, BigDecimal> gridIntensityByFy;
  private Map<String, String> archetypeByStateType;
  private Map<String, ArchetypeProps> propertiesByArchetype;
  private Map<String, BigDecimal> sunshineByState;

  @Data
  @Builder
  public static class ArchetypeProps {
    private BigDecimal yearOneAnnualConsumptionKwh;
    private BigDecimal alpha;
  }

  public Optional<String> resolveArchetype(String state, String facilityType) {
    String key = normalize(state) + "|" + normalize(facilityType);
    return Optional.ofNullable(archetypeByStateType.get(key));
  }

  public ArchetypeProps getArchetypeProperties(String archetype) {
    return propertiesByArchetype.get(archetype);
  }

  public BigDecimal resolveGridIntensity(String financialYear) {
    return gridIntensityByFy.get(financialYear);
  }

  /** Returns sunshine hours per day for a state code (for example, {@code India_Assam}). */
  public BigDecimal sunshineHoursForState(String state) {
    if (sunshineByState == null || state == null) {
      return null;
    }
    return sunshineByState.get(normalize(state));
  }

  private static String normalize(String s) {
    return s == null ? "" : s.trim();
  }

  @SuppressWarnings("unchecked")
  public static Co2ReferenceBundle fromRmsResponse(Map<String, Object> body) {
    Map<String, BigDecimal> gif = new HashMap<>();
    List<Map<String, Object>> gifRows = (List<Map<String, Object>>) body.get("gridIntensityFactors");
    if (gifRows != null) {
      for (Map<String, Object> row : gifRows) {
        String fy = (String) row.get("financialYear");
        BigDecimal published = toBigDecimal(row.get("gridIntensityFactor"));
        BigDecimal projected = toBigDecimal(row.get("projectedGridIntensityFactor"));
        if (fy != null) {
          gif.put(fy, published != null ? published : projected);
        }
      }
    }
    Map<String, String> lookup = new HashMap<>();
    List<Map<String, Object>> lookupRows = (List<Map<String, Object>>) body.get("archetypeLookups");
    if (lookupRows != null) {
      for (Map<String, Object> row : lookupRows) {
        lookup.put(normalize((String) row.get("state")) + "|" + normalize((String) row.get("facilityType")),
            (String) row.get("archetype"));
      }
    }
    Map<String, ArchetypeProps> props = new HashMap<>();
    List<Map<String, Object>> propRows = (List<Map<String, Object>>) body.get("archetypeProperties");
    if (propRows != null) {
      for (Map<String, Object> row : propRows) {
        props.put((String) row.get("archetype"), ArchetypeProps.builder()
            .yearOneAnnualConsumptionKwh(toBigDecimal(row.get("yearOneAnnualConsumptionKwh")))
            .alpha(toBigDecimal(row.get("alpha")))
            .build());
      }
    }
    Map<String, BigDecimal> sunshine = new HashMap<>();
    List<Map<String, Object>> sunRows = (List<Map<String, Object>>) body.get("stateSunshineHours");
    if (sunRows != null) {
      for (Map<String, Object> row : sunRows) {
        sunshine.put(normalize((String) row.get("state")), toBigDecimal(row.get("sunshineHoursPerDay")));
      }
    }
    return Co2ReferenceBundle.builder()
        .gridIntensityByFy(gif)
        .archetypeByStateType(lookup)
        .propertiesByArchetype(props)
        .sunshineByState(sunshine)
        .build();
  }

  private static BigDecimal toBigDecimal(Object o) {
    if (o == null) {
      return null;
    }
    if (o instanceof Number) {
      return BigDecimal.valueOf(((Number) o).doubleValue());
    }
    return new BigDecimal(o.toString());
  }
}
