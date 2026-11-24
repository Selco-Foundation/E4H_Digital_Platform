package org.egov.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.request.RequestInfo;
import org.egov.config.Configuration;
import org.egov.kafka.OrganizationProducer;
import org.egov.repository.OrganisationRepository;
import org.egov.repository.OrganisationUserRepository;
import org.egov.repository.OrganisationUserRepository.OrgUserLink;
import org.egov.tracer.model.CustomException;
import org.egov.util.HRMSUtils;
import org.egov.util.JurisdictionScopeResolver;
import org.egov.util.OrgAdminScopeResolver;
import org.egov.validator.OrganisationUserServiceValidator;
import org.egov.web.models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * An ORG_ADMIN edits users of its own organisation, never a platform administrator: editing one
 * (mobile, email, and with MH2-06 the password) would hand over the account.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrganisationUserServiceOrgAdminTest {

    private static final String OWN_ORG = "org-1";
    private static final String ROW = "row-1";
    private static final String TARGET = "target-uuid";

    @Mock private OrganisationUserServiceValidator validator;
    @Mock private OrganisationUserRepository userRepository;
    @Mock private OrganisationUserEnrichmentService enrichmentService;
    @Mock private OrganizationProducer producer;
    @Mock private Configuration configuration;
    @Mock private NotificationService notificationService;
    @Mock private HRMSUtils hrmsUtils;
    @Mock private JurisdictionScopeResolver jurisdictionScopeResolver;
    @Mock private OrgAdminScopeResolver orgAdminScopeResolver;
    @Mock private CredentialsNotificationService credentialsNotificationService;
    @Mock private OrganisationRepository organisationRepository;

    private OrganisationUserService service;

    @BeforeEach
    void setUp() {
        service = new OrganisationUserService(validator, userRepository, enrichmentService, producer, configuration,
                notificationService, hrmsUtils, new ObjectMapper(), jurisdictionScopeResolver, orgAdminScopeResolver,
                credentialsNotificationService, organisationRepository);
        when(orgAdminScopeResolver.resolve(any())).thenReturn(new OrgAdminScopeResolver.OrgScope(OWN_ORG));
        when(userRepository.getOrgUserLink(ROW)).thenReturn(Optional.of(new OrgUserLink(OWN_ORG, TARGET)));
    }

    private void targetHolds(String... roleCodes) {
        List<Role> roles = java.util.Arrays.stream(roleCodes).map(c -> Role.builder().code(c).build()).toList();
        Employee employee = Employee.builder().user(User.builder().uuid(TARGET).roles(roles).build()).build();
        when(hrmsUtils.getUserById(any(), eq(TARGET))).thenReturn(employee);
    }

    private static OrgUserRequest update() {
        return OrgUserRequest.builder().requestInfo(new RequestInfo()).id(ROW).organizationId(OWN_ORG)
                .user(User.builder().userName("target").build()).build();
    }

    private static DeleteOrgUserRequest delete() {
        return DeleteOrgUserRequest.builder().requestInfo(new RequestInfo()).id(ROW).build();
    }

    @Test
    void orgAdminCannotUpdateAPlatformAdmin() {
        targetHolds("EMPLOYEE", "ORG_PLATFORM_ADMIN");
        CustomException e = assertThrows(CustomException.class, () -> service.updateOrgUser(update()));
        assertEquals("USER_NOT_EDITABLE", e.getCode());
        verifyNoInteractions(validator, producer);
    }

    @Test
    void orgAdminCannotDeleteAPlatformAdmin() {
        targetHolds("ORG_PLATFORM_ADMIN");
        assertThrows(CustomException.class, () -> service.deleteUserOrg(delete()));
        verifyNoInteractions(validator, producer);
    }

    @Test
    void orgAdminCanUpdateAPeer() {
        targetHolds("EMPLOYEE", "ORG_ADMIN");
        service.updateOrgUser(update());
        verify(validator).validateUpdateOrgUserRequest(any());
    }

    @Test
    void targetHrmsCannotReturnIsRefused() {
        when(hrmsUtils.getUserById(any(), eq(TARGET))).thenReturn(null);
        assertThrows(CustomException.class, () -> service.updateOrgUser(update()));
        verifyNoInteractions(validator);
    }

    @Test
    void anotherOrgsRowLooksLikeAMissingOne() {
        when(userRepository.getOrgUserLink(ROW)).thenReturn(Optional.of(new OrgUserLink("org-2", TARGET)));
        CustomException e = assertThrows(CustomException.class, () -> service.updateOrgUser(update()));
        assertEquals("Organization", e.getCode());
        verifyNoInteractions(hrmsUtils);
    }

    @Test
    void platformAdminCallerIsNotChecked() {
        when(orgAdminScopeResolver.resolve(any())).thenReturn(OrgAdminScopeResolver.OrgScope.unrestricted());
        targetHolds("ORG_PLATFORM_ADMIN");
        service.updateOrgUser(update());
        verify(validator).validateUpdateOrgUserRequest(any());
        verifyNoInteractions(hrmsUtils);
    }
}
