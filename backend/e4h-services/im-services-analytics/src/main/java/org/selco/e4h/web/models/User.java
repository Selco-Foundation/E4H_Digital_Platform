package org.selco.e4h.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

/**
 * User model for escalation notifications
 * Based on egov-user service structure
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("uuid")
    private String uuid;
    
    @JsonProperty("userName")
    private String userName;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("mobileNumber")
    private String mobileNumber;
    
    @JsonProperty("emailId")
    private String emailId;
    
    @JsonProperty("gender")
    private String gender;
    
    @JsonProperty("active")
    private Boolean active;
    
    @JsonProperty("tenantId")
    private String tenantId;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("roles")
    private List<Role> roles;
}
