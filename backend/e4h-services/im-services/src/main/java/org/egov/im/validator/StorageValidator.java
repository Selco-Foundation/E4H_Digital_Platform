package org.egov.im.validator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.egov.im.config.IMConfiguration;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
@Component
@Slf4j
public class StorageValidator {

	private final IMConfiguration fileStoreConfig;

	public void validate(List<MultipartFile> files) {
		log.info("validating {} files", files.size());
		// Use configuration for maximum file count
		int maxFiles = fileStoreConfig.getMaxFileListSize() != null ? 
						fileStoreConfig.getMaxFileListSize() : 7;
		if(files.size() > maxFiles) {
			throw new CustomException("EG_FILE_LIST_EXCEEDED",
					String.format("Cannot upload more than %d files", maxFiles));
		}
		files.forEach(file -> {
			String extension =
					Objects.requireNonNull(FilenameUtils.getExtension(file
									.getOriginalFilename())).toLowerCase();

			validateFileExtension(extension);
			
			// Only validate video content type for video files
			if (isVideoFile(extension)) {
				try {
					validateVideoContentType(file.getInputStream(), extension);
				} catch (IOException e) {
					throw new CustomException("Error validating", e.getMessage());
				}
			}
			
            validateInputContentType(file, extension);
		});
	}
	
	private void validateFileExtension(String extension) {
        log.info("validating file extension {}", extension);
		Map<String, List<String>> fileExtension = fileStoreConfig.getAllowedFormatsMap();
		if(!fileExtension.containsKey(extension)) {
			throw new CustomException("EG_FILESTORE_INVALID_INPUT",
					String.format("Invalid input provided for file :  %s , please upload any of the allowed formats : ",
							fileStoreConfig.getAllowedKeySet()));
		}
	}

	private void validateVideoContentType(InputStream inputStream, String extension) {
        log.info("validating video content type, extension {}", extension);
		String detectedFormat;
		Tika tika = new Tika();
		try {
			detectedFormat = tika.detect(inputStream);
		} catch (IOException e) {
			throw new CustomException("EG_FILESTORE_PARSING_ERROR",
					String.format("Error parsing the uploaded video file: %s", e.getMessage()));
		}
		List<String> allowedFormats = fileStoreConfig.getAllowedFormatsMap().get(extension);
		if (allowedFormats == null || !allowedFormats.contains(detectedFormat)) {
			throw new CustomException("EG_FILESTORE_INVALID_INPUT",
					String.format("Invalid video format: detected %s, expected formats: %s", detectedFormat, allowedFormats));
		}
	}

	private void validateInputContentType(MultipartFile file, String extension){

		String contentType = file.getContentType();

		if (!fileStoreConfig.getAllowedFormatsMap().get(extension).contains(contentType)) {
			throw new CustomException("EG_FILESTORE_INVALID_INPUT", "Invalid Content Type");
		}
	}
	
	private boolean isVideoFile(String extension) {
        log.info("validating video file extension {}", extension);
		return extension.equals("mp4") || extension.equals("avi") || 
			   extension.equals("mov") || extension.equals("wmv");
	}

//	private void validateVideoSize(MultipartFile file) {
//		String contentType = file.getContentType();
//
//		if (contentType != null && contentType.startsWith("video/")) {
//			long maxSizeInBytes = fileStoreConfig.getMaxVideoSizeInMB() * 1024 * 1024; // Convert MB to Bytes
//			long fileSizeInBytes = file.getSize();
//
//			if (fileSizeInBytes > maxSizeInBytes) {
//				throw new CustomException("EG_FILESTORE_VIDEO_SIZE_EXCEEDED",
//						"File size exceeds the allowed limit of " + fileStoreConfig.getMaxVideoSizeInMB() + "MB.");
//			}
//		}
//	}
}
