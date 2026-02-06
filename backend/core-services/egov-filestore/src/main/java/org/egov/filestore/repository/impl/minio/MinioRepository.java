package org.egov.filestore.repository.impl.minio;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import io.minio.PutObjectArgs;
import io.minio.errors.*;
import org.apache.commons.io.FilenameUtils;
import org.egov.filestore.config.FileStoreConfig;
import org.egov.filestore.config.Properties;
import org.egov.filestore.domain.model.FileLocation;
import org.egov.filestore.persistence.entity.Artifact;
import org.egov.filestore.repository.CloudFilesManager;
import org.egov.filestore.repository.impl.CloudFileMgrUtils;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@ConditionalOnProperty(value = "isS3Enabled", havingValue = "true")
public class MinioRepository implements CloudFilesManager {

	private static final String ERROR_IN_CONFIGURATION = "Error in Configuration";

	@Autowired
	private MinioClient minioClient;
	
	@Autowired
	private MinioConfig minioConfig;

	@Autowired
	private CloudFileMgrUtils util;
	
	@Autowired
	private FileStoreConfig fileStoreConfig;

	@Autowired
	private Properties properties;

	@Override
	public void saveFiles(List<org.egov.filestore.domain.model.Artifact> artifacts) {
		log.trace("Entering saveFiles method with artifactCount: {}", artifacts.size());
		log.info("Saving {} files to MinIO", artifacts.size());

		List<org.egov.filestore.persistence.entity.Artifact> persistList = new ArrayList<>();
		artifacts.forEach(artifact -> {
			log.trace("Processing artifact: {}", artifact.getFileLocation().getFileName());
			FileLocation fileLocation = artifact.getFileLocation();
			String completeName = fileLocation.getFileName();
			int index = completeName.indexOf('/');
			String fileNameWithPath = completeName.substring(index + 1, completeName.length());
			log.debug("Uploading file to MinIO: {}", fileNameWithPath);
			push(artifact.getMultipartFile(), fileNameWithPath);

			if (artifact.getThumbnailImages() != null && !artifact.getThumbnailImages().isEmpty()) {
				log.debug("Uploading {} thumbnail images for file: {}", 
						artifact.getThumbnailImages().size(), fileNameWithPath);
				pushThumbnailImages(artifact);
			}

			fileLocation.setFileSource(minioConfig.getSource());
			persistList.add(mapToEntity(artifact));
		});
		log.info("Successfully saved {} files to MinIO", persistList.size());
	}

	private void push(MultipartFile multipartFile, String fileNameWithPath) {
		log.trace("Entering push method for fileName: {}", fileNameWithPath);
		pushWithRetry(multipartFile, fileNameWithPath, properties.getVideoUploadRetry());
	}

	private void pushWithRetry(MultipartFile multipartFile, String fileNameWithPath, int retriesLeft) {
		try (InputStream is = multipartFile.getInputStream()) {
			long fileSize = multipartFile.getSize(); // Use the size directly from MultipartFile

			// Build the PutObjectArgs for MinIO upload
			PutObjectArgs putObjectArgs = PutObjectArgs.builder()
					.bucket(minioConfig.getBucketName())
					.object(fileNameWithPath)
					.stream(is, fileSize, -1) // -1 for auto-detection of part size
					.contentType(multipartFile.getContentType()) // Set content type from MultipartFile
					.build();

			log.info("Uploading file: {} to MinIO bucket: {}", fileNameWithPath, minioConfig.getBucketName());
			minioClient.putObject(putObjectArgs);

			log.debug("Upload successful for file: {}", fileNameWithPath);

		} catch (IOException e) {
			if (retriesLeft > 0) {
				log.warn("IOException occurred during file upload. Retries left: {}. Retrying...", retriesLeft, e);
				try {
					Thread.sleep(properties.getMinioRetryDelayMs()); // Configurable delay between retries
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					throw new CustomException("INTERRUPTED", "Thread interrupted during retry delay: " + ie.getMessage());
				}
				pushWithRetry(multipartFile, fileNameWithPath, retriesLeft - 1);
			} else {
				log.error("Max retries reached for file: {}. IOException occurred while reading or uploading file", fileNameWithPath, e);
				throw new CustomException("IOEXCEPTION", "IOException after retries: " + e.getMessage());
			}
		} catch (MinioException | InvalidKeyException | IllegalArgumentException | NoSuchAlgorithmException e) {
			log.error("Error occurred while uploading file: {}", fileNameWithPath, e);
			throw new CustomException(ERROR_IN_CONFIGURATION, e.getMessage());
		}
	}

	private void push(InputStream is, long contentLength, String contentType, String fileNameWithPath) {
		log.trace("Entering push method (stream) for fileName: {}", fileNameWithPath);
		pushWithRetry(is, contentLength, contentType, fileNameWithPath, properties.getVideoUploadRetry());
	}

