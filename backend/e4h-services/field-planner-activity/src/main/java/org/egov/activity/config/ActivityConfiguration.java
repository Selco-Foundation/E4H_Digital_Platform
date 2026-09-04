package org.egov.activity.config;

import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Component
public class ActivityConfiguration {

    @Value("${fieldplan.facility.idgen.id.format}")
    private String fieldPlanFacilityIdFormat;

    @Value("${egov.fieldplan.host}")
    private String fieldPlanServiceHost;

    @Value("${egov.search.fieldplan.url}")
    private String fieldPlanServiceSearchUrl;

    @Value("${egov.search.fieldplan.facility.url}")
    private String fieldPlanFacilityServiceSearchUrl;

    @Value("${egov.facility.host}")
    private String facilityServiceHost;

    @Value("${egov.search.facility.url}")
    private String facilityServiceSearchUrl;

    @Value("${egov.facility.update.url}")
    private String facilityServiceUpdateUrl;

    @Value("${egov.v2.search.facility.url}")
    private String facilityServiceSearchUrlV2;

    @Value("${egov.pdf.host}")
    private String pdfServiceHost;

    @Value("${egov.createnosave.pdf.url}")
    private String pdfCreateNoSaveUrl;

    @Value("${egov.createandsave.pdf.url}")
    private String pdfCreateSaveFilestore;

    @Value("${egov.kafka.notification.email.topic}")
    private String notificationEmailTopic;

    @Value("${egov.off.grid.single.phase.key}")
    private String bomACOffGridSinglePhase;

    @Value("${egov.off.grid.three.phase.key}")
    private String bomACOffGridSThreePhase;

    @Value("${egov.hybrid.single.phase.key}")
    private String bomHybridSinglePhase;

    @Value("${egov.hybrid.three.phase.key}")
    private String bomHybridThreePhase;

    @Value("${egov.dc.system.key}")
    private String bomDCSystem;

    @Value("${egov.on.grid.single.phase.key}")
    private String bomACOnGridSinglePhase;

    @Value("${egov.on.grid.three.phase.key}")
    private String bomACOnGridSThreePhase;

    @Value("${search.api.limit:100}")
    private String searchApiLimit;

    @Value("${global.tenant.id}")
    private String tenantId;

    @Value("${egov.mdms.host}")
    private String mdmsHost;
    @Value("${egov.mdms.search.endpoint}")
    private String mdmsEndPoint;

    @Value("${project.document.id.verification.required}")
    private String documentIdVerificationRequired;

    @Value("${activity.assignment.kafka.create.topic}")
    private String createActivityAssignmentTopic;

    @Value("${activity.assignment.kafka.unassign.topic}")
    private String unassignActivityAssignmentTopic;

    @Value("${activity.facility.kafka.create.topic}")
    private String createActivityFacilityTopic;

    @Value("${egov.ingestion.host}")
    private String ingestionServiceHost;

    @Value("${egov.ingestion.document.append.url}")
    private String ingestionDocumentAppendUrl;

    @Value("${egov.ingestion.document.append.module}")
    private String ingestionDocumentAppendModule;

    /**
     * Max serialized size (bytes) of the ActivityFacility list pushed per Kafka message on
     * {@link #createActivityFacilityTopic}. Sized by bytes rather than item count because each
     * item's additionalDetails carries a BOM template copy whose size varies by systemType - a
     * fixed item count can't guarantee staying under the producer's max.request.size (default
     * 1 MiB) for every template. Default keeps ~50% margin below that default.
     */
    @Value("${activity.facility.create.kafka.batch.max.bytes:500000}")
    private Integer createActivityFacilityBatchMaxBytes;

    @Value("${activity.kafka.create.topic}")
    private String createActivityTopic;

    @Value("${activity.facility.kafka.update.topic}")
    private String updateActivityFacilityTopic;

    @Value("${activity.facility.kafka.delete.topic}")
    private String deleteActivityFacilityTopic;

    @Value("${activity.assignment.kafka.update.topic}")
    private String updateActivityAssignmentTopic;

    @Value("${bom.kafka.create.topic}")
    private String createBOMTopic;

    @Value("${bom.kafka.update.topic}")
    private String updateBOMTopic;

    @Value("${facility.user.kafka.create.topic}")
    private String createFacilityUserTopic;

    @Value("${facility.user.kafka.update.topic}")
    private String updateFacilityUserTopic;

    @Value("${project.search.max.limit}")
    private Integer maxLimit;

    @Value("${project.default.offset}")
    private Integer defaultOffset;

    @Value("${project.default.limit}")
    private Integer defaultLimit;

    @Value("${project.mdms.module}")
    private String mdmsModule;

    @Value("${task.mdms.module}")
    private String taskMdmsModule;

    @Value("${egov.location.hierarchy.type}")
    private String locationHierarchyType;

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

    @Value("${egov.vendor.user.host}")
    private String orgUserHost;

    @Value("${egov.vendor.user.search.url}")
    private String orgUserSearchUrl;

    @Value("${egov.vendor.user.update.url}")
    private String orgUserUpdateUrl;

    @Value("${egov.vendor.organisation.search.url}")
    private String organisationSearchUrl;

    @Value("${facility.management.transaction.kafka.create.topic}")
    private String transactionPersistTopic;

    @Value("${facility.management.comment.kafka.create.topic}")
    private String commentPersistTopic;

    @Value("${egov.asset.host}")
    private String assetHost;

    @Value("${egov.asset.search.url}")
    private String assetSearchUrl;

    @Value("${egov.asset.update.url}")
    private String assetUpdateUrl;

    // Defaulted inline so environments that predate this property still start.
    @Value("${egov.asset.search.limit:1000}")
    private Integer assetSearchLimit;

    @Value("${email.activity.assignment.subject}")
    private String activityEmailSubject;

    @Value("${email.activity.assignment.body}")
    private String activityEmailBody;

    @Value("${egov.amc.scheduler.host}")
    private String amcSchedulerHost;

    @Value("${egov.amc.scheduler.asset.create.url}")
    private String amcAssetCreateUrl;

    @Value("${egov.amc.scheduler.configuration.search.url}")
    private String amcConfigurationSearchUrl;

    @Value("${egov.amc.scheduler.visit.generate.url}")
    private String amcVisitGenerateUrl;

    //Localization
    @Value("${egov.localization.host}")
    private String localizationHost;

    @Value("${egov.localization.context.path}")
    private String localizationContextPath;

    @Value("${egov.localization.search.endpoint}")
    private String localizationSearchEndpoint;

    /**
     * Shared user-analytics topic feeding the user-analytics-report index. Same topic im-services
     * (SEM), health-facility-registry, boundary-service, amc-scheduler-service and project publish
     * to — see ActivityAnalyticsService.
     */
    @Value("${activity.kafka.user.analytics.topic:user-analytics-event}")
    private String userAnalyticsTopic;
}
