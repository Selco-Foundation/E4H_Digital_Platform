package org.egov.im.web.models.imV1;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import org.egov.im.annotation.CharacterConstraint;
import org.egov.im.web.models.User;
import org.hibernate.validator.constraints.SafeHtml;
import org.springframework.validation.annotation.Validated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Instance of Service request raised for a particular service. As per extension propsed in the Service definition \&quot;attributes\&quot; carry the input values requried by metadata definition in the structure as described by the corresponding schema.  * Any one of &#39;address&#39; or &#39;(lat and lang)&#39; or &#39;addressid&#39; is mandatory 
 */
@ApiModel(description = "Instance of Service request raised for a particular service. As per extension propsed in the Service definition \"attributes\" carry the input values requried by metadata definition in the structure as described by the corresponding schema.  * Any one of 'address' or '(lat and lang)' or 'addressid' is mandatory ")
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2020-07-15T11:35:33.568+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Incident   {

    	@NotNull
    	@SafeHtml
    	@JsonProperty("incidentType")
    	private String incidentType = null;
    	
    	@SafeHtml
    	@JsonProperty("incidentSubType")
    	private String incidentSubType = null;
    	
    	@SafeHtml
    	@JsonProperty("incidentId")
    	private String incidentId = null;
    
    	@SafeHtml
    	@JsonProperty("District")
    	private String district = null;
        
    	@SafeHtml
    	@JsonProperty("Block")
    	private String block = null;
    	
    	@SafeHtml
    	@JsonProperty("PhcType")
    	private String phcType = null;
    	
    	@SafeHtml
    	@JsonProperty("PhcSubType")
    	private String phcSubType = null;

        @NotNull
        @SafeHtml
        @JsonProperty("tenantId")
        private String tenantId = null;


        
        @JsonProperty("citizen")
        private User citizen = null;


        @CharacterConstraint(size = 600)
        @JsonProperty("additionalDetail")
        private Object additionalDetail = null;



        @JsonProperty("auditDetails")
        private AuditDetails auditDetails = null;
        
        @SafeHtml
        @JsonProperty("accountId")
        private String accountId = null;

}