	private void pushWithRetry(InputStream is, long contentLength, String contentType, String fileNameWithPath, int retriesLeft) {
		try {
			long fileSize = is.available();
			PutObjectArgs.Builder putObjectArgsBuilder = PutObjectArgs.builder()
					.bucket(minioConfig.getBucketName())
					.object(fileNameWithPath)
					.stream(is, fileSize, -1) // Set part size to -1 for auto detection
					.contentType(contentType); // Change this as per your file's content type
			
			log.info("Uploading stream file: {} to MinIO bucket: {}", fileNameWithPath, minioConfig.getBucketName());
			minioClient.putObject(putObjectArgsBuilder.build());
			log.debug("Stream upload successful for file: {}", fileNameWithPath);

		} catch (IOException e) {
			if (retriesLeft > 0) {
				log.warn("IOException occurred during stream upload. Retries left: {}. Retrying...", retriesLeft, e);
				try {
					Thread.sleep(properties.getMinioRetryDelayMs()); // Configurable delay between retries
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					throw new CustomException("INTERRUPTED", "Thread interrupted during retry delay: " + ie.getMessage());
				}
				pushWithRetry(is, contentLength, contentType, fileNameWithPath, retriesLeft - 1);
			} else {
				log.error("Max retries reached for stream file: {}. IOException occurred", fileNameWithPath, e);
				throw new CustomException("IOEXCEPTION", "IOException after retries: " + e.getMessage());
			}
		} catch (MinioException | InvalidKeyException | IllegalArgumentException | NoSuchAlgorithmException e) {
			log.error("Error occurred while uploading stream file: {}", fileNameWithPath, e);
			throw new CustomException(ERROR_IN_CONFIGURATION, e.getMessage());
		}
	}

	private void pushThumbnailImages(org.egov.filestore.domain.model.Artifact artifact) {
		log.trace("Entering pushThumbnailImages method for fileName: {}", 
				artifact.getFileLocation().getFileName());
		try {
			int thumbnailCount = artifact.getThumbnailImages().size();
			log.debug("Uploading {} thumbnail images for fileName: {}", 
					thumbnailCount, artifact.getFileLocation().getFileName());

			for (Map.Entry<String, BufferedImage> entry : artifact.getThumbnailImages().entrySet()) {
				log.trace("Processing thumbnail: {}", entry.getKey());
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				ImageIO.write(entry.getValue(),
						FilenameUtils.getExtension(artifact.getMultipartFile().getOriginalFilename()), os);
				byte[] byteArray = os.toByteArray();
				log.debug("Thumbnail size: {} bytes for key: {}", byteArray.length, entry.getKey());
				ByteArrayInputStream is = new ByteArrayInputStream(byteArray);
				push(is, byteArray.length, artifact.getMultipartFile().getContentType(), entry.getKey());
				os.flush();
			}
			log.debug("Successfully uploaded {} thumbnail images", thumbnailCount);

		} catch (Exception ioe) {
			Map<String, String> map = new HashMap<>();
			log.error("Exception while uploading thumbnail images for fileName: {}", 
					artifact.getFileLocation().getFileName(), ioe);
			map.put("ERROR_MINIO_UPLOAD", "An error has occured while trying to upload image to filestore system .");
			throw new CustomException(map);
		}
	}

	@Override
	public Map<String, String> getFiles(List<Artifact> artifacts) {
		log.trace("Entering getFiles method with artifactCount: {}", artifacts.size());
		log.info("Retrieving signed URLs for {} artifacts", artifacts.size());

		Map<String, String> mapOfIdAndSASUrls = new HashMap<>();

		for(Artifact artifact : artifacts) {
			log.trace("Processing artifact: {}", artifact.getFileStoreId());
			String fileLocation = artifact.getFileLocation().getFileName();
			String fileName = fileLocation.
					substring(fileLocation.indexOf('/') + 1, fileLocation.length());
			log.debug("Extracted fileName: {} from fileLocation: {}", fileName, fileLocation);
			String signedUrl = getSignedUrl(fileName);
			if (util.isFileAnImage(artifact.getFileName())) {
				log.debug("File is an image, generating thumbnail signed URLs for: {}", fileName);
				try {
					signedUrl = setThumnailSignedURL(fileName, new StringBuilder(signedUrl));
					log.debug("Generated thumbnail signed URLs successfully");
				} catch (InvalidKeyException | ErrorResponseException | IllegalArgumentException
						| InsufficientDataException | InternalException | InvalidBucketNameException
						| InvalidExpiresRangeException | InvalidResponseException | NoSuchAlgorithmException
						| XmlParserException | IOException e) {
					log.error("Error generating thumbnail signed URLs for fileName: {}", fileName, e);
					// Continue with original signed URL
				}
			}
			
			mapOfIdAndSASUrls.put(artifact.getFileStoreId(), signedUrl);
		}
		log.info("Successfully generated {} signed URLs", mapOfIdAndSASUrls.size());
		return mapOfIdAndSASUrls;
	}
		
