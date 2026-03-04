package org.egov.filestore.web.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.filestore.domain.model.FileInfo;
import org.egov.filestore.domain.service.HLSStorageService;
import org.egov.filestore.domain.service.StorageService;
import org.egov.filestore.utils.StorageUtil;
import org.egov.filestore.web.contract.File;
import org.egov.filestore.web.contract.FileStoreResponse;
import org.egov.filestore.web.contract.GetFilesByTagResponse;
import org.egov.filestore.web.contract.ResponseFactory;
import org.egov.filestore.web.contract.StorageResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/v1/files")
public class StorageController {

    private final StorageService storageService;
    private final ResponseFactory responseFactory;
    private final StorageUtil storageUtil;
    private final HLSStorageService hlsStorageService;


    @GetMapping("/id")
    @ResponseBody
    public ResponseEntity<Resource> getFile(@RequestParam(value = "tenantId") String tenantId,
                                            @RequestParam("fileStoreId") String fileStoreId) {
        log.trace("Entering getFile method with fileStoreId: {}, tenantId: {}", fileStoreId, tenantId);
        log.info("Retrieving file for fileStoreId: {}, tenantId: {}", fileStoreId, tenantId);
        org.egov.filestore.domain.model.Resource resource = null;
        try {
            resource = storageService.retrieve(fileStoreId, tenantId);
            log.debug("File retrieved successfully, fileName: {}, contentType: {}", 
                    resource.getFileName(), resource.getContentType());
        } catch (IOException e) {
            log.error("Error while retrieving file for fileStoreId: {}, tenantId: {}", fileStoreId, tenantId, e);
            throw new RuntimeException("Failed to retrieve file", e);
        }
        String fileName = resource.getFileName().substring(resource.getFileName().lastIndexOf('/') + 1, resource.getFileName().length());
        log.info("Returning file response for fileName: {}", fileName);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .header(HttpHeaders.CONTENT_TYPE, resource.getContentType()).body(resource.getResource());
    }

