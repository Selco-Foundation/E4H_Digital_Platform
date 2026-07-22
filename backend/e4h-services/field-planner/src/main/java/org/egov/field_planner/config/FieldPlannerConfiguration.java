package org.egov.field_planner.config;

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
public class FieldPlannerConfiguration {

    @Value("${egov.product.host}")
    private String productHost;

    @Value("${fieldplan.facility.idgen.id.format}")
    private String fieldPlanFacilityIdFormat;

    @Value("${egov.household.host}")
    private String householdServiceHost;

    @Value("${fieldPlan.facility.consumer.bulk.create.topic}")
    private String bulkCreateFieldPlanFacilityTopic;

    @Value("${fieldPlan.facility.consumer.bulk.unassign.topic}")
    private String bulkUnassignFieldPlanFacilityTopic;

    @Value("${fieldPlan.facility.consumer.bulk.update.topic}")
    private String bulkUpdateFieldPlanFacilityTopic;

    @Value("${egov.project.host}")
    private String projectServiceHost;

    @Value("${egov.search.project.url}")
    private String projectServiceSearchUrl;

    @Value("${egov.facility.host}")
    private String facilityServiceHost;

    @Value("${egov.search.facility.url}")
    private String facilityServiceSearchUrl;

    @Value("${egov.v2.search.facility.url}")
    private String facilityServiceSearchUrlV2;

    @Value("${egov.fieldplan.activity.host}")
    private String fieldPlanActivityServiceHost;

    @Value("${egov.fieldplan.activity.search.url}")
    private String fieldPlanActivitySearchUrl;

    @Value("${egov.facility.activity.create.url}")
    private String facilityActivityCreateUrl;

    @Value("${egov.fieldplan.activity.update.url}")
    private String fieldPlanActivityUpdateUrl;

    @Value("${search.api.limit:100}")
    private String searchApiLimit;

    @Value("${egov.mdms.host}")
    private String mdmsHost;
    @Value("${egov.mdms.search.endpoint}")
    private String mdmsEndPoint;
    @Value("${egov.mdms.v2.search.endpoint}")
    private String mdmsV2SearchEndpoint;

    @Value("${project.document.id.verification.required}")
    private String documentIdVerificationRequired;

    @Value("${fieldPlan.management.system.kafka.create.topic}")
    private String saveFieldPlanTopic;

    @Value("${icc.report.kafka.create.topic}")
    private String saveIccTemplate;

    @Value("${egov.filestore.host}")
    private String fileStoreHost;

    @Value("${egov.filestore.module}")
    private String fileStoreModule;

    @Value("${fieldPlan.facility.kafka.create.topic}")
    private String createFieldPlanFacilityTopic;

    @Value("${fieldPlan.facility.kafka.unassign.topic}")
    private String deleteFieldPlanFacilityTopic;

    @Value("${fieldPlan.facility.kafka.update.topic}")
    private String updateFieldPlanFacilityTopic;

    @Value("${fieldPlan.kafka.update.topic}")
    private String updateFieldPlanTopic;

    @Value("${fieldPlan.template.kafka.create.topic}")
    private String createFieldPlanTemplateTopic;

    @Value("${fieldPlan.template.kafka.update.topic}")
    private String updateFieldPlanTemplateTopic;

    @Value("${project.search.max.limit}")
    private Integer maxLimit;

    @Value("${project.default.offset}")
    private Integer defaultOffset;

    @Value("${project.default.limit}")
    private Integer defaultLimit;

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

    @Value("${email.activity.assignment.subject}")
    private String activityEmailSubject;

    @Value("${email.activity.assignment.body}")
    private String activityEmailBody;

    @Value("${egov.kafka.notification.email.topic}")
    private String notificationEmailTopic;

    @Value("${email.activity.assignment.default.password}")
    private String defaultUserPassword;
}
