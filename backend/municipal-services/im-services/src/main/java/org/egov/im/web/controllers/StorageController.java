package org.egov.im.web.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.im.service.StorageService;
import org.egov.im.web.models.ProcessingContext;
import org.egov.im.web.models.storage.StorageResponse;
import org.egov.tracer.model.CustomException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v2/video")
@Slf4j
public class StorageController {

    private final StorageService storageService;

    @PostMapping(value = "upload")
    public StorageResponse storeFiles(@RequestParam("file") List<MultipartFile> files,
                                      @RequestParam(value = "tenantId") String tenantId,
                                      @RequestParam(value = "module", required = true) String module,
                                      @RequestParam(value = "tag", required = false) String tag,
                                      @RequestParam(value = "requestInfo", required = false) String requestInfo) {

        log.info("Received upload request for jurisdiction: {}, module: {}, tag: {} with file count: {}",
                tenantId, module, tag, files.size());
        try {
            ProcessingContext context = ProcessingContext.builder()
                    .requestInfo(requestInfo)
                    .tag(tag)
                    .tenantId(tenantId)
                    .module(module)
                    .build();
            return storageService.save(files, context);
        } catch (IOException e) {
            throw new CustomException("ERROR_UPLOADING_TO_FILESTORE", e.getMessage());
        }
    }
}
