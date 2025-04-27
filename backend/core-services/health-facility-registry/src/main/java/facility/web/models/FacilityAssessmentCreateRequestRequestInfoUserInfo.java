package facility.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This is acting ID token of the authenticated user on the server. Any value provided by the clients will be ignored and actual user based on authtoken will be used on the server.
 */

@Schema(name = "FacilityAssessmentCreateRequest_RequestInfo_userInfo", description = "This is acting ID token of the authenticated user on the server. Any value provided by the clients will be ignored and actual user based on authtoken will be used on the server.")
@JsonTypeName("FacilityAssessmentCreateRequest_RequestInfo_userInfo")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-04-24T16:00:58.522282987+05:30[Asia/Kolkata]", comments = "Generator version: 7.4.0")
public class FacilityAssessmentCreateRequestRequestInfoUserInfo {

    private String tenantId;

    private Integer id;

    private String uuid;

    private String userName;

    private String mobileNumber;

    private String emailId;

    @Valid
    private List<@Valid FacilityAssessmentCreateRequestRequestInfoUserInfoRolesInner> roles = new ArrayList<>();

    private String salutation;

    private String name;

    private String gender;

    private String alternateMobileNumber;

    private String altContactNumber;

    private String pan;

    private String aadhaarNumber;

    private String permanentAddress;

    private String permanentCity;

    private String permanentPincode;

    private String correspondenceCity;

    private String correspondencePincode;

    private String correspondenceAddress;

    private Boolean active;

    private String locale;

    private String type;

    private Boolean accountLocked;

    private Long accountLockedDate;

    private String fatherOrHusbandName;

    private String relationship;

    private String signature;

    private String bloodGroup;

    private String photo;

    private String identificationMark;

    private Long createdBy;

    private String password;

    private String otpReference;

    private Long lastModifiedBy;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate createdDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate lastModifiedDate;

    private Long dob;

    private Long pwdExpiryDate;

    public FacilityAssessmentCreateRequestRequestInfoUserInfo() {
        super();
    }

    /**
     * Constructor with only required parameters
     */
    public FacilityAssessmentCreateRequestRequestInfoUserInfo(String tenantId, String userName, List<@Valid FacilityAssessmentCreateRequestRequestInfoUserInfoRolesInner> roles) {
        this.tenantId = tenantId;
        this.userName = userName;
        this.roles = roles;
    }

