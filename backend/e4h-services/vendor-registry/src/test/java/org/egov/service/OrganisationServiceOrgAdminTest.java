package org.egov.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.request.RequestInfo;
import org.egov.config.Configuration;
import org.egov.kafka.OrganizationProducer;
import org.egov.repository.OrganisationRepository;
import org.egov.tracer.model.CustomException;
import org.egov.util.OrgAdminScopeResolver;
import org.egov.validator.OrganisationServiceValidator;
import org.egov.web.models.ApplicationStatus;
import org.egov.web.models.OrgRequest;
import org.egov.web.models.Organisation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Organisation create/update as seen by an ORG_ADMIN, who is bound to one organisation. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrganisationServiceOrgAdminTest {

    private static final String OWN_ORG = "org-1";

    @Mock private OrganisationServiceValidator validator;
    @Mock private OrganisationRepository repository;
    @Mock private OrganisationEnrichmentService enrichmentService;
    @Mock private OrganizationProducer producer;
    @Mock private Configuration configuration;
    @Mock private IndividualService individualService;
    @Mock private NotificationService notificationService;
    @Mock private OrgAdminScopeResolver orgAdminScopeResolver;

    private OrganisationService service;

    @BeforeEach
    void setUp() {
        service = new OrganisationService(validator, repository, enrichmentService, producer, configuration,
                individualService, notificationService, new ObjectMapper(), orgAdminScopeResolver);
        when(orgAdminScopeResolver.resolve(any())).thenReturn(new OrgAdminScopeResolver.OrgScope(OWN_ORG));
        when(repository.getOrganisations(any())).thenReturn(List.of(stored()));
    }

    private static Organisation stored() {
        return Organisation.builder().id(OWN_ORG).tenantId("in").orgType("PLATFORM").orgSubType("SUB")
                .orgStatus(ApplicationStatus.ACTIVE).isActive(true).build();
    }

    private static OrgRequest request(Organisation organisation) {
        return OrgRequest.builder().requestInfo(new RequestInfo()).organisations(List.of(organisation)).build();
    }

    private static OrgRequest createRequest(String... roleCodes) {
        List<org.egov.common.contract.request.Role> roles = java.util.Arrays.stream(roleCodes)
                .map(code -> org.egov.common.contract.request.Role.builder().code(code).build()).toList();
        RequestInfo requestInfo = RequestInfo.builder()
                .userInfo(org.egov.common.contract.request.User.builder().uuid("caller").roles(roles).build())
                .build();
        return OrgRequest.builder().requestInfo(requestInfo)
                .organisations(List.of(Organisation.builder().tenantId("in").build())).build();
    }

    @Test
    void orgAdminCannotCreateOrganisations() {
        CustomException e = assertThrows(CustomException.class,
                () -> service.createOrganisationWithoutWorkFlow(createRequest("ORG_ADMIN")));
        assertEquals("ORG_CREATE_NOT_ALLOWED", e.getCode());
        verifyNoInteractions(producer);
    }

    @Test
    void otherRolesCannotCreateOrganisations() {
        assertThrows(CustomException.class,
                () -> service.createOrganisationWithoutWorkFlow(createRequest("VENDOR_ADMIN", "EMPLOYEE")));
        assertThrows(CustomException.class,
                () -> service.createOrganisationWithoutWorkFlow(createRequest("PLATFORM_ORG_ADMIN")));
        verifyNoInteractions(producer);
    }

    @Test
    void platformAdminCanCreateOrganisations() {
        service.createOrganisationWithoutWorkFlow(createRequest("ORG_PLATFORM_ADMIN"));
        verify(producer).push(any(), any());
    }

    @Test
    void orgAdminCannotUpdateAnotherOrganisation() {
        Organisation other = stored();
        other.setId("org-2");
        CustomException e = assertThrows(CustomException.class,
                () -> service.updateOrganisationWithoutWorkFlow(request(other)));
        assertEquals("INVALID_ORG_ID", e.getCode());
        verifyNoInteractions(producer);
    }

    @Test
    void orgAdminCannotChangeTheOrgType() {
        Organisation update = stored();
        update.setOrgType("VENDOR");
        CustomException e = assertThrows(CustomException.class,
                () -> service.updateOrganisationWithoutWorkFlow(request(update)));
        assertEquals("ORG_FIELDS_NOT_EDITABLE", e.getCode());
    }

    @Test
    void orgAdminCannotDeactivateTheirOrganisation() {
        Organisation update = stored();
        update.setIsActive(false);
        assertThrows(CustomException.class, () -> service.updateOrganisationWithoutWorkFlow(request(update)));
    }

    @Test
    void orgAdminCanUpdateTheirOwnOrganisation() {
        Organisation update = stored();
        update.setName("Renamed");
        update.setOrgStatus(null); // not sent: left as stored
        service.updateOrganisationWithoutWorkFlow(request(update));
        verify(producer).push(any(), any());
    }

    @Test
    void unrestrictedCallersAreNotChecked() {
        when(orgAdminScopeResolver.resolve(any())).thenReturn(OrgAdminScopeResolver.OrgScope.unrestricted());
        Organisation other = stored();
        other.setId("org-2");
        other.setOrgType("VENDOR");
        service.updateOrganisationWithoutWorkFlow(request(other));
        verify(producer).push(any(), any());
    }
}
