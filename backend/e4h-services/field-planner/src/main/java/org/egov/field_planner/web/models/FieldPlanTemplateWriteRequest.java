package org.egov.field_planner.web.models;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller-only request wrapper for create/update.
 * Each template file is persisted individually via FileStoreService and its resulting
 * fileStoreId is stamped onto the corresponding FieldPlanTemplate at the same list index.
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

    @Builder.Default
    private List<MultipartFile> excelFiles = new ArrayList<>();
}
