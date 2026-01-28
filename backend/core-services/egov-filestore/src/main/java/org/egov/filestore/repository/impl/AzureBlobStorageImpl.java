package org.egov.filestore.repository.impl;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.egov.filestore.domain.model.Artifact;
import org.egov.filestore.domain.model.FileLocation;
import org.egov.filestore.repository.AzureClientFacade;
import org.egov.filestore.repository.CloudFilesManager;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.blob.BlobContainerPublicAccessType;
import com.microsoft.azure.storage.blob.BlobRequestOptions;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@ConditionalOnProperty(value = "isAzureStorageEnabled", havingValue = "true")
public class AzureBlobStorageImpl implements CloudFilesManager {

	private CloudBlobClient azureBlobClient;
	
	@Autowired
	private AzureClientFacade azureFacade;
	
	@Autowired
	private CloudFileMgrUtils util;
	
	@Value("${is.container.fixed}")
	private Boolean isContainerFixed;
	
	@Value("${fixed.container.name}")
	private String fixedContainerName;
	
	@Value("${azure.blob.host}")
	private String azureBlobStorageHost;
	
	@Value("${azure.accountName}")
	private String azureAccountName;
	
	@Value("${azure.accountKey}")
	private String azureAccountKey;
	
	@Value("${image.small}")
	private String _small;

	@Value("${image.medium}")
	private String _medium;

	@Value("${image.large}")
	private String _large;
	
	
	/**
	 * Azure specific implementation
	 * 
	 */
	@Override
	public void saveFiles(List<Artifact> artifacts) {
		log.trace("Entering saveFiles method with artifactCount: {}", artifacts.size());
		log.info("Saving {} files to Azure Blob Storage", artifacts.size());
		if(null == azureBlobClient)
			azureBlobClient = azureFacade.getAzureClient();
		
		artifacts.forEach(artifact -> {
			log.trace("Processing artifact: {}", artifact.getFileLocation().getFileName());
			CloudBlobContainer container= null;
			String completeName = artifact.getFileLocation().getFileName();
			int index = completeName.indexOf('/');
			String containerName = completeName.substring(0, index);
			String fileNameWithPath = completeName.substring(index + 1, completeName.length());
			log.debug("Extracted containerName: {}, fileNameWithPath: {}", containerName, fileNameWithPath);
			try {
				if(isContainerFixed) {
					log.debug("Using fixed container: {}", fixedContainerName);
					container = azureBlobClient.getContainerReference(fixedContainerName);
				} else {
					log.debug("Using dynamic container: {}", containerName);
					container = azureBlobClient.getContainerReference(containerName);
				}
				container.createIfNotExists(BlobContainerPublicAccessType.CONTAINER, new BlobRequestOptions(), new OperationContext());
				log.debug("Container ready for upload");
				
				Long contentLength = artifact.getMultipartFile().getSize();
				BufferedInputStream inputStream = new BufferedInputStream(artifact.getMultipartFile().getInputStream());
				log.debug("File size: {} bytes, contentType: {}", contentLength, artifact.getMultipartFile().getContentType());
				
				if(artifact.getMultipartFile().getContentType().startsWith("image/")) {
					String extension = FilenameUtils.getExtension(artifact.getMultipartFile().getOriginalFilename());
					log.debug("File is an image, uploading {} thumbnail versions", 
							artifact.getThumbnailImages() != null ? artifact.getThumbnailImages().size() : 0);
					// Removed generating versions of image because it's already available in thumbnailImages, and it's causing the issue because using same input stream
					Map<String, BufferedImage> mapOfImagesAndPaths = artifact.getThumbnailImages();
					for(String key: mapOfImagesAndPaths.keySet()) {
						log.trace("Uploading thumbnail: {}", key);
						upload(container, key, null, null, mapOfImagesAndPaths.get(key), extension);
						mapOfImagesAndPaths.get(key).flush();
					}
				}
				log.debug("Uploading main file: {}", fileNameWithPath);
				upload(container, fileNameWithPath, inputStream, contentLength, null, null);
				log.debug("File uploaded successfully to Azure Blob Storage");
				
			} catch (Exception e) {
				log.error("Exception while creating container or uploading file for fileName: {}", fileNameWithPath, e);
			}
			
		});
		log.info("Successfully saved {} files to Azure Blob Storage", artifacts.size());
	}
	