    @GetMapping("/metadata")
    @ResponseBody
    public ResponseEntity<org.egov.filestore.domain.model.Resource> getMetaData(
            @RequestParam(value = "tenantId") String tenantId, @RequestParam("fileStoreId") String fileStoreId) {
        log.trace("Entering getMetaData method with fileStoreId: {}, tenantId: {}", fileStoreId, tenantId);
        log.info("Fetching metadata for fileStoreId: {}, tenantId: {}", fileStoreId, tenantId);
        org.egov.filestore.domain.model.Resource resource = null;
        try {
            resource = storageService.retrieve(fileStoreId, tenantId);
            log.debug("Metadata retrieved successfully for fileStoreId: {}", fileStoreId);
        } catch (IOException e) {
            log.error("Error while fetching metadata for fileStoreId: {}, tenantId: {}", fileStoreId, tenantId, e);
            throw new RuntimeException("Failed to fetch metadata", e);
        }
        resource.setResource(null);
        log.info("Returning metadata response for fileStoreId: {}", fileStoreId);
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    @GetMapping(value = "/tag", produces = APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public GetFilesByTagResponse getUrlListByTag(@RequestParam(value = "tenantId") String tenantId,
                                                 @RequestParam("tag") String tag) {
        log.trace("Entering getUrlListByTag method with tag: {}, tenantId: {}", tag, tenantId);
        log.info("Retrieving files by tag: {}, tenantId: {}", tag, tenantId);
        final List<FileInfo> fileInfoList = storageService.retrieveByTag(tag, tenantId);
        log.debug("Found {} files for tag: {}", fileInfoList.size(), tag);
        log.info("Returning file list response for tag: {}", tag);
        return responseFactory.getFilesByTagResponse(fileInfoList);
    }

    @PostMapping(produces = APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public StorageResponse storeFiles(@RequestParam("file") List<MultipartFile> files,
                                      @RequestParam(value = "tenantId") String tenantId,
                                      @RequestParam(value = "module", required = true) String module,
                                      @RequestParam(value = "tag", required = false) String tag,
                                      @RequestParam(value = "requestInfo", required = false) String requestInfo
    ) {
        log.trace("Entering storeFiles method with module: {}, tag: {}, tenantId: {}, fileCount: {}", 
                module, tag, tenantId, files.size());
        log.info("Storing {} files for module: {}, tag: {}, tenantId: {}", files.size(), module, tag, tenantId);
        RequestInfo reqInfo = storageUtil.getRequestInfo(requestInfo);
        log.debug("RequestInfo processed successfully");
        final List<String> fileStoreIds = storageService.save(files, module, tag, tenantId, reqInfo);
        log.debug("Files stored successfully, generated {} fileStoreIds", fileStoreIds.size());
        log.info("File storage completed for module: {}, tag: {}", module, tag);
        return getStorageResponse(fileStoreIds, tenantId);
    }

    private StorageResponse getStorageResponse(List<String> fileStorageIds, String tenantId) {
        List<File> files = new ArrayList<>();
        for (String fileStorageId : fileStorageIds) {
            File f = new File(fileStorageId, tenantId);
            files.add(f);
        }
        return new StorageResponse(files);
    }

    @GetMapping("/url")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUrls(@RequestParam(value = "tenantId") String tenantId,
                                                       @RequestParam("fileStoreIds") List<String> fileStoreIds) {
        log.trace("Entering getUrls method with tenantId: {}, fileStoreIds count: {}", tenantId, fileStoreIds.size());
        log.info("Retrieving URLs for {} fileStoreIds, tenantId: {}", fileStoreIds.size(), tenantId);
        Map<String, Object> responseMap = new HashMap<>();
        if (fileStoreIds.isEmpty()) {
            log.warn("Empty fileStoreIds list provided for tenantId: {}", tenantId);
            return new ResponseEntity<>(new HashMap<>(), HttpStatus.OK);
        }
        Map<String, String> maps = storageService.getUrls(tenantId, fileStoreIds);
        log.debug("Retrieved {} URLs for tenantId: {}", maps.size(), tenantId);

        List<FileStoreResponse> responses = new ArrayList<>();
        for (Entry<String, String> entry : maps.entrySet()) {
            responses.add(FileStoreResponse.builder().id(entry.getKey()).url(entry.getValue()).build());
        }
        responseMap.putAll(maps);
        responseMap.put("fileStoreIds", responses);

        log.info("Returning URL response for {} fileStoreIds", fileStoreIds.size());
        return new ResponseEntity<>(responseMap, HttpStatus.OK);
    }


    @PostMapping(value = "hls", produces = APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public StorageResponse storeHlsFiles(@RequestParam("file") List<MultipartFile> files,
                                         @RequestParam(value = "tenantId") String tenantId,
                                         @RequestParam(value = "module", required = true) String module,
                                         @RequestParam(value = "tag", required = false) String tag,
                                         @RequestParam(value = "requestInfo", required = false) String requestInfo) {
        log.trace("Entering storeHlsFiles method with module: {}, tag: {}, tenantId: {}, fileCount: {}", 
                module, tag, tenantId, files.size());
        log.info("Received HLS upload request for tenantId: {}, module: {}, tag: {}, fileCount: {}",
                tenantId, module, tag, files.size());
        RequestInfo reqInfo = storageUtil.getRequestInfo(requestInfo);
        log.debug("RequestInfo processed for HLS upload");
        final List<String> fileStoreIds = hlsStorageService.save(files, module, tag, tenantId, reqInfo);
        log.debug("HLS files stored successfully, generated {} fileStoreIds", fileStoreIds.size());
        log.info("HLS file storage completed for module: {}, tag: {}", module, tag);
        return getStorageResponse(fileStoreIds, tenantId);
    }

    @GetMapping("get-hls")
    public ResponseEntity<Resource> getHlsChunk(
            @RequestParam String fileStoreId,
            @RequestParam String quality,
            @RequestParam String filename,
            @RequestParam("tenantId") String tenantId) {
        log.trace("Entering getHlsChunk method with fileStoreId: {}, quality: {}, filename: {}, tenantId: {}", 
                fileStoreId, quality, filename, tenantId);
        log.info("Retrieving HLS chunk for fileStoreId: {}, quality: {}, filename: {}, tenantId: {}", 
                fileStoreId, quality, filename, tenantId);

        try {
            org.egov.filestore.domain.model.Resource resource =
                    storageService.retrieve(fileStoreId, quality, filename, tenantId);

            if (resource == null) {
                log.warn("HLS chunk not found for fileStoreId: {}, quality: {}, filename: {}", 
                        fileStoreId, quality, filename);
                return ResponseEntity.notFound().build();
            }

            String fileName = resource.getFileName()
                    .substring(resource.getFileName().lastIndexOf('/') + 1);
            log.debug("HLS chunk retrieved successfully, fileName: {}, contentType: {}", 
                    fileName, resource.getContentType());
            log.info("Returning HLS chunk response for fileName: {}", fileName);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, resource.getContentType())
                    .body(resource.getResource());

        } catch (Exception e) {
            log.error("Error retrieving HLS chunk for fileStoreId: {}, quality: {}, filename: {}, tenantId: {}", 
                    fileStoreId, quality, filename, tenantId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/file")
    public ResponseEntity<Void> getS3SignedUrlFile( @RequestParam String tenantId, @RequestParam String fileStoreId) {
        log.trace("Entering getS3SignedUrlFile method with fileStoreId: {}, tenantId: {}", fileStoreId, tenantId);
        if (tenantId == null || tenantId.trim().isEmpty()) {
            log.warn("Invalid tenantId provided for fileStoreId: {}", fileStoreId);
            return ResponseEntity.badRequest().build();
        }
        if (fileStoreId == null || fileStoreId.trim().isEmpty()) {
            log.warn("Invalid fileStoreId provided for tenantId: {}", tenantId);
            return ResponseEntity.badRequest().build();
        }
        log.info("Retrieving signed URL for fileStoreId: {}, tenantId: {}", fileStoreId, tenantId);
        try {
            String signedUrl = storageService.retrieveSignedUrl(fileStoreId, tenantId);
            if (signedUrl == null || signedUrl.trim().isEmpty()) {
                log.warn("Signed URL not found for fileStoreId: {}, tenantId: {}", fileStoreId, tenantId);
                return ResponseEntity.notFound().build();
            }
            log.debug("Signed URL generated successfully for fileStoreId: {}", fileStoreId);
            log.info("Redirecting to signed URL for fileStoreId: {}", fileStoreId);
            return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT)  // 307 redirect
                    .location(URI.create(signedUrl))
                    .build();
        } catch (Exception e) {
            log.error("Error while retrieving signed URL for fileStoreId: {} and tenantId: {}", fileStoreId, tenantId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.TEXT_HTML)
                    .body(null); // Will be handled by static HTML below
        }
    }
}
