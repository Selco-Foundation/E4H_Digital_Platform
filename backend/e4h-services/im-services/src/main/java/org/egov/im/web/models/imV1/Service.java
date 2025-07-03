package org.egov.im.web.models.imV1;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.im.web.models.AuditDetails;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;


/**
 * Instance of Service request raised for a particular service. As per extension propsed in the Service definition \&quot;attributes\&quot; carry the input values requried by metadata definition in the structure as described by the corresponding schema.  * Any one of &#39;address&#39; or &#39;(lat and lang)&#39; or &#39;addressid&#39; is mandatory 
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Service   {
	
  @JsonProperty("citizen")
  @Valid
  private Citizen citizen;
  
  @NotNull
  @JsonProperty("tenantId")
  @Size(min=2,max=25)
  @Pattern(regexp="^[a-zA-Z.]*$")
  private String tenantId;

  @NotNull
  @JsonProperty("issueType")
  @Size(min=2,max=50)
  @Pattern(regexp="^[a-zA-Z0-9._]*$")
  private String issueType;
  

  @JsonProperty("incidentId")
  private String incidentId;

  @JsonProperty("addressId")
  private String addressId;
  
  @JsonProperty("address")
  @Pattern(regexp = "^[a-zA-Z0-9!@#.,/: ()&']*$")
  @Size(max=160)
  private String address;


  @JsonProperty("accountId")
  private String accountId;

  @JsonProperty("firstName")
  @Pattern(regexp="(^[a-zA-Z. ]$)")
  private String firstName;

  @JsonProperty("lastName")
  @Pattern(regexp="(^[a-zA-Z. ]$)")
  private String lastName;

  @JsonProperty("phone")
  @NotEmpty
  @Pattern(regexp="(^$|[0-9]{10})")
  private String phone;

  @JsonProperty("attributes")
  private Object attributes;
  
  
  @JsonProperty("active")
  private Boolean active;

  /**
   * The current status of the service request.
   */
  public enum StatusEnum {
	  
	OPEN("open"),
	
	ASSIGNED("assigned"),
	        
    CLOSED("closed"),
    
    CANCELLED("cancelled"),
    
    REJECTED("rejected"),
    
    REASSIGNREQUESTED("reassignrequested"),
    
    RESOLVED("resolved");    

    private String value;

    StatusEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static StatusEnum fromValue(String text) {
      for (StatusEnum b : StatusEnum.values()) {
        if (String.valueOf(b.value).equalsIgnoreCase(text)) {
          return b;
        }
      }
      return null;
    }
  }

  @JsonProperty("status")
  private StatusEnum status;

  /**
   * source of the complaint - Text, Mobile app, Phone, CSC, WhatsApp
   */
//  public enum SourceEnum {
//    SMS("sms"),
//    
//    EMAIL("email"),
//    
//    IVR("ivr"),
//    
//    MOBILEAPP("mobileapp"),
//    
//    WHATSAPP("whatsapp"),
//    
//    CSC("csc"),
//    
//    WEB("web");
//
//    private String value;
//
//    SourceEnum(String value) {
//      this.value = value;
//    }
//
//    @Override
//    @JsonValue
//    public String toString() {
//      return String.valueOf(value);
//    }
//
//    @JsonCreator
//    public static SourceEnum fromValue(String text) {
//      for (SourceEnum b : SourceEnum.values()) {
//        if (String.valueOf(b.value).equals(text)) {
//          return b;
//        }
//      }
//      return null;
//    }
//  }


  @JsonProperty("expectedTime")
  private Long expectedTime;

  @JsonProperty("feedback")
  @Pattern(regexp = "^[a-zA-Z0-9!@#.,/: ()&']*$")
  private String feedback;


  @JsonProperty("auditDetails")
  private AuditDetails auditDetails;
  
  
  }

