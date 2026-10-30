package org.egov.im.validator;

import org.egov.im.web.models.Document;
import org.egov.im.web.models.IncidentRequest;
import org.egov.im.web.models.OnHoldReason;
import org.egov.im.web.models.Workflow;
import org.egov.tracer.model.CustomException;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.egov.im.util.IMConstants.IM_WF_ON_HOLD;
import static org.egov.im.util.IMConstants.ON_HOLD_REASON_FUND_UNAVAILABLE;
import static org.egov.im.util.IMConstants.ON_HOLD_REASON_OTHER;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the server-side checks on the ON_HOLD action.
 */
class OnHoldValidationTest {

    private static final List<Document> LETTER = List.of(Document.builder().fileStoreId("fs-1").documentType("application/pdf").build());

    private final ServiceRequestValidator validator = new ServiceRequestValidator(null, null, null);

    @Test
    void fundUnavailableWithDocumentAndNoCommentPasses() {
        assertDoesNotThrow(() -> validator.validateOnHold(request(ON_HOLD_REASON_FUND_UNAVAILABLE, null, LETTER)));
    }

    @Test
    void otherWithCommentAndDocumentPasses() {
        assertDoesNotThrow(() -> validator.validateOnHold(request(ON_HOLD_REASON_OTHER, "Awaiting district approval", LETTER)));
    }

    @Test
    void missingDocumentFails() {
        assertFailsWith("ON_HOLD_DOCUMENT_MANDATORY", request(ON_HOLD_REASON_FUND_UNAVAILABLE, null, Collections.emptyList()));
        assertFailsWith("ON_HOLD_DOCUMENT_MANDATORY", request(ON_HOLD_REASON_FUND_UNAVAILABLE, null, null));
        assertFailsWith("ON_HOLD_DOCUMENT_MANDATORY",
                request(ON_HOLD_REASON_FUND_UNAVAILABLE, null, List.of(Document.builder().fileStoreId(" ").build())));
    }

    @Test
    void missingOrUnknownReasonFails() {
        assertFailsWith("INVALID_ON_HOLD_REASON", request(null, null, LETTER));
        assertFailsWith("INVALID_ON_HOLD_REASON", request("BUDGET_FROZEN", null, LETTER));
    }

    @Test
    void otherWithoutCommentFails() {
        assertFailsWith("ON_HOLD_COMMENT_MANDATORY", request(ON_HOLD_REASON_OTHER, "  ", LETTER));
    }

    @Test
    void commentOver500CharactersFails() {
        assertFailsWith("ON_HOLD_COMMENT_LENGTH_EXCEEDED", request(ON_HOLD_REASON_OTHER, "a".repeat(501), LETTER));
        assertDoesNotThrow(() -> validator.validateOnHold(request(ON_HOLD_REASON_OTHER, "a".repeat(500), LETTER)));
    }

    @Test
    void otherActionsAreNotChecked() {
        IncidentRequest assign = request(null, null, null);
        assign.getWorkflow().setAction("ASSIGN");
        assertDoesNotThrow(() -> validator.validateOnHold(assign));
    }

    private void assertFailsWith(String code, IncidentRequest request) {
        CustomException e = assertThrows(CustomException.class, () -> validator.validateOnHold(request));
        assertTrue(e.getErrors().containsKey(code), "Expected " + code + " but got " + e.getErrors().keySet());
    }

    private static IncidentRequest request(String reason, String comments, List<Document> documents) {
        Workflow workflow = Workflow.builder()
                .action(IM_WF_ON_HOLD)
                .comments(comments)
                .verificationDocuments(documents)
                .onHoldReason(reason == null ? null : OnHoldReason.builder().reason(reason).build())
                .build();
        IncidentRequest request = new IncidentRequest();
        request.setWorkflow(workflow);
        return request;
    }
}
