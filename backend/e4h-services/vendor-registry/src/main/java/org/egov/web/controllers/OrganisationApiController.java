package org.egov.web.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiParam;
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

        OrgRequest orgRequest = organisationService.createOrganisationWithoutWorkFlow(body);
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(body.getRequestInfo(), true);
        OrgResponse orgResponse = OrgResponse.builder().responseInfo(responseInfo).organisations(orgRequest.getOrganisations()).build();
        return new ResponseEntity<OrgResponse>(orgResponse, HttpStatus.OK);

    }

    @RequestMapping(value = "/_search", method = RequestMethod.POST)
    public ResponseEntity<OrgServiceResponse> orgServicesOrganisationV1SearchPOST(
            @ApiParam(value = "", allowableValues = "application/json") @RequestHeader(value = "Content-Type", required = false) String contentType,
            @ApiParam(value = "") @Valid @RequestBody OrgSearchRequest body) {

        List<Organisation> organisations = organisationService.searchOrganisation(body);
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(body.getRequestInfo(), true);
        Integer count = organisationService.countAllOrganisations(body);
        OrgServiceResponse orgServiceResponse = OrgServiceResponse.builder().responseInfo(responseInfo).organisations(organisations).totalCount(count).build();
        return new ResponseEntity<OrgServiceResponse>(orgServiceResponse, HttpStatus.OK);
    }

    @RequestMapping(value = "/_update", method = RequestMethod.POST)
    public ResponseEntity<OrgResponse> orgServicesOrganisationV1UpdatePOST(
            @ApiParam(value = "", allowableValues = "application/json") @RequestHeader(value = "Content-Type", required = false) String contentType,
            @ApiParam(value = "") @Valid @RequestBody OrgRequest body) {

        OrgRequest orgRequest = organisationService.updateOrganisationWithoutWorkFlow(body);
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(body.getRequestInfo(), true);
        OrgResponse orgResponse = OrgResponse.builder().responseInfo(responseInfo).organisations(orgRequest.getOrganisations()).build();
        return new ResponseEntity<OrgResponse>(orgResponse, HttpStatus.OK);
    }

    @RequestMapping(value = "/user/_create", method = RequestMethod.POST)
    public ResponseEntity<OrgUserResponse> orgUserV1CreatePost(@ApiParam(value = "Capture linkage of Project and staff user.", required = true) @Valid @RequestBody OrgUserRequest request) {

        List<OrgUser> orgUserList = userService.createOrgUser(request);
        OrgUserResponse response = OrgUserResponse.builder()
                .orgUsers(orgUserList)
                .responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(), true))
                .build();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @RequestMapping(value = "/user/_search", method = RequestMethod.POST)
    public ResponseEntity<OrgUserResponseSearch> projectStaffV1SearchPost(
            @Valid @ModelAttribute URLParams urlParams,
            @ApiParam(value = "Capture details of Project staff.", required = true) @Valid @RequestBody OrgUserSearchRequest request
    ) throws Exception {
        List<OrgUserEnriched> orgUserList = userService.searchOrganisationUsers(
                request,
                urlParams
        );
        OrgUserResponseSearch response = OrgUserResponseSearch.builder()
                .orgUsers(orgUserList)
                .totalCount(0)
                .responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(), true))
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