    public FacilityAssessmentCreateRequestRequestInfoUserInfo tenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    /**
     * Unique Identifier of the tenant to which user primarily belongs
     *
     * @return tenantId
     */
    @NotNull
    @Schema(name = "tenantId", description = "Unique Identifier of the tenant to which user primarily belongs", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("tenantId")
    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public FacilityAssessmentCreateRequestRequestInfoUserInfo id(Integer id) {
        this.id = id;
        return this;
    }

    /**
     * User id of the authenticated user. Will be deprecated in future
     *
     * @return id
     */

    @Schema(name = "id", description = "User id of the authenticated user. Will be deprecated in future", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public FacilityAssessmentCreateRequestRequestInfoUserInfo uuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    /**
     * UUID of the user
     *
     * @return uuid
     */

    @Schema(name = "uuid", description = "UUID of the user", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("uuid")
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public FacilityAssessmentCreateRequestRequestInfoUserInfo userName(String userName) {
        this.userName = userName;
        return this;
    }

    /**
     * Unique user name of the authenticated user
     *
     * @return userName
     */
    @NotNull
    @Schema(name = "userName", description = "Unique user name of the authenticated user", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("userName")
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public FacilityAssessmentCreateRequestRequestInfoUserInfo mobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
        return this;
    }

    /**
     * mobile number of the autheticated user
     *
     * @return mobileNumber
     */

    @Schema(name = "mobileNumber", description = "mobile number of the autheticated user", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("mobileNumber")
    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public FacilityAssessmentCreateRequestRequestInfoUserInfo emailId(String emailId) {
        this.emailId = emailId;
        return this;
    }

    /**
     * email address of the authenticated user
     *
     * @return emailId
     */

    @Schema(name = "emailId", description = "email address of the authenticated user", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("emailId")
    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public FacilityAssessmentCreateRequestRequestInfoUserInfo roles(List<@Valid FacilityAssessmentCreateRequestRequestInfoUserInfoRolesInner> roles) {
        this.roles = roles;
        return this;
    }

    public FacilityAssessmentCreateRequestRequestInfoUserInfo addRolesItem(FacilityAssessmentCreateRequestRequestInfoUserInfoRolesInner rolesItem) {
        if (this.roles == null) {
            this.roles = new ArrayList<>();
        }
        this.roles.add(rolesItem);
        return this;
    }

    /**
     * List of all the roles
     *
     * @return roles
     */
    @NotNull
    @Valid
    @Schema(name = "roles", description = "List of all the roles", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("roles")
    public List<@Valid FacilityAssessmentCreateRequestRequestInfoUserInfoRolesInner> getRoles() {
        return roles;
    }

    public void setRoles(List<@Valid FacilityAssessmentCreateRequestRequestInfoUserInfoRolesInner> roles) {
        this.roles = roles;
    }

    public FacilityAssessmentCreateRequestRequestInfoUserInfo salutation(String salutation) {
        this.salutation = salutation;
        return this;
    }

    /**
     * Salutation of the authenticated user
     *
     * @return salutation
     */

    @Schema(name = "salutation", description = "Salutation of the authenticated user", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("salutation")
    public String getSalutation() {
        return salutation;
    }

    public void setSalutation(String salutation) {
        this.salutation = salutation;
    }

    public FacilityAssessmentCreateRequestRequestInfoUserInfo name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Name of the authenticated user
     *
     * @return name
     */

    @Schema(name = "name", description = "Name of the authenticated user", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FacilityAssessmentCreateRequestRequestInfoUserInfo gender(String gender) {
        this.gender = gender;
        return this;
    }

    /**
     * Gender of the authenticated user
     *
     * @return gender
     */

    @Schema(name = "gender", description = "Gender of the authenticated user", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("gender")
    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public FacilityAssessmentCreateRequestRequestInfoUserInfo alternateMobileNumber(String alternateMobileNumber) {
        this.alternateMobileNumber = alternateMobileNumber;
        return this;
    }

    /**
     * Alternate mobile number of the authenticated user
     *
     * @return alternateMobileNumber
     */

    @Schema(name = "alternateMobileNumber", description = "Alternate mobile number of the authenticated user", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("alternateMobileNumber")
    public String getAlternateMobileNumber() {
        return alternateMobileNumber;
    }

    public void setAlternateMobileNumber(String alternateMobileNumber) {
        this.alternateMobileNumber = alternateMobileNumber;
    }

    public FacilityAssessmentCreateRequestRequestInfoUserInfo altContactNumber(String altContactNumber) {
        this.altContactNumber = altContactNumber;
        return this;
    }

    /**
     * Alternate Contact number of the authenticated user
     *
     * @return altContactNumber
     */

    @Schema(name = "altContactNumber", description = "Alternate Contact number of the authenticated user", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("altContactNumber")
    public String getAltContactNumber() {
        return altContactNumber;
    }

    public void setAltContactNumber(String altContactNumber) {
        this.altContactNumber = altContactNumber;
    }

    public FacilityAssessmentCreateRequestRequestInfoUserInfo pan(String pan) {
        this.pan = pan;
        return this;
    }

    /**
     * Pan details of the authenticated user
     *
     * @return pan
     */

    @Schema(name = "pan", description = "Pan details of the authenticated user", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("pan")
    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public FacilityAssessmentCreateRequestRequestInfoUserInfo aadhaarNumber(String aadhaarNumber) {
        this.aadhaarNumber = aadhaarNumber;
        return this;
    }

    /**
     * Aadhaar number of the authenticated user
     *
     * @return aadhaarNumber
     */

    @Schema(name = "aadhaarNumber", description = "Aadhaar number of the authenticated user", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("aadhaarNumber")
    public String getAadhaarNumber() {
        return aadhaarNumber;
    }

    public void setAadhaarNumber(String aadhaarNumber) {
        this.aadhaarNumber = aadhaarNumber;
    }

    public FacilityAssessmentCreateRequestRequestInfoUserInfo permanentAddress(String permanentAddress) {
        this.permanentAddress = permanentAddress;
        return this;
    }

    /**
     * Permanent address of the user.
     *
     * @return permanentAddress
     */
    @Size(max = 300)
    @Schema(name = "permanentAddress", description = "Permanent address of the user.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("permanentAddress")
    public String getPermanentAddress() {
        return permanentAddress;
    }

    public void setPermanentAddress(String permanentAddress) {
        this.permanentAddress = permanentAddress;
    }

    public FacilityAssessmentCreateRequestRequestInfoUserInfo permanentCity(String permanentCity) {
        this.permanentCity = permanentCity;
        return this;
    }

    /**
     * City of the permanent address.
     *
     * @return permanentCity
     */
    @Size(max = 300)
    @Schema(name = "permanentCity", description = "City of the permanent address.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("permanentCity")
    public String getPermanentCity() {
        return permanentCity;
    }

    public void setPermanentCity(String permanentCity) {
        this.permanentCity = permanentCity;
    }

    public FacilityAssessmentCreateRequestRequestInfoUserInfo permanentPincode(String permanentPincode) {
        this.permanentPincode = permanentPincode;
        return this;
    }

    /**
     * Get permanentPincode
     *
     * @return permanentPincode
     */
    @Size(max = 6)
    @Schema(name = "permanentPincode", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("permanentPincode")
    public String getPermanentPincode() {
        return permanentPincode;
    }

    public void setPermanentPincode(String permanentPincode) {
        this.permanentPincode = permanentPincode;
    }

    public FacilityAssessmentCreateRequestRequestInfoUserInfo correspondenceCity(String correspondenceCity) {
        this.correspondenceCity = correspondenceCity;
        return this;
    }

    /**
     * City of the correspondence address.
     *
     * @return correspondenceCity
     */
    @Size(max = 50)
    @Schema(name = "correspondenceCity", description = "City of the correspondence address.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("correspondenceCity")
    public String getCorrespondenceCity() {
        return correspondenceCity;
    }

    public void setCorrespondenceCity(String correspondenceCity) {
        this.correspondenceCity = correspondenceCity;
    }

    public FacilityAssessmentCreateRequestRequestInfoUserInfo correspondencePincode(String correspondencePincode) {
        this.correspondencePincode = correspondencePincode;
        return this;
    }

    /**
     * Permanent address pincode.
     *
     * @return correspondencePincode
     */
    @Size(max = 6)
    @Schema(name = "correspondencePincode", description = "Permanent address pincode.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("correspondencePincode")
    public String getCorrespondencePincode() {
        return correspondencePincode;
    }

    public void setCorrespondencePincode(String correspondencePincode) {
        this.correspondencePincode = correspondencePincode;
    }

    public FacilityAssessmentCreateRequestRequestInfoUserInfo correspondenceAddress(String correspondenceAddress) {
        this.correspondenceAddress = correspondenceAddress;
        return this;
    }

    /**
     * Correspondence address of the user.
     *
     * @return correspondenceAddress
     */
    @Size(max = 300)
    @Schema(name = "correspondenceAddress", description = "Correspondence address of the user.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("correspondenceAddress")
    public String getCorrespondenceAddress() {
        return correspondenceAddress;
    }

    public void setCorrespondenceAddress(String correspondenceAddress) {
        this.correspondenceAddress = correspondenceAddress;
    }

    public FacilityAssessmentCreateRequestRequestInfoUserInfo active(Boolean active) {
        this.active = active;
        return this;
    }

    /**
     * True if the user is active and False if the user is inactive.
     *
     * @return active
     */

    @Schema(name = "active", description = "True if the user is active and False if the user is inactive.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("active")
    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public FacilityAssessmentCreateRequestRequestInfoUserInfo locale(String locale) {
        this.locale = locale;
        return this;
    }

    /**
     * Value will be set to \"en_IN\".
     *
     * @return locale
     */
    @Size(max = 10)
    @Schema(name = "locale", description = "Value will be set to \"en_IN\".", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("locale")
    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public FacilityAssessmentCreateRequestRequestInfoUserInfo type(String type) {
        this.type = type;
        return this;
    }

    /**
     * System set value internally. For employee value will be always \"EMPLOYEE\". For citizen value will be \"CITIZEN\".
     *
     * @return type
     */
    @Size(max = 20)
    @Schema(name = "type", description = "System set value internally. For employee value will be always \"EMPLOYEE\". For citizen value will be \"CITIZEN\".", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public FacilityAssessmentCreateRequestRequestInfoUserInfo accountLocked(Boolean accountLocked) {
        this.accountLocked = accountLocked;
        return this;
    }

    /**
     * Set to True if account is locked after several incorrect password
     *
     * @return accountLocked
     */

    @Schema(name = "accountLocked", description = "Set to True if account is locked after several incorrect password", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("accountLocked")
    public Boolean getAccountLocked() {
        return accountLocked;
    }

    public void setAccountLocked(Boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    public FacilityAssessmentCreateRequestRequestInfoUserInfo accountLockedDate(Long accountLockedDate) {
        this.accountLockedDate = accountLockedDate;
        return this;
    }

    /**
     * Date when account is locked after several incorrect password
     *
     * @return accountLockedDate
     */

    @Schema(name = "accountLockedDate", description = "Date when account is locked after several incorrect password", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("accountLockedDate")
    public Long getAccountLockedDate() {
        return accountLockedDate;
    }

    public void setAccountLockedDate(Long accountLockedDate) {
        this.accountLockedDate = accountLockedDate;
    }

    public FacilityAssessmentCreateRequestRequestInfoUserInfo fatherOrHusbandName(String fatherOrHusbandName) {
        this.fatherOrHusbandName = fatherOrHusbandName;
        return this;
    }

    /**
     * Name of user's father or husband.
     *
     * @return fatherOrHusbandName
     */
    @Size(max = 100)
    @Schema(name = "fatherOrHusbandName", description = "Name of user's father or husband.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("fatherOrHusbandName")
    public String getFatherOrHusbandName() {
        return fatherOrHusbandName;
    }

    public void setFatherOrHusbandName(String fatherOrHusbandName) {
        this.fatherOrHusbandName = fatherOrHusbandName;
    }

    public FacilityAssessmentCreateRequestRequestInfoUserInfo relationship(String relationship) {
        this.relationship = relationship;
        return this;
    }

    /**
     * Relationship of user with the guardian. \"FATHER, MOTHER, HUSBAND OR OTHER\"
     *
     * @return relationship
     */
    @Size(max = 20)
    @Schema(name = "relationship", description = "Relationship of user with the guardian. \"FATHER, MOTHER, HUSBAND OR OTHER\"", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("relationship")
    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public FacilityAssessmentCreateRequestRequestInfoUserInfo signature(String signature) {
        this.signature = signature;
        return this;
    }

    /**
     * Image to be loaded for the signature of the employee
     *
     * @return signature
     */

    @Schema(name = "signature", description = "Image to be loaded for the signature of the employee", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("signature")
    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public FacilityAssessmentCreateRequestRequestInfoUserInfo bloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
        return this;
    }

    /**
     * Blood group of the user.
     *
     * @return bloodGroup
     */
    @Size(max = 3)
    @Schema(name = "bloodGroup", description = "Blood group of the user.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("bloodGroup")
    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public FacilityAssessmentCreateRequestRequestInfoUserInfo photo(String photo) {
        this.photo = photo;
        return this;
    }

    /**
     * Image to be loaded for the photo of the user
     *
     * @return photo
     */

    @Schema(name = "photo", description = "Image to be loaded for the photo of the user", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("photo")
    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public FacilityAssessmentCreateRequestRequestInfoUserInfo identificationMark(String identificationMark) {
        this.identificationMark = identificationMark;
        return this;
    }

    /**
     * Any identification mark of user.
     *
     * @return identificationMark
     */

    @Schema(name = "identificationMark", description = "Any identification mark of user.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("identificationMark")
    public String getIdentificationMark() {
        return identificationMark;
    }

    public void setIdentificationMark(String identificationMark) {
        this.identificationMark = identificationMark;
    }

    public FacilityAssessmentCreateRequestRequestInfoUserInfo createdBy(Long createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    /**
     * Id of the user who created the record.
     *
     * @return createdBy
     */

    @Schema(name = "createdBy", description = "Id of the user who created the record.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("createdBy")
    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public FacilityAssessmentCreateRequestRequestInfoUserInfo password(String password) {
        this.password = password;
        return this;
    }

    /**
     * password of the user.
     *
     * @return password
     */

    @Schema(name = "password", description = "password of the user.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("password")
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public FacilityAssessmentCreateRequestRequestInfoUserInfo otpReference(String otpReference) {
        this.otpReference = otpReference;
        return this;
    }

    /**
     * This is the UUID token that we genarate as part of OTP.
     *
     * @return otpReference
     */

    @Schema(name = "otpReference", description = "This is the UUID token that we genarate as part of OTP.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("otpReference")
    public String getOtpReference() {
        return otpReference;
    }

    public void setOtpReference(String otpReference) {
        this.otpReference = otpReference;
    }

    public FacilityAssessmentCreateRequestRequestInfoUserInfo lastModifiedBy(Long lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
        return this;
    }

    /**
     * Id of the user who last modified the record.
     *
     * @return lastModifiedBy
     */

    @Schema(name = "lastModifiedBy", description = "Id of the user who last modified the record.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("lastModifiedBy")
    public Long getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(Long lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public FacilityAssessmentCreateRequestRequestInfoUserInfo createdDate(LocalDate createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    /**
     * Date on which the user master data was added into the system.
     *
     * @return createdDate
     */
    @Valid
    @Schema(name = "createdDate", description = "Date on which the user master data was added into the system.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("createdDate")
    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public FacilityAssessmentCreateRequestRequestInfoUserInfo lastModifiedDate(LocalDate lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
        return this;
    }

    /**
     * Date on which the user master data was last modified.
     *
     * @return lastModifiedDate
     */
    @Valid
    @Schema(name = "lastModifiedDate", description = "Date on which the user master data was last modified.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("lastModifiedDate")
    public LocalDate getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(LocalDate lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public FacilityAssessmentCreateRequestRequestInfoUserInfo dob(Long dob) {
        this.dob = dob;
        return this;
    }

    /**
     * Date of birth of the User
     *
     * @return dob
     */

    @Schema(name = "dob", description = "Date of birth of the User", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("dob")
    public Long getDob() {
        return dob;
    }

    public void setDob(Long dob) {
        this.dob = dob;
    }

    public FacilityAssessmentCreateRequestRequestInfoUserInfo pwdExpiryDate(Long pwdExpiryDate) {
        this.pwdExpiryDate = pwdExpiryDate;
        return this;
    }

    /**
     * Get pwdExpiryDate
     *
     * @return pwdExpiryDate
     */

    @Schema(name = "pwdExpiryDate", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("pwdExpiryDate")
    public Long getPwdExpiryDate() {
        return pwdExpiryDate;
    }

    public void setPwdExpiryDate(Long pwdExpiryDate) {
        this.pwdExpiryDate = pwdExpiryDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FacilityAssessmentCreateRequestRequestInfoUserInfo facilityAssessmentCreateRequestRequestInfoUserInfo = (FacilityAssessmentCreateRequestRequestInfoUserInfo) o;
        return Objects.equals(this.tenantId, facilityAssessmentCreateRequestRequestInfoUserInfo.tenantId) &&
                Objects.equals(this.id, facilityAssessmentCreateRequestRequestInfoUserInfo.id) &&
                Objects.equals(this.uuid, facilityAssessmentCreateRequestRequestInfoUserInfo.uuid) &&
                Objects.equals(this.userName, facilityAssessmentCreateRequestRequestInfoUserInfo.userName) &&
                Objects.equals(this.mobileNumber, facilityAssessmentCreateRequestRequestInfoUserInfo.mobileNumber) &&
                Objects.equals(this.emailId, facilityAssessmentCreateRequestRequestInfoUserInfo.emailId) &&
                Objects.equals(this.roles, facilityAssessmentCreateRequestRequestInfoUserInfo.roles) &&
                Objects.equals(this.salutation, facilityAssessmentCreateRequestRequestInfoUserInfo.salutation) &&
                Objects.equals(this.name, facilityAssessmentCreateRequestRequestInfoUserInfo.name) &&
                Objects.equals(this.gender, facilityAssessmentCreateRequestRequestInfoUserInfo.gender) &&
                Objects.equals(this.alternateMobileNumber, facilityAssessmentCreateRequestRequestInfoUserInfo.alternateMobileNumber) &&
                Objects.equals(this.altContactNumber, facilityAssessmentCreateRequestRequestInfoUserInfo.altContactNumber) &&
                Objects.equals(this.pan, facilityAssessmentCreateRequestRequestInfoUserInfo.pan) &&
                Objects.equals(this.aadhaarNumber, facilityAssessmentCreateRequestRequestInfoUserInfo.aadhaarNumber) &&
                Objects.equals(this.permanentAddress, facilityAssessmentCreateRequestRequestInfoUserInfo.permanentAddress) &&
                Objects.equals(this.permanentCity, facilityAssessmentCreateRequestRequestInfoUserInfo.permanentCity) &&
                Objects.equals(this.permanentPincode, facilityAssessmentCreateRequestRequestInfoUserInfo.permanentPincode) &&
                Objects.equals(this.correspondenceCity, facilityAssessmentCreateRequestRequestInfoUserInfo.correspondenceCity) &&
                Objects.equals(this.correspondencePincode, facilityAssessmentCreateRequestRequestInfoUserInfo.correspondencePincode) &&
                Objects.equals(this.correspondenceAddress, facilityAssessmentCreateRequestRequestInfoUserInfo.correspondenceAddress) &&
                Objects.equals(this.active, facilityAssessmentCreateRequestRequestInfoUserInfo.active) &&
                Objects.equals(this.locale, facilityAssessmentCreateRequestRequestInfoUserInfo.locale) &&
                Objects.equals(this.type, facilityAssessmentCreateRequestRequestInfoUserInfo.type) &&
                Objects.equals(this.accountLocked, facilityAssessmentCreateRequestRequestInfoUserInfo.accountLocked) &&
                Objects.equals(this.accountLockedDate, facilityAssessmentCreateRequestRequestInfoUserInfo.accountLockedDate) &&
                Objects.equals(this.fatherOrHusbandName, facilityAssessmentCreateRequestRequestInfoUserInfo.fatherOrHusbandName) &&
                Objects.equals(this.relationship, facilityAssessmentCreateRequestRequestInfoUserInfo.relationship) &&
                Objects.equals(this.signature, facilityAssessmentCreateRequestRequestInfoUserInfo.signature) &&
                Objects.equals(this.bloodGroup, facilityAssessmentCreateRequestRequestInfoUserInfo.bloodGroup) &&
                Objects.equals(this.photo, facilityAssessmentCreateRequestRequestInfoUserInfo.photo) &&
                Objects.equals(this.identificationMark, facilityAssessmentCreateRequestRequestInfoUserInfo.identificationMark) &&
                Objects.equals(this.createdBy, facilityAssessmentCreateRequestRequestInfoUserInfo.createdBy) &&
                Objects.equals(this.password, facilityAssessmentCreateRequestRequestInfoUserInfo.password) &&
                Objects.equals(this.otpReference, facilityAssessmentCreateRequestRequestInfoUserInfo.otpReference) &&
                Objects.equals(this.lastModifiedBy, facilityAssessmentCreateRequestRequestInfoUserInfo.lastModifiedBy) &&
                Objects.equals(this.createdDate, facilityAssessmentCreateRequestRequestInfoUserInfo.createdDate) &&
                Objects.equals(this.lastModifiedDate, facilityAssessmentCreateRequestRequestInfoUserInfo.lastModifiedDate) &&
                Objects.equals(this.dob, facilityAssessmentCreateRequestRequestInfoUserInfo.dob) &&
                Objects.equals(this.pwdExpiryDate, facilityAssessmentCreateRequestRequestInfoUserInfo.pwdExpiryDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantId, id, uuid, userName, mobileNumber, emailId, roles, salutation, name, gender, alternateMobileNumber, altContactNumber, pan, aadhaarNumber, permanentAddress, permanentCity, permanentPincode, correspondenceCity, correspondencePincode, correspondenceAddress, active, locale, type, accountLocked, accountLockedDate, fatherOrHusbandName, relationship, signature, bloodGroup, photo, identificationMark, createdBy, password, otpReference, lastModifiedBy, createdDate, lastModifiedDate, dob, pwdExpiryDate);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class FacilityAssessmentCreateRequestRequestInfoUserInfo {\n");
        sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    uuid: ").append(toIndentedString(uuid)).append("\n");
        sb.append("    userName: ").append(toIndentedString(userName)).append("\n");
        sb.append("    mobileNumber: ").append(toIndentedString(mobileNumber)).append("\n");
        sb.append("    emailId: ").append(toIndentedString(emailId)).append("\n");
        sb.append("    roles: ").append(toIndentedString(roles)).append("\n");
        sb.append("    salutation: ").append(toIndentedString(salutation)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    gender: ").append(toIndentedString(gender)).append("\n");
        sb.append("    alternateMobileNumber: ").append(toIndentedString(alternateMobileNumber)).append("\n");
        sb.append("    altContactNumber: ").append(toIndentedString(altContactNumber)).append("\n");
        sb.append("    pan: ").append(toIndentedString(pan)).append("\n");
        sb.append("    aadhaarNumber: ").append(toIndentedString(aadhaarNumber)).append("\n");
        sb.append("    permanentAddress: ").append(toIndentedString(permanentAddress)).append("\n");
        sb.append("    permanentCity: ").append(toIndentedString(permanentCity)).append("\n");
        sb.append("    permanentPincode: ").append(toIndentedString(permanentPincode)).append("\n");
        sb.append("    correspondenceCity: ").append(toIndentedString(correspondenceCity)).append("\n");
        sb.append("    correspondencePincode: ").append(toIndentedString(correspondencePincode)).append("\n");
        sb.append("    correspondenceAddress: ").append(toIndentedString(correspondenceAddress)).append("\n");
        sb.append("    active: ").append(toIndentedString(active)).append("\n");
        sb.append("    locale: ").append(toIndentedString(locale)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    accountLocked: ").append(toIndentedString(accountLocked)).append("\n");
        sb.append("    accountLockedDate: ").append(toIndentedString(accountLockedDate)).append("\n");
        sb.append("    fatherOrHusbandName: ").append(toIndentedString(fatherOrHusbandName)).append("\n");
        sb.append("    relationship: ").append(toIndentedString(relationship)).append("\n");
        sb.append("    signature: ").append(toIndentedString(signature)).append("\n");
        sb.append("    bloodGroup: ").append(toIndentedString(bloodGroup)).append("\n");
        sb.append("    photo: ").append(toIndentedString(photo)).append("\n");
        sb.append("    identificationMark: ").append(toIndentedString(identificationMark)).append("\n");
        sb.append("    createdBy: ").append(toIndentedString(createdBy)).append("\n");
        sb.append("    password: ").append(toIndentedString(password)).append("\n");
        sb.append("    otpReference: ").append(toIndentedString(otpReference)).append("\n");
        sb.append("    lastModifiedBy: ").append(toIndentedString(lastModifiedBy)).append("\n");
        sb.append("    createdDate: ").append(toIndentedString(createdDate)).append("\n");
        sb.append("    lastModifiedDate: ").append(toIndentedString(lastModifiedDate)).append("\n");
        sb.append("    dob: ").append(toIndentedString(dob)).append("\n");
        sb.append("    pwdExpiryDate: ").append(toIndentedString(pwdExpiryDate)).append("\n");
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

