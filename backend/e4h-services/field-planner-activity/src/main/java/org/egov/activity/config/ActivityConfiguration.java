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

    @Value("${egov.facility.host}")
    private String facilityServiceHost;

    @Value("${egov.search.facility.url}")
    private String facilityServiceSearchUrl;

    @Value("${egov.v2.search.facility.url}")
    private String facilityServiceSearchUrlV2;

    @Value("${egov.pdf.host}")
    private String pdfServiceHost;

    @Value("${egov.createnosave.pdf.url}")
    private String pdfCreateNoSaveUrl;

    @Value("${egov.pdf.key}")
    private String bomKeypdf;

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

    @Value("${activity.assignment.kafka.create.topic}")
    private String createActivityAssignmentTopic;

    @Value("${activity.facility.kafka.create.topic}")
    private String createActivityFacilityTopic;

    @Value("${activity.kafka.update.topic}")
    private String updateActivityFacilityTopic;

    @Value("${bom.kafka.create.topic}")
    private String createBOMTopic;

    @Value("${bom.kafka.update.topic}")
    private String updateBOMTopic;

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
}
