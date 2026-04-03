package org.egov.web.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.common.models.core.URLParams;
import org.egov.service.OrganisationService;
import org.egov.service.OrganisationUserService;
import org.egov.util.ResponseInfoFactory;
import org.egov.web.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;

@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2023-02-15T14:49:42.141+05:30")

@Controller
@RequestMapping("/organisation/v1")
@Slf4j
public class OrganisationApiController {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private ResponseInfoFactory responseInfoFactory;

    @Autowired
    private OrganisationService organisationService;

    @Autowired
    private OrganisationUserService userService;


    @RequestMapping(value = "/_create", method = RequestMethod.POST)
    public ResponseEntity<OrgResponse> orgServicesOrganisationV1CreatePOST(
            @ApiParam(value = "", allowableValues = "application/json") @RequestHeader(value = "Content-Type", required = false) String contentType,
            @ApiParam(value = "") @Valid @RequestBody OrgRequest body) {
        log.trace("OrganisationApiController::orgServicesOrganisationV1CreatePOST entry");
        log.info("Received create organisation request for tenant: {}",
                body.getOrganisations() != null && !body.getOrganisations().isEmpty()
                    ? body.getOrganisations().get(0).getTenantId() : "unknown");

        OrgRequest orgRequest = organisationService.createOrganisationWithoutWorkFlow(body);

        log.debug("Organisation created successfully, count: {}",
                orgRequest.getOrganisations() != null ? orgRequest.getOrganisations().size() : 0);

        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(body.getRequestInfo(), true);
        OrgResponse orgResponse = OrgResponse.builder().responseInfo(responseInfo).organisations(orgRequest.getOrganisations()).build();

        log.info("Create organisation request completed successfully");
        return new ResponseEntity<OrgResponse>(orgResponse, HttpStatus.OK);

    }

    @RequestMapping(value = "/_search", method = RequestMethod.POST)
    public ResponseEntity<OrgServiceResponse> orgServicesOrganisationV1SearchPOST(
            @ApiParam(value = "", allowableValues = "application/json") @RequestHeader(value = "Content-Type", required = false) String contentType,
            @ApiParam(value = "") @Valid @RequestBody OrgSearchRequest body) {
        log.trace("OrganisationApiController::orgServicesOrganisationV1SearchPOST entry");
        log.info("Received search organisation request for tenant: {}",
                body.getSearchCriteria() != null ? body.getSearchCriteria().getTenantId() : "unknown");

        List<Organisation> organisations = organisationService.searchOrganisation(body);
        log.debug("Search returned {} organisations", organisations != null ? organisations.size() : 0);

        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(body.getRequestInfo(), true);
        Integer count = organisationService.countAllOrganisations(body);
        log.debug("Total organisation count: {}", count);

        OrgServiceResponse orgServiceResponse = OrgServiceResponse.builder().responseInfo(responseInfo).organisations(organisations).totalCount(count).build();
        log.info("Search organisation request completed successfully");
        return new ResponseEntity<OrgServiceResponse>(orgServiceResponse, HttpStatus.OK);
    }

    @RequestMapping(value = "/_update", method = RequestMethod.POST)
    public ResponseEntity<OrgResponse> orgServicesOrganisationV1UpdatePOST(
            @ApiParam(value = "", allowableValues = "application/json") @RequestHeader(value = "Content-Type", required = false) String contentType,
            @ApiParam(value = "") @Valid @RequestBody OrgRequest body) {
        log.trace("OrganisationApiController::orgServicesOrganisationV1UpdatePOST entry");
        log.info("Received update organisation request for tenant: {}",
                body.getOrganisations() != null && !body.getOrganisations().isEmpty()
                    ? body.getOrganisations().get(0).getTenantId() : "unknown");

        OrgRequest orgRequest = organisationService.updateOrganisationWithoutWorkFlow(body);
        log.debug("Organisation updated successfully, count: {}",
                orgRequest.getOrganisations() != null ? orgRequest.getOrganisations().size() : 0);

        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(body.getRequestInfo(), true);
        OrgResponse orgResponse = OrgResponse.builder().responseInfo(responseInfo).organisations(orgRequest.getOrganisations()).build();

        log.info("Update organisation request completed successfully");
        return new ResponseEntity<OrgResponse>(orgResponse, HttpStatus.OK);
    }

    @RequestMapping(value = "/user/_create", method = RequestMethod.POST)
    public ResponseEntity<OrgUserResponse> orgUserV1CreatePost(@ApiParam(value = "Capture linkage of Project and staff user.", required = true) @Valid @RequestBody OrgUserRequest request) {
        log.trace("OrganisationApiController::orgUserV1CreatePost entry");
        log.info("Received create organisation user request");
        OrgUserRequest orgUserList = userService.createOrgUser(request);
        OrgUserResponse response = OrgUserResponse.builder()
                .user(orgUserList.getUser())
                .userId(orgUserList.getUserId())
                .id(orgUserList.getId())
                .organizationId(orgUserList.getOrganizationId())
                .auditDetails(orgUserList.getAuditDetails())
                .responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(), true))
                .build();
        log.info("Create organisation user request completed successfully");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @RequestMapping(value = "/user/_search", method = RequestMethod.POST)
    public ResponseEntity<OrgUserResponseSearch> OrganisationUsersV1SearchPost(
            @Valid @ModelAttribute URLParams urlParams,
            @ApiParam(value = "Capture details of Project staff.", required = true) @Valid @RequestBody OrgUserSearchRequest request
    ) throws Exception {
        log.trace("OrganisationApiController::OrganisationUsersV1SearchPost entry");
        log.info("Received search organisation users request");
        List<OrgUser> orgUserList = userService.searchOrganisationUsers(
                request,
                urlParams
        );
        Integer count = userService.countOrganisationUsers(request);
        log.debug("Search returned {} organisation users", orgUserList != null ? orgUserList.size() : 0);
        OrgUserResponseSearch response = OrgUserResponseSearch.builder()
                .orgUsers(orgUserList)
                .totalCount(count)
                .responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(), true))
                .build();

        log.info("Search organisation users request completed successfully");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @RequestMapping(value = "/user/_update", method = RequestMethod.POST)
    public ResponseEntity<OrgUserResponse> orgUserUpdate(@ApiParam(value = "Capture linkage of Project and staff user.", required = true) @Valid @RequestBody OrgUserRequest request) {

        OrgUserRequest orgUserList = userService.updateOrgUser(request);
        OrgUserResponse response = OrgUserResponse.builder()
                .user(orgUserList.getUser())
                .id(orgUserList.getId())
                .organizationId(orgUserList.getOrganizationId())
                .auditDetails(orgUserList.getAuditDetails())
                .responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(), true))
                .build();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @RequestMapping(value = "/user/_delete", method = RequestMethod.POST)
    public ResponseEntity<OrgUserResponse> deleteUserOrg(@ApiParam(value = "Delete org user.", required = true) @Valid @RequestBody DeleteOrgUserRequest request) {

        DeleteOrgUserRequest orgUserList = userService.deleteUserOrg(request);
        OrgUserResponse response = OrgUserResponse.builder()
                .user(orgUserList.getUser())
                .id(orgUserList.getId())
                .organizationId(orgUserList.getOrganizationId())
                .auditDetails(orgUserList.getAuditDetails())
                .responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(), true))
                .build();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
