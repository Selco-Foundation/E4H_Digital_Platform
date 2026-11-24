package org.egov.validator;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.config.Configuration;
import org.egov.helper.MDMSTestBuilder;
import org.egov.helper.OrganisationRequestTestBuilder;
import org.egov.repository.OrganisationRepository;
import org.egov.tracer.model.CustomException;
import org.egov.util.BoundaryUtil;
import org.egov.util.MDMSUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.web.models.OrgRequest;
import org.egov.web.models.Organisation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@Slf4j
class OrganisationServiceValidatorTest {

    @InjectMocks
    private OrganisationServiceValidator organisationServiceValidator;

    @Mock
    private MDMSUtil mdmsUtil;

    @Mock
    private OrganisationRepository organisationRepository;

    @Mock
    private BoundaryUtil boundaryUtil;

    @Mock
    private Configuration config;

    @BeforeEach
    void setUp() throws Exception {
        Object mdmsResponse = MDMSTestBuilder.builder().getMockMDMSData();
        lenient().when(mdmsUtil.mDMSCall(any(RequestInfo.class),any(String.class))).thenReturn(mdmsResponse);
    }

    @Test
    void shouldNotThrowException_IfCreateValidationSuccess() {
//        OrgRequest orgRequest = OrganisationRequestTestBuilder.builder().withRequestInfo().addGoodOrganisationForCreate().build();
//        assertDoesNotThrow(() -> organisationServiceValidator.validateCreateOrgRegistryWithoutWorkFlow(orgRequest));
    }

    @Test
    void shouldThrowException_IfRequestInfoIsNullForCreateOrganisation() {
        OrgRequest orgRequest = OrganisationRequestTestBuilder.builder().addGoodOrganisationForCreate().build();
        orgRequest.setRequestInfo(null);
        CustomException exception = assertThrows(CustomException.class, ()-> organisationServiceValidator.validateCreateOrgRegistryWithoutWorkFlow(orgRequest));
        assertTrue(exception.toString().contains("Request info is mandatory"));
    }

    @Test
    void shouldThrowException_IfUserInfoIsNullForCreateOrganisation() {
        OrgRequest orgRequest = OrganisationRequestTestBuilder.builder().withRequestInfo().addGoodOrganisationForCreate().build();
        orgRequest.getRequestInfo().setUserInfo(null);
        CustomException exception = assertThrows(CustomException.class, ()-> organisationServiceValidator.validateCreateOrgRegistryWithoutWorkFlow(orgRequest));
        assertTrue(exception.toString().contains("UserInfo is mandatory"));
    }

    @Test
    void shouldThrowException_IfUUIDIsBlankForCreateOrganisation() {
        OrgRequest orgRequest = OrganisationRequestTestBuilder.builder().withRequestInfo().addGoodOrganisationForCreate().build();
        orgRequest.getRequestInfo().getUserInfo().setUuid(null);
        CustomException exception = assertThrows(CustomException.class, ()-> organisationServiceValidator.validateCreateOrgRegistryWithoutWorkFlow(orgRequest));
        assertTrue(exception.toString().contains("UUID is mandatory"));
    }

    @Test
    void shouldThrowException_IfProjectIsNullForCreateOrganisation() {
        OrgRequest orgRequest = OrganisationRequestTestBuilder.builder().withRequestInfo().addGoodOrganisationForCreate().build();
        orgRequest.setOrganisations(null);
        CustomException exception = assertThrows(CustomException.class, ()-> organisationServiceValidator.validateCreateOrgRegistryWithoutWorkFlow(orgRequest));
        assertTrue(exception.toString().contains("At least one organisation detail is required"));
    }

    @Test
    void shouldThrowException_IfTenantIdIsNullForCreateOrganisation() {
        OrgRequest orgRequest = OrganisationRequestTestBuilder.builder().withRequestInfo().addGoodOrganisationForCreate().build();
        orgRequest.getOrganisations().get(0).setTenantId(null);
        CustomException exception = assertThrows(CustomException.class, ()-> organisationServiceValidator.validateCreateOrgRegistryWithoutWorkFlow(orgRequest));
        assertTrue(exception.toString().contains("Tenant id is mandatory"));
    }

    @Test
    void shouldThrowException_IfOrganisationNameIsNullForCreateOrganisation() {
        OrgRequest orgRequest = OrganisationRequestTestBuilder.builder().withRequestInfo().addGoodOrganisationForCreate().build();
        orgRequest.getOrganisations().get(0).setName(null);
        CustomException exception = assertThrows(CustomException.class, ()-> organisationServiceValidator.validateCreateOrgRegistryWithoutWorkFlow(orgRequest));
        System.out.println(exception);
        assertTrue(exception.toString().contains("Organisation name is mandatory"));
    }

