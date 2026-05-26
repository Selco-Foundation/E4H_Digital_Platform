package facility.config;

import lombok.*;
import org.egov.tracer.config.TracerConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;

import java.util.List;

@org.springframework.context.annotation.Configuration
@Data
@Import({TracerConfiguration.class})
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class Configuration {


    // User Config
    @Value("${egov.user.host}")
    private String userHost;

    @Value("${egov.user.context.path}")
    private String userContextPath;

    @Value("${egov.user.create.path}")
    private String userCreateEndpoint;

    @Value("${egov.user.search.path}")
    private String userSearchEndpoint;

    @Value("${egov.user.update.path}")
    private String userUpdateEndpoint;

    @Value("${user.default.password}")
    private String defaultUserPassword;


    //Idgen Config
    @Value("${egov.idgen.host}")
    private String idGenHost;

    @Value("${egov.idgen.path}")
    private String idGenPath;


    //Workflow Config
    @Value("${egov.workflow.host}")
    private String wfHost;

    @Value("${egov.workflow.transition.path}")
    private String wfTransitionPath;

    @Value("${egov.workflow.businessservice.search.path}")
    private String wfBusinessServiceSearchPath;

    @Value("${egov.workflow.processinstance.search.path}")
    private String wfProcessInstanceSearchPath;


    //MDMS
    @Value("${egov.mdms.host}")
    private String mdmsHost;

    @Value("${egov.mdms.search.endpoint}")
    private String mdmsEndPoint;


    //HRMS
    @Value("${egov.hrms.host}")
    private String hrmsHost;

    @Value("${egov.hrms.search.endpoint}")
    private String hrmsSearchEndPoint;

    @Value("${egov.hrms.update.endpoint}")
    private String hrmsUpdateEndPoint;

    @Value("${egov.hrms.create.endpoint}")
    private String hrmsCreateEndPoint;

//    @Value("${egov.hrms.create.endpoint}")
//    private String hrmsCreateEndpoint;

    @Value("${egov.hrms.default.department.code}")
    private String hrmsDefaultDepartmentCode;

    @Value("${egov.hrms.default.designation.code}")
    private String hrmsDefaultDesignationCode;


    //URLShortening
    @Value("${egov.url.shortner.host}")
    private String urlShortnerHost;

    @Value("${egov.url.shortner.endpoint}")
    private String urlShortnerEndpoint;


    //SMSNotification
    @Value("${egov.sms.notification.topic}")
    private String smsNotificationTopic;

    //Enc Service TenantId
    @Value("${enc.service.tenant.id}")
    private String encServiceTenantId;


    //Facility Search
    @Value("${onm-non-ready.allowed.roles}")
    private List<String> onmNonReadyAllowedRoles;

    //Localization
    @Value("${egov.localization.host}")
    private String localizationHost;

    @Value("${egov.localization.context.path}")
    private String localizationContextPath;

    @Value("${egov.localization.upsert.path}")
    private String localizationUpsertPath;

    /**
     * Base URL for im-services (e.g. {@code http://localhost:8880}). When blank, incident boundary sync after facility block update is skipped.
     */
    @Value("${egov.im.services.host:}")
    private String imServicesHost;

    /**
     * Path to POST incident boundary-by-facility update (under im-services context), e.g. {@code /im-services/v2/request/_update-boundary-by-facility}.
     */
    @Value("${egov.im.services.incident.boundary-by-facility.path:/im-services/v2/request/_update-boundary-by-facility}")
    private String imIncidentBoundaryByFacilityUpdatePath;

    @Value("${egov.boundary.hierarchy.type:SELCO}")
    private String boundaryHierarchyType;

    /**
     * Operator endpoint {@code POST /v2/facility/_backfill-boundary-relationships}. Disabled by default.
     */
    @Value("${facility.boundary.backfill.enabled:false}")
    private boolean facilityBoundaryBackfillEnabled;

    @Value("${egov.vendor.host:}")
    private String vendorHost;

    @Value("${egov.vendor.organisation.search.path:/vendor/organisation/v1/_search}")
    private String vendorOrganisationSearchPath;

    @Value("${egov.vendor.organisation.user.search.path:/vendor/organisation/v1/user/_search}")
    private String vendorOrganisationUserSearchPath;
}
