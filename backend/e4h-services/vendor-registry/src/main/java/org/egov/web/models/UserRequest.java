package org.egov.web.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.egov.common.contract.user.enums.BloodGroup;
import org.hibernate.validator.constraints.Email;

import javax.validation.constraints.Size;
import java.util.*;
import java.util.stream.Collectors;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {

    private Long id;

    @Size(max = 64)
    private String userName;

    @Size(max = 5)
    private String salutation;

    @Size(max = 50)
    private String name;

    @Size(max = 15)
    private String gender;

    private String mobileNumber;
    
    private String alternatemobilenumber;

    @Email
    @Size(max = 128)
    private String emailId;

    @Size(max = 50)
    private String altContactNumber;

    @Size(max = 10)
    private String pan;


    @Size(max = 20)
    private String aadhaarNumber;


    @Size(max = 300)
    private String permanentAddress;


    @Size(max = 50)
    private String permanentCity;


    @Size(max = 10)
    private String permanentPinCode;


    @Size(max = 300)
    private String correspondenceAddress;

    @Size(max = 50)
    private String correspondenceCity;

    @Size(max = 10)
    private String correspondencePinCode;
    private Boolean active;


    @Size(max = 16)
    private String locale;

    private Boolean accountLocked;
    private Long accountLockedDate;

    @Size(max = 50)
    private String fatherOrHusbandName;


    @Size(max = 36)
    private String signature;


    @Size(max = 32)
    private String bloodGroup;


    @Size(max = 36)
    private String photo;


    @Size(max = 300)
    private String identificationMark;

    private Long createdBy;

    @Size(max = 64)
    private String password;


    private String otpReference;
    private Long lastModifiedBy;

    @Size(max = 50)
    private String tenantId;

    private Set<RoleRequest> roles;


    @Size(max = 36)
    private String uuid;


    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private Date createdDate;
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private Date lastModifiedDate;
    @JsonFormat(pattern = "dd/MM/yyyy")
    private Date dob;
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private Date pwdExpiryDate;
}
