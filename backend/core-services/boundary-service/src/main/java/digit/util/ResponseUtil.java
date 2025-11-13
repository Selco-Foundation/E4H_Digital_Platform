package digit.util;

import digit.web.models.BoundaryRequest;
import digit.web.models.BoundaryResponse;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.response.ResponseInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ResponseUtil {

    @Autowired
    private ResponseInfoFactory responseInfoFactory;

    public BoundaryResponse createBoundaryResponse(BoundaryRequest boundaryRequest) {
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(boundaryRequest.getRequestInfo(), true);
        log.debug("ResponseUtil::createdBoundaryResponse");
        BoundaryResponse boundaryResponse = BoundaryResponse.builder().responseInfo(responseInfo).boundary(boundaryRequest.getBoundary()).build();
        return boundaryResponse;
    }
}
