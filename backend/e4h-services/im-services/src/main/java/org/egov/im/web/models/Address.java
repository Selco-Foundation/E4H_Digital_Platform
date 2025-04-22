package org.egov.im.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import org.hibernate.validator.constraints.SafeHtml;
import org.springframework.validation.annotation.Validated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

import jakarta.validation.Valid;

/**
 * Representation of a address. Indiavidual APIs may choose to extend from this using allOf if more details needed to be added in their case. 
 */
@ApiModel(description = "Representation of a address. Indiavidual APIs may choose to extend from this using allOf if more details needed to be added in their case. ")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2020-07-15T11:35:33.568+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Address   {
        @SafeHtml
        @JsonProperty("tenantId")
        private String tenantId = null;


        @SafeHtml
        @JsonProperty("id")
        private String id = null;
        
        @SafeHtml
        @JsonProperty("district")
        private String district = null;

        @SafeHtml
        @JsonProperty("block")
        private String block = null;

        @SafeHtml
        @JsonProperty("phcType")
        private String phcType = null;

        @SafeHtml
        @JsonProperty("phcSubType")
        private String phcSubType = null;


        @JsonProperty("additionDetails")
        private Object additionDetails = null;



}

