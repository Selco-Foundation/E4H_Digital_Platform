package org.egov.filestore.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

@Component
@Slf4j
public class StorageUtil {

	private ObjectMapper objectMapper;

	@Autowired
	public StorageUtil(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public RequestInfo getRequestInfo(String requestInfoBase64) {
		RequestInfo requestInfo = null;
		try {
			//String decoded = new String(Base64.getDecoder().decode(requestInfoBase64));
			if(requestInfoBase64 != null)
			requestInfo = objectMapper.readValue(requestInfoBase64, RequestInfo.class);
			else
				return new RequestInfo();
		} catch (IOException e) {

			log.error(e.getMessage());
			throw new CustomException("INVALID_REQ_INFO","Failed to deserialization the requestinfo object");
		}
		return requestInfo;
	}

	/*public void enrichAuditDetails(RequestInfo requestInfo, Artifact artifact) {
		if (requestInfo.getUserInfo() != null) {
			artifact.setCreatedBy(requestInfo.getUserInfo().getUuid());
			artifact.setLastModifiedBy(requestInfo.getUserInfo().getUuid());
		}
		artifact.setCreatedTime(System.currentTimeMillis());
		artifact.setLastModifiedTime(System.currentTimeMillis());
	}*/

	public File convertMultipartFileToFile(MultipartFile multipartFile) throws IOException {
		File file = new File(System.getProperty("java.io.tmpdir") + "/" + multipartFile.getOriginalFilename());
		try (FileOutputStream fos = new FileOutputStream(file)) {
			fos.write(multipartFile.getBytes());
		}
		return file;
	}
}
