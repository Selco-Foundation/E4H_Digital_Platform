package org.egov.processor.models.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.egov.processor.models.ProcessingContext;

@Builder(toBuilder = true)
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class StorageProcessingContext {
    private StorageResponse storageResponse;
    private ProcessingContext context;
}
