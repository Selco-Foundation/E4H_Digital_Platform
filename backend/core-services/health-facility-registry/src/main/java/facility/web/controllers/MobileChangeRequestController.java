package facility.web.controllers;

import facility.service.MobileChangeRequestService;
import facility.web.models.MobileChangeRequest;
import facility.web.models.MobileChangeRequestActionRequest;
import facility.web.models.MobileChangeRequestCreateRequest;
import facility.web.models.MobileChangeRequestResponse;
import facility.web.models.MobileChangeRequestSearchRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.response.ResponseInfo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Registered-mobile-number change requests (SEM 4.0). Facility users {@code _create},
 * {@code _cancel} and {@code _search} their own; the CRM ({@code COMPLAINT_ASSESSOR})
 * {@code _search}es the queue and {@code _approve}s or {@code _reject}s. Separate endpoints per
 * action so the gateway's role-action mapping can tell them apart.
 */
@RestController
@RequestMapping("/v2/facility/mobile-change-request")
@RequiredArgsConstructor
public class MobileChangeRequestController {

    private final MobileChangeRequestService service;

    @PostMapping("/_create")
    public ResponseEntity<MobileChangeRequestResponse> create(@Valid @RequestBody MobileChangeRequestCreateRequest request) {
        MobileChangeRequest created = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(single(request.getRequestInfo(), created));
    }

    @PostMapping("/_cancel")
    public ResponseEntity<MobileChangeRequestResponse> cancel(@Valid @RequestBody MobileChangeRequestActionRequest request) {
        return ResponseEntity.ok(single(request.getRequestInfo(), service.cancel(request)));
    }

    @PostMapping("/_approve")
    public ResponseEntity<MobileChangeRequestResponse> approve(@Valid @RequestBody MobileChangeRequestActionRequest request) {
        return ResponseEntity.ok(single(request.getRequestInfo(), service.approve(request)));
    }

    @PostMapping("/_reject")
    public ResponseEntity<MobileChangeRequestResponse> reject(@Valid @RequestBody MobileChangeRequestActionRequest request) {
        return ResponseEntity.ok(single(request.getRequestInfo(), service.reject(request)));
    }

    @PostMapping("/_search")
    public ResponseEntity<MobileChangeRequestResponse> search(@Valid @RequestBody MobileChangeRequestSearchRequest request) {
        MobileChangeRequestResponse response = service.search(request);
        response.setResponseInfo(responseInfo(request.getRequestInfo()));
        return ResponseEntity.ok(response);
    }

    private static MobileChangeRequestResponse single(RequestInfo requestInfo, MobileChangeRequest changeRequest) {
        return MobileChangeRequestResponse.builder()
                .responseInfo(responseInfo(requestInfo))
                .mobileChangeRequests(List.of(changeRequest))
                .totalCount(1)
                .build();
    }

    private static ResponseInfo responseInfo(RequestInfo requestInfo) {
        return ResponseInfo.builder()
                .apiId(requestInfo.getApiId())
                .ver(requestInfo.getVer())
                .ts(System.currentTimeMillis())
                .msgId(requestInfo.getMsgId())
                .status("successful")
                .build();
    }
}