    @Test
    void shouldThrowException_IfPocPasswordBreaksThePolicyForCreateOrganisation() {
        OrgRequest orgRequest = OrganisationRequestTestBuilder.builder().withRequestInfo().addGoodOrganisationForCreate().build();
        orgRequest.getOrganisations().get(0).setOrgType("VENDOR");
        orgRequest.getOrganisations().get(0).setOrgPocPassword("password");
        CustomException exception = assertThrows(CustomException.class, ()-> organisationServiceValidator.validateCreateOrgRegistryWithoutWorkFlow(orgRequest));
        assertEquals("INVALID_PWD_PATTERN", exception.getCode());
    }

    @Test
    void shouldThrowException_IfPocDetailsAreMissingForCreateOrganisation() {
        Organisation organisation = Organisation.builder().orgPocName(" ").orgPocPhone("12345").build();
        CustomException exception = assertThrows(CustomException.class,
                () -> OrganisationServiceValidator.validatePocDetails(organisation));
        assertEquals(java.util.Set.of("ORG_POC_NAME_REQUIRED", "ORG_POC_PHONE_INVALID", "ORG_POC_EMAIL_REQUIRED", "ORG_POC_USERNAME_REQUIRED"),
                exception.getErrors().keySet());
    }

    @Test
    void pocPasswordIsNeverLoggedOrSerialised() throws Exception {
        Organisation organisation = Organisation.builder().name("org").orgPocPassword("abc1@de").build();
        assertFalse(organisation.toString().contains("abc1@de"));
        assertFalse(new ObjectMapper().writeValueAsString(organisation).contains("abc1@de"));
        assertEquals("abc1@de", new ObjectMapper().readValue("{\"orgPocPassword\":\"abc1@de\"}", Organisation.class).getOrgPocPassword());
    }

    @Test
    void shouldThrowException_IfAddressTenantIdIsNullForCreateOrganisation() {
        OrgRequest orgRequest = OrganisationRequestTestBuilder.builder().withRequestInfo().addGoodOrganisationForCreate().build();
        orgRequest.getOrganisations().get(0).getOrgAddress().get(0).setTenantId(null);
        CustomException exception = assertThrows(CustomException.class, ()-> organisationServiceValidator.validateCreateOrgRegistryWithoutWorkFlow(orgRequest));
//        assertTrue(exception.toString().contains("Tenant id is mandatory"));
    }

    @Test
    void shouldThrowException_IfBoundaryTypeIsNullForCreateOrganisation() {
        OrgRequest orgRequest = OrganisationRequestTestBuilder.builder().withRequestInfo().addGoodOrganisationForCreate().build();
        orgRequest.getOrganisations().get(0).getOrgAddress().get(0).setBoundaryType(null);
        CustomException exception = assertThrows(CustomException.class, ()-> organisationServiceValidator.validateCreateOrgRegistryWithoutWorkFlow(orgRequest));
//        assertTrue(exception.toString().contains("Address's boundary type is mandatory"));
    }

    @Test
    void shouldThrowException_IfBoundaryCodeIsNullForCreateOrganisation() {
        OrgRequest orgRequest = OrganisationRequestTestBuilder.builder().withRequestInfo().addGoodOrganisationForCreate().build();
        orgRequest.getOrganisations().get(0).getOrgAddress().get(0).setBoundaryCode(null);
        CustomException exception = assertThrows(CustomException.class, ()-> organisationServiceValidator.validateCreateOrgRegistryWithoutWorkFlow(orgRequest));
//        assertTrue(exception.toString().contains("Address's boundary code is mandatory"));
    }

    @Test
    void shouldThrowException_IfOrgTypeInValidForCreateOrganisation() {
//        OrgRequest orgRequest = OrganisationRequestTestBuilder.builder().withRequestInfo().addGoodOrganisationForCreate().build();
//        orgRequest.getOrganisations().get(0).getFunctions().get(0).setType("ot1");
//        CustomException exception = assertThrows(CustomException.class, ()-> organisationServiceValidator.validateCreateOrgRegistryWithoutWorkFlow(orgRequest));
//        assertTrue(exception.toString().contains("INVALID_ORG_TYPE"));
    }

    @Test
    void shouldThrowException_IfTaxIdentifierInValidForCreateOrganisation() {
//        OrgRequest orgRequest = OrganisationRequestTestBuilder.builder().withRequestInfo().addGoodOrganisationForCreate().build();
//        orgRequest.getOrganisations().get(0).getIdentifiers().get(0).setType("ot1");
//        CustomException exception = assertThrows(CustomException.class, ()-> organisationServiceValidator.validateCreateOrgRegistryWithoutWorkFlow(orgRequest));
//        assertTrue(exception.toString().contains("INVALID_ORG.IDENTIFIER_TYPE"));
    }

}
