package digit.service.enrichment;

import digit.web.models.BoundaryRequest;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.utils.AuditDetailsEnrichmentUtil;
import org.egov.common.utils.UUIDEnrichmentUtil;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
public class BoundaryEntityEnricher {

    private BoundaryEntityEnricher() {}

    /**
     *  Enrich the create boundary request
     *  param boundaryRequest
     */
    public static void enrichCreateBoundaryRequest(BoundaryRequest boundaryRequest) {
        log.trace("enrichCreateBoundaryRequest method invoked");
        int boundaryCount = boundaryRequest.getBoundary() != null ? boundaryRequest.getBoundary().size() : 0;
        log.debug("Enriching boundary create request, boundary count={}", boundaryCount);
        
        boundaryRequest.getBoundary().forEach(boundary -> {
            log.debug("Enriching boundary, code={}", boundary.getCode());
            UUIDEnrichmentUtil.enrichRandomUuid(boundary,"id");
            boundary.setAuditDetails(AuditDetailsEnrichmentUtil.prepareAuditDetails(boundary.getAuditDetails(),boundaryRequest.getRequestInfo(),Boolean.TRUE));
        });
        
        log.debug("Boundary create request enrichment completed");
    }

    /**
     * Enrich the update boundary request
     * @param boundaryRequest
     */
    public static void enrichUpdateBoundaryRequest(BoundaryRequest boundaryRequest) {
        log.trace("enrichUpdateBoundaryRequest method invoked");
        int boundaryCount = boundaryRequest.getBoundary() != null ? boundaryRequest.getBoundary().size() : 0;
        log.debug("Enriching boundary update request, boundary count={}", boundaryCount);
        
        boundaryRequest.getBoundary().forEach(boundary -> {
            log.debug("Enriching boundary, code={}", boundary.getCode());
            boundary.setAuditDetails(AuditDetailsEnrichmentUtil.prepareAuditDetails(boundary.getAuditDetails(),boundaryRequest.getRequestInfo(),Boolean.FALSE));
        });
        
        log.debug("Boundary update request enrichment completed");
    }
}
