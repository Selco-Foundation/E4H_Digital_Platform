package org.egov.filestore.validator;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.tika.Tika;
import org.egov.filestore.config.FileStoreConfig;
import org.egov.filestore.domain.model.Artifact;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class StorageValidator {

	private FileStoreConfig fileStoreConfig;

	
	@Autowired
	public StorageValidator(FileStoreConfig fileStoreConfig) {
		super();
		this.fileStoreConfig = fileStoreConfig;
	}


	public void validate(Artifact artifact) {
		log.trace("Entering validate method");
		String originalFileName = artifact.getMultipartFile().getOriginalFilename();
		log.info("Validating artifact for fileName: {}", originalFileName);
		
		String extension = (FilenameUtils.getExtension(originalFileName)).toLowerCase();
		log.debug("File extension: {}", extension);
		validateFileExtention(extension);
		validateContentType(artifact.getFileContentInString(), extension);
		validateInputContentType(artifact);
		log.info("Validation successful for fileName: {}", originalFileName);
	}
	
	private void validateFileExtention(String extension) {
		log.trace("Entering validateFileExtention method for extension: {}", extension);
		if(!fileStoreConfig.getAllowedFormatsMap().containsKey(extension)) {
			log.error("Invalid file extension: {}. Allowed formats: {}", extension, fileStoreConfig.getAllowedKeySet());
			throw new CustomException("EG_FILESTORE_INVALID_INPUT","Inalvid input provided for file : " + extension + ", please upload any of the allowed formats : " + fileStoreConfig.getAllowedKeySet());
		}
		log.debug("File extension validation passed for: {}", extension);
	}
	
	private void validateContentType(String inputStreamAsString, String extension) {
		log.trace("Entering validateContentType method for extension: {}", extension);
		String inputFormat = null;
		Tika tika = new Tika();
		try {
			log.debug("Detecting content type using Tika for extension: {}", extension);
			InputStream ipStreamForValidation = IOUtils.toInputStream(inputStreamAsString, fileStoreConfig.getImageCharsetType());
			inputFormat = tika.detect(ipStreamForValidation);
			ipStreamForValidation.close();
			log.debug("Detected content type: {} for extension: {}", inputFormat, extension);
		} catch (IOException e) {
			log.error("Error parsing file content for extension: {}", extension, e);
			throw new CustomException("EG_FILESTORE_PARSING_ERROR","not able to parse the input please upload a proper file of allowed type : " + e.getMessage());
		}
		
		if (!fileStoreConfig.getAllowedFormatsMap().get(extension).contains(inputFormat)) {
			log.error("Content type mismatch. Detected: {}, Expected for extension {}: {}", 
					inputFormat, extension, fileStoreConfig.getAllowedFormatsMap().get(extension));
			throw new CustomException("EG_FILESTORE_INVALID_INPUT", "Inalvid input provided for file, the extension does not match the file format. Please upload any of the allowed formats : "
							+ fileStoreConfig.getAllowedKeySet());
		}
		log.debug("Content type validation passed for extension: {}", extension);
	}

	private void validateInputContentType(Artifact artifact){
		log.trace("Entering validateInputContentType method");
		MultipartFile file =  artifact.getMultipartFile();
		String contentType = file.getContentType();
		String extension = (FilenameUtils.getExtension(artifact.getMultipartFile().getOriginalFilename())).toLowerCase();
		log.debug("Validating input contentType: {} for extension: {}", contentType, extension);

		if (!fileStoreConfig.getAllowedFormatsMap().get(extension).contains(contentType)) {
			log.error("Invalid content type: {} for extension: {}. Allowed types: {}", 
					contentType, extension, fileStoreConfig.getAllowedFormatsMap().get(extension));
			throw new CustomException("EG_FILESTORE_INVALID_INPUT", "Invalid Content Type");
		}
		log.debug("Input content type validation passed for extension: {}", extension);
	}

	
	/*private void validateFilesToUpload(List<MultipartFile> filesToStore, String module, String tag, String tenantId) {
		if (CollectionUtils.isEmpty(filesToStore)) {
			throw new EmptyFileUploadRequestException(module, tag, tenantId);
		}
	}*/
	
	
}
