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

    @Value("${search.api.limit:100}")
    private String searchApiLimit;

    @Value("${egov.mdms.host}")
    private String mdmsHost;
    @Value("${egov.mdms.search.endpoint}")
    private String mdmsEndPoint;

    @Value("${project.document.id.verification.required}")
    private String documentIdVerificationRequired;

    @Value("${fieldPlan.management.system.kafka.create.topic}")
    private String saveFieldPlanTopic;

    @Value("${fieldPlan.facility.kafka.create.topic}")
    private String createFieldPlanFacilityTopic;

    @Value("${fieldPlan.kafka.update.topic}")
    private String updateFieldPlanTopic;

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

    @Value("${project.task.no.resource.validation.status}")
    private List<String> noResourceStatuses;

    @Value("${project.attendance.feature.enabled:true}")
    private Boolean isAttendanceFeatureEnabled;

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

    @Value("${egov.asset.host}")
    private String assetHost;

    @Value("${egov.asset.search.url}")
    private String assetSearchUrl;

    @Value("${egov.asset.update.url}")
    private String assetUpdateUrl;

    @Value("${egov.hrms.host}")
    private String hrmsHost;

    @Value("${egov.hrms.search.url}")
    private String hrmsSearchUrl;
}