	/**
	 * There's a problem with this implementation: In case of images, we are trying to retrieve 4 different versions of the same file namely - 
	 * small, medium, large and the original. The path stored in the db is the path of the original file only, we are making suitable changes
	 * to that file path by appending some extensions to obtain file paths of the different versions. 
	 * TODO: This has to be fixed, we need to keep track of all these versions by storing their paths in the db separately instead of deriving them.
	 * 
	 * Secondly, once these paths are obtained, their SAS urls are being returned as comma separated values in a single string, this has to change to
	 * list of strings. We aren't taking this up because this will cause high impact on UI.
	 * TODO: Change comma separated string to list of strings and test it with UI once their changes are done.
	 */
	
	public Map<String, String> getFiles(Map<String, String> mapOfIdAndFilePath) {
		log.trace("Entering getFiles method (Map version) with fileCount: {}", mapOfIdAndFilePath.size());
		log.info("Retrieving SAS URLs for {} files from Azure Blob Storage", mapOfIdAndFilePath.size());
		if(null == azureBlobClient)
			azureBlobClient = azureFacade.getAzureClient();
		Map<String, String> mapOfIdAndSASUrls = new HashMap<>();
		mapOfIdAndFilePath.keySet().forEach(id -> {
			log.trace("Processing file with id: {}", id);
			if(util.isFileAnImage(mapOfIdAndFilePath.get(id))) {
				log.debug("File is an image, generating thumbnail SAS URLs for id: {}", id);
				StringBuilder url = new StringBuilder();
				/* Don't change the order of images within this if, it is index-based and UI will break.*/
				String[] imageFormats = {_large, _medium, _small};
				url.append(getSASURL(mapOfIdAndFilePath.get(id), util.generateSASToken(azureBlobClient, mapOfIdAndFilePath.get(id))));
				String replaceString = mapOfIdAndFilePath.get(id).substring(mapOfIdAndFilePath.get(id).lastIndexOf('.'),
						mapOfIdAndFilePath.get(id).length());
				for(String format: Arrays.asList(imageFormats)) {
					url.append(",");
					String path = mapOfIdAndFilePath.get(id);
					path = path.replaceAll(replaceString, format + replaceString);
					url.append(getSASURL(path, util.generateSASToken(azureBlobClient, path)));
				}
				mapOfIdAndSASUrls.put(id, url.toString());
			}else {
				log.debug("File is not an image, generating single SAS URL for id: {}", id);
				mapOfIdAndSASUrls.put(id, getSASURL(mapOfIdAndFilePath.get(id), util.generateSASToken(azureBlobClient, mapOfIdAndFilePath.get(id))));
			}
		});
		log.info("Successfully generated {} SAS URLs", mapOfIdAndSASUrls.size());
		return mapOfIdAndSASUrls;
	}
	
	
	/**
	 * Prepares the SASUrls for the resource on azure
	 * 
	 * @param path
	 * @param sasToken
	 * @return
	 */
	private String getSASURL(String path, String sasToken) {
		log.trace("Entering getSASURL method for path: {}", path);
		StringBuilder sasURL = new StringBuilder();
		String host = azureBlobStorageHost.replace("$accountName", azureAccountName);		
		sasURL.append(host).append("/").append(path).append("?").append(sasToken);
		log.debug("Generated SAS URL for path: {}", path);
		return sasURL.toString();
	}

	
	/**
	 * Uploads the file to Azure Blob Storage
	 * 
	 * @param container
	 * @param completePath
	 * @param file
	 * @param image
	 * @param extension
	 */
	public void upload(CloudBlobContainer container, String completePath, InputStream inputStream, Long contentLength, BufferedImage image, String extension) {
		log.trace("Entering upload method for path: {}", completePath);
		log.info("Uploading file to Azure Blob Storage: {}", completePath);
		try{
			if(null == inputStream && null != image) {
				log.debug("Uploading image buffer to Azure Blob Storage");
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				ImageIO.write(image, extension, os);
				CloudBlockBlob blob = container.getBlockBlobReference(completePath);
				blob.upload(new ByteArrayInputStream(os.toByteArray()), 8*1024*1024);
				log.debug("Image uploaded successfully to path: {}", completePath);
			}else {
				log.debug("Uploading input stream to Azure Blob Storage, contentLength: {}", contentLength);
				CloudBlockBlob blob = container.getBlockBlobReference(completePath);
				blob.upload(inputStream, contentLength);
				log.debug("File uploaded successfully to path: {}", completePath);
			}
			log.info("File upload completed for path: {}", completePath);

		}catch(Exception e) {
			log.error("Error uploading file to Azure Blob Storage for path: {}", completePath, e);
			throw new CustomException("WG_WF_UPLOAD_ERROR",e.getMessage());
		}
	}

