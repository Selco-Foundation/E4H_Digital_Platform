package org.egov.amc.config;

import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Component
public class AMCServiceConfiguration {

    @Value("${egov.product.host}")
    private String productHost;

    @Value("${egov.household.host}")
    private String householdServiceHost;

    @Value("${visit.management.transaction.kafka.create.topic}")
    private String transactionPersistTopic;

    @Value("${egov.project.host}")
    private String projectServiceHost;

    @Value("${egov.search.project.url}")
    private String projectServiceSearchUrl;

    @Value("${egov.staff.project.create.url}")
    private String projectStaffCreateUrl;

    @Value("${egov.asset.host}")
    private String assetServiceHost;

    @Value("${egov.asset.search.url}")
    private String assetServiceSearchUrl;

    @Value("${egov.otp.host}")
    private String otpServiceHost;

    @Value("${egov.otp.create.url}")
    private String otpServiceCreateUrl;

    @Value("${egov.otp.validate.url}")
    private String otpServiceValidateUrl;

    @Value("${egov.otp.default}")
    private String defaultOtp;

    @Value("${egov.otp.bypass.validation}")
    private boolean byPassOtpValidation;

    @Value("${egov.facility.host}")
    private String facilityServiceHost;

    @Value("${egov.search.facility.url}")
    private String facilityServiceSearchUrl;

    @Value("${egov.v2.search.facility.url}")
    private String facilityServiceSearchUrlV2;

    @Value("${egov.v2.bulk.search.facility.url}")
    private String facilityBulkSearchUrl;

    /**
     * Index-only endpoint on facility-service for a facility's AMC snapshot. Deliberately not the
     * facility {@code _update} API: that would persist the AMC fields into the facility table's
     * additional_details, and AMC data is meant to exist on the search index only.
     */
    @Value("${egov.v2.facility.amc.index.update.url}")
    private String facilityAmcIndexUpdateUrl;

    @Value("${search.api.limit:100}")
    private String searchApiLimit;

    @Value("${egov.mdms.host}")
    private String mdmsHost;

    @Value("${egov.mdms.search.endpoint}")
    private String mdmsEndPoint;

    @Value("${project.document.id.verification.required}")
    private String documentIdVerificationRequired;

    @Value("${amc.configuration.create.topic}")
    private String saveAmcConfigurationTopic;

    @Value("${amc.configuration.update.topic}")
    private String updateAmcConfigurationTopic;

    @Value("${asset.amc.create.topic}")
    private String saveAssetAmcTopic;

    @Value("${asset.amc.update.topic}")
    private String updateAssetAmcTopic;

    @Value("${scheduled.visit.create.topic}")
    private String saveScheduledVisitTopic;

    @Value("${scheduled.visit.update.topic}")
    private String updateScheduledVisitTopic;

    @Value("${scheduled.visit.delete.topic}")
    private String deleteScheduledVisitTopic;

    @Value("${scheduled.visit.index.create.topic}")
    private String saveScheduledVisitIndexTopic;

    @Value("${scheduled.visit.index.update.topic}")
    private String updateScheduledVisitIndexTopic;

    @Value("${project.search.max.limit}")
    private Integer maxLimit;

    @Value("${project.default.offset}")
    private Integer defaultOffset;

    @Value("${project.default.limit}")
    private Integer defaultLimit;

    @Value("${egov.boundary.hierarchy.type}")
    private String boundaryHierarchyType;

    @Value("${egov.user.id.validator}")
    private String egovUserIdValidator;

    @Value("${egov.boundary.host}")
    private String boundaryServiceHost;

    @Value("${egov.boundary.search.url}")
    private String boundarySearchUrl;

    @Value("${egov.workflow.host}")
    private String wfHost;

    @Value("${egov.workflow.transition.path}")
    private String wfTransitionPath;

    @Value("${egov.workflow.search.path}")
    private String wfSearchPath;

    @Value("${egov.workflow.module.name}")
    private String moduleName;

    @Value("${egov.workflow.business.service}")
    private String businessService;

    @Value("${egov.hrms.host}")
    private String hrmsHost;

    @Value("${egov.hrms.search.url}")
    private String hrmsSearchUrl;

    /**
     * HRMS role code identifying an AMC field staff member. Used to pick which of a visit's assignees
     * becomes the mapped vendor on the search index. Configurable because role codes are MDMS data and
     * can differ per environment.
     */
    @Value("${amc.mapped.vendor.role.code}")
    private String mappedVendorRoleCode;

    @Value("${egov.vendor.host}")
    private String vendorHost;

    @Value("${egov.vendor.search.url}")
    private String vendorSearchUrl;

    @Value("${email.activity.assignment.subject}")
    private String activityEmailSubject;

    @Value("${email.activity.assignment.body}")
    private String activityEmailBody;

    @Value("${egov.kafka.notification.email.topic}")
    private String notificationEmailTopic;

    @Value("${email.activity.assignment.default.password}")
    private String defaultUserPassword;

    @Value("${notification.sms.enabled:false}")
    private Boolean smsEnabled;

    @Value("${kafka.topics.notification.sms:egov.core.notification.sms}")
    private String smsNotificationTopic;

    @Value("${amc.otp.sms.message.template:Your OTP for scheduled visit verification is {otp}.}")
    private String otpSmsTemplate;

    // Shared user-analytics topic -> user-analytics-report index (also produced by im-services SEM,
    // health-facility-registry and boundary-service).
    @Value("${amc.kafka.user.analytics.topic:user-analytics-event}")
    private String userAnalyticsTopic;

    @Value("${egov.localization.host:}")
    private String localizationHost;

    @Value("${egov.localization.context.path:}")
    private String localizationContextPath;

    @Value("${egov.localization.search.endpoint:}")
    private String localizationSearchEndpoint;

    public String getFacilityBulkSearchPath() {
        return facilityBulkSearchUrl;
    }
}
