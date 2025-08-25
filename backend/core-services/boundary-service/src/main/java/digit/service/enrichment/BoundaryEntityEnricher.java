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
        log.debug("BoundaryEntityEnricher::enrichCreateBoundaryRequest");
        boundaryRequest.getBoundary().forEach(boundary -> {
            UUIDEnrichmentUtil.enrichRandomUuid(boundary,"id");
            boundary.setAuditDetails(AuditDetailsEnrichmentUtil.prepareAuditDetails(boundary.getAuditDetails(),boundaryRequest.getRequestInfo(),Boolean.TRUE));
        });
    }

    /**
     * Enrich the update boundary request
     * @param boundaryRequest
     */
    public static void enrichUpdateBoundaryRequest(BoundaryRequest boundaryRequest) {
        log.debug("BoundaryEntityEnricher::enrichUpdateBoundaryRequest");
        boundaryRequest.getBoundary().forEach(boundary -> {
            boundary.setAuditDetails(AuditDetailsEnrichmentUtil.prepareAuditDetails(boundary.getAuditDetails(),boundaryRequest.getRequestInfo(),Boolean.FALSE));
        });
    }
}