	@Override
	public Map<String, String> getFiles(List<org.egov.filestore.persistence.entity.Artifact> artifacts) {
		log.trace("Entering getFiles method (List version) with artifactCount: {}", artifacts.size());
		log.info("Retrieving SAS URLs for {} artifacts from Azure Blob Storage", artifacts.size());
		if(null == azureBlobClient)
			azureBlobClient = azureFacade.getAzureClient();
		Map<String, String> mapOfIdAndSASUrls = new HashMap<>();
		for(org.egov.filestore.persistence.entity.Artifact artifact : artifacts) {
			log.trace("Processing artifact: {}", artifact.getFileStoreId());
			if (util.isFileAnImage(artifact.getFileName())) {
				log.debug("File is an image, generating thumbnail SAS URLs for fileStoreId: {}", artifact.getFileStoreId());
				StringBuilder url = new StringBuilder();
				/* Don't change the order of images within this if, it is index-based and UI will break.*/
				String[] imageFormats = {_large, _medium, _small};
				url.append(getSASURL(artifact.getFileName(), util.generateSASToken(azureBlobClient, artifact.getFileName())));
				String replaceString = artifact.getFileName().substring(artifact.getFileName().lastIndexOf('.'),
						artifact.getFileName().length());
				for (String format : Arrays.asList(imageFormats)) {
					url.append(",");
					String path = artifact.getFileName();
					path = path.replaceAll(replaceString, format + replaceString);
					url.append(getSASURL(path, util.generateSASToken(azureBlobClient, path)));
				}
				mapOfIdAndSASUrls.put(artifact.getFileStoreId(), url.toString());
			} else {
				log.debug("File is not an image, generating single SAS URL for fileStoreId: {}", artifact.getFileStoreId());
				mapOfIdAndSASUrls.put(artifact.getFileStoreId(), getSASURL(artifact.getFileName(), util.generateSASToken(azureBlobClient, artifact.getFileName())));
			}
		}
		log.info("Successfully generated {} SAS URLs", mapOfIdAndSASUrls.size());
		return mapOfIdAndSASUrls;
	}

	public Resource read(FileLocation fileLocation) {
		log.trace("Entering read method for fileName: {}, tenantId: {}", 
				fileLocation.getFileName(), fileLocation.getTenantId());
		log.info("Reading file from Azure Blob Storage for fileName: {}, tenantId: {}", 
				fileLocation.getFileName(), fileLocation.getTenantId());
		Resource resource = null;
		CloudBlobContainer container= null;
		File f = new File(fileLocation.getFileStoreId());
		if(null == azureBlobClient)
			azureBlobClient = azureFacade.getAzureClient();
		if (fileLocation.getFileSource().equals("AzureBlobStorage")) {
			try {
				String fileName = fileLocation.getFileName().substring(fileLocation.getFileName().indexOf('/') + 1,
						fileLocation.getFileName().length());
				int index = fileLocation.getFileName().indexOf('/');
				String containerName = fileLocation.getFileName().substring(0, index);
				log.debug("Extracted containerName: {}, fileName: {}", containerName, fileName);
				if(isContainerFixed) {
					log.debug("Using fixed container: {}", fixedContainerName);
					container = azureBlobClient.getContainerReference(fixedContainerName);
				} else {
					log.debug("Using dynamic container: {}", containerName);
					container = azureBlobClient.getContainerReference(containerName);
				}

				CloudBlockBlob blob = container.getBlockBlobReference(fileName);
				log.debug("Downloading blob from Azure Blob Storage");
				blob.download(new FileOutputStream(f.getName()));
				log.debug("File downloaded successfully to: {}", f.getName());
			}catch(Exception e) {
				log.error("Error reading file from Azure Blob Storage for fileName: {}, tenantId: {}", 
						fileLocation.getFileName(), fileLocation.getTenantId(), e);
				throw new CustomException("WG_WF_READ_ERROR",e.getMessage());
			}
			resource = new FileSystemResource(Paths.get(f.getPath()).toFile());
			log.info("File resource created successfully for fileName: {}", fileLocation.getFileName());
			return resource;
		} else {
			log.warn("File source is not AzureBlobStorage: {}", fileLocation.getFileSource());
			return null;
		}
	}
	
	
}