	private String setThumnailSignedURL(String fileName, StringBuilder url) throws InvalidKeyException, ErrorResponseException, IllegalArgumentException, InsufficientDataException, InternalException, InvalidBucketNameException, InvalidExpiresRangeException, InvalidResponseException, NoSuchAlgorithmException, XmlParserException, IOException {
		log.trace("Entering setThumnailSignedURL method for fileName: {}", fileName);
		log.debug("Generating thumbnail signed URLs for fileName: {}", fileName);
		String[] imageFormats = { fileStoreConfig.get_large(), fileStoreConfig.get_medium(), fileStoreConfig.get_small() };
		for (String  format : Arrays.asList(imageFormats)) {
			url.append(",");
			String replaceString = fileName.substring(fileName.lastIndexOf('.'), fileName.length());
			String path = fileName.replaceAll(replaceString, format + replaceString);
			log.debug("Generating signed URL for thumbnail format: {}, path: {}", format, path);
			url.append(getSignedUrl(path));
		}
		log.debug("Successfully generated thumbnail signed URLs for fileName: {}", fileName);
		return url.toString();
	}
	
	public String getSignedUrl(String fileName) {
		log.trace("Entering getSignedUrl method for fileName: {}", fileName);
		String signedUrl = null;
		try {
			signedUrl = minioClient.getPresignedObjectUrl(io.minio.http.Method.GET, minioConfig.getBucketName(), fileName,
					fileStoreConfig.getPreSignedUrlTimeOut(), new HashMap<String, String>());
			log.debug("Generated signed URL successfully for fileName: {}", fileName);
		} catch (InvalidKeyException | ErrorResponseException | IllegalArgumentException | InsufficientDataException
				| InternalException | InvalidBucketNameException | InvalidExpiresRangeException
				| InvalidResponseException | NoSuchAlgorithmException | XmlParserException | ServerException | IOException e) {
			log.error("Error generating signed URL for fileName: {}", fileName, e);
		}
        return signedUrl;
	}

	public Resource read(FileLocation fileLocation) {
		log.trace("Entering read method for fileName: {}, tenantId: {}", 
				fileLocation.getFileName(), fileLocation.getTenantId());
		log.info("Reading file from MinIO for fileName: {}, tenantId: {}", 
				fileLocation.getFileName(), fileLocation.getTenantId());

		Resource resource = null;
		File f = new File(fileLocation.getFileStoreId());
		File parentDir = f.getParentFile();
		if (parentDir != null && !parentDir.exists()) {
			parentDir.mkdirs(); // Create the directory (and any missing parent directories)
			log.debug("Created parent directory: {}", parentDir.getAbsolutePath());
		}

		if (fileLocation.getFileSource() == null || fileLocation.getFileSource().equals(minioConfig.getSource())) {
			String fileName = fileLocation.getFileName().substring(fileLocation.getFileName().indexOf('/') + 1,
					fileLocation.getFileName().length());
			log.debug("Extracted fileName: {} from fileLocation", fileName);

			try {
				log.info("Retrieving file from MinIO bucket: {}/{}", minioConfig.getBucketName(), fileName);
				minioClient.getObject(minioConfig.getBucketName(), fileName, f.getAbsolutePath());
				log.debug("File downloaded successfully to: {}", f.getAbsolutePath());
			} catch (InvalidKeyException | ErrorResponseException | IllegalArgumentException |
                     InsufficientDataException | InternalException | InvalidBucketNameException |
                     InvalidResponseException | NoSuchAlgorithmException | XmlParserException | IOException |
                     ServerException e) {
				log.error("Error while downloading the file from MinIO for fileName: {}, bucket: {}", 
						fileName, minioConfig.getBucketName(), e);
				Map<String, String> map = new HashMap<>();
				map.put("ERROR_MINIO_DOWNLOAD",
						"An error has occured while trying to download image from filestore system .");
				throw new CustomException(map);
			}
			resource = new FileSystemResource(Paths.get(f.getPath()).toFile());
			log.debug("File resource created successfully");
		} else {
			log.warn("File source mismatch, expected: {}, actual: {}", 
					minioConfig.getSource(), fileLocation.getFileSource());
		}
		return resource;
	}

	private Artifact mapToEntity(org.egov.filestore.domain.model.Artifact artifact) {
		log.trace("Entering mapToEntity method for fileStoreId: {}", artifact.getFileLocation().getFileStoreId());
		FileLocation fileLocation = artifact.getFileLocation();
		Artifact entity = Artifact.builder().fileStoreId(fileLocation.getFileStoreId()).fileName(fileLocation.getFileName())
				.contentType(artifact.getMultipartFile().getContentType()).module(fileLocation.getModule())
				.tag(fileLocation.getTag()).tenantId(fileLocation.getTenantId())
				.fileSource(fileLocation.getFileSource()).build();
		log.debug("Mapped artifact to entity for fileStoreId: {}", fileLocation.getFileStoreId());
		return entity;
	}

}
