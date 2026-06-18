package org.egov.field_planner.web.models;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller-only request wrapper for create/update.
 * The template file is accepted for validation/processing but is not persisted in the database.
 */
@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FieldPlanTemplateWriteRequest {

    @NotNull
    @Valid
    private FieldPlanTemplateBulkRequest bulkRequest;

    private MultipartFile excelFile;
}
