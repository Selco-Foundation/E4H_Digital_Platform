package org.egov.filestore.web.contract;

import lombok.extern.slf4j.Slf4j;
import org.egov.filestore.domain.model.FileInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ResponseFactory {

	private static final String FORMAT = "%s/v1/files/id?fileStoreId=%s&tenantId=%s";
	private String contextPath;

    public ResponseFactory(@Value("${server.contextPath}") String contextPath) {
        this.contextPath = contextPath;
        log.debug("ResponseFactory initialized with contextPath: {}", contextPath);
    }

    public GetFilesByTagResponse getFilesByTagResponse(List<FileInfo> listOfFileInfo) {
        log.trace("Entering getFilesByTagResponse method with fileInfo count: {}", listOfFileInfo.size());
        log.info("Creating response for {} files by tag", listOfFileInfo.size());
        List<FileRecord> fileRecords = listOfFileInfo.stream().map(fileInfo -> {
            String url = String.format(FORMAT, contextPath,
					fileInfo.getFileLocation().getFileStoreId(),
					fileInfo.getTenantId());
            log.debug("Generated URL for fileStoreId: {}, tenantId: {}", 
					fileInfo.getFileLocation().getFileStoreId(), fileInfo.getTenantId());
            return new FileRecord(url, fileInfo.getContentType());
        }).collect(Collectors.toList());

        log.info("Successfully created response with {} file records", fileRecords.size());
        return new GetFilesByTagResponse(fileRecords);
    }


}
