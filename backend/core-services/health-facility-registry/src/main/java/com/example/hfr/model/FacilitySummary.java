package com.example.hfr.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Generated;
import javax.validation.constraints.*;
import java.util.Objects;

/**
 * FacilitySummary
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-04-24T16:00:58.522282987+05:30[Asia/Kolkata]", comments = "Generator version: 7.4.0")
public class FacilitySummary {

    private String summary;

    public FacilitySummary summary(String summary) {
        this.summary = summary;
        return this;
    }

    /**
     * Placeholder summary field
     *
     * @return summary
     */

    @Schema(name = "summary", description = "Placeholder summary field", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("summary")
    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FacilitySummary facilitySummary = (FacilitySummary) o;
        return Objects.equals(this.summary, facilitySummary.summary);
    }

    @Override
    public int hashCode() {
        return Objects.hash(summary);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class FacilitySummary {\n");
        sb.append("    summary: ").append(toIndentedString(summary)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

