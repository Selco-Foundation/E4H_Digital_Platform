package org.egov.filestore.repository.impl;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;

import org.egov.filestore.config.FileStoreConfig;
import org.egov.tracer.model.CustomException;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.imgscalr.Scalr.Mode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.SharedAccessBlobPolicy;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CloudFileMgrUtils {

	@Autowired
	private FileStoreConfig fileStoreConfig;

	@Value("${azure.sas.expiry.time.in.secs}")
	private Integer azureSASExpiryinSecs;

	/**
	 * This method creates different versions of an image. A single image will be
	 * stored in small, medium and large formats along with the original image. This
	 * is to facililate fasters searches on the app
	 * 
	 * @param inputStream
	 * @param fileName
	 * @return
	 */
	public Map<String, BufferedImage> createVersionsOfImage(InputStream inputStream, String fileName) {
		log.trace("Entering createVersionsOfImage method for fileName: {}", fileName);
		log.info("Creating thumbnail versions of image: {}", fileName);
		
		Map<String, BufferedImage> mapOfImagesAndPaths = new HashMap<>();
		BufferedImage largeImage = null;
		BufferedImage mediumImg = null;
		BufferedImage smallImg = null;
		try {
			BufferedImage originalImage = ImageIO.read(inputStream);
			
			if (null == originalImage) {
				log.error("Image source unavailable for fileName: {}", fileName);
				Map<String, String> map = new HashMap<>();
				map.put("Image Source Unavailable", "Image File present in upload request is Invalid/Not Readable");
				throw new CustomException(map);
			}
			log.debug("Original image read successfully, dimensions: {}x{}", 
					originalImage.getWidth(), originalImage.getHeight());
			
			log.debug("Resizing image to large version: {}px width", fileStoreConfig.getLargeWidth());
			largeImage = Scalr.resize(originalImage, Method.QUALITY, Mode.AUTOMATIC, fileStoreConfig.getLargeWidth(), null,
					Scalr.OP_ANTIALIAS);
			log.debug("Resizing image to medium version: {}px width", fileStoreConfig.getMediumWidth());
			mediumImg = Scalr.resize(originalImage, Method.QUALITY, Mode.AUTOMATIC, fileStoreConfig.getMediumWidth(), null,
					Scalr.OP_ANTIALIAS);
			log.debug("Resizing image to small version: {}px width", fileStoreConfig.getSmallWidth());
			smallImg = Scalr.resize(originalImage, Method.QUALITY, Mode.AUTOMATIC, fileStoreConfig.getSmallWidth(), null,
					Scalr.OP_ANTIALIAS);

			int lastIndex = fileName.length();
			String replaceString = fileName.substring(fileName.lastIndexOf('.'), lastIndex);

			mapOfImagesAndPaths.put(fileName.replace(replaceString, fileStoreConfig.get_large() + replaceString), largeImage);
			mapOfImagesAndPaths.put(fileName.replace(replaceString, fileStoreConfig.get_medium() + replaceString), mediumImg);
			mapOfImagesAndPaths.put(fileName.replace(replaceString, fileStoreConfig.get_small() + replaceString), smallImg);

			log.info("Successfully created {} thumbnail versions of image: {}", mapOfImagesAndPaths.size(), fileName);
		} catch (Exception e) {
			log.error("Error while creating different versions of the image for fileName: {}", fileName, e);
		} finally {
			if (largeImage != null) largeImage.flush();
			if (mediumImg != null) mediumImg.flush();
			if (smallImg != null) smallImg.flush();
		}

		return mapOfImagesAndPaths;
	}

	/**
	 * Generates SAS tokens for the given URI, this token is used to access files
	 * from Azure:
	 * sr = resource to be accessed 
	 * sig = HMAC hash used as signature 
	 * se = expiry time of the token 
	 * sp = permission granted to the client
	 * 
	 * Check -
	 * https://docs.microsoft.com/en-us/azure/storage/blobs/storage-dotnet-shared-access-signature-part-2
	 * 
	 * @param absolutePath
	 * @param azureBlobClient
	 * @return
	 */
	public String generateSASToken(CloudBlobClient azureBlobClient, String absolutePath) {
		log.trace("Entering generateSASToken method for absolutePath: {}", absolutePath);
		String sasUrl = null;
		try {
			int index = absolutePath.indexOf('/');
			String containerName = absolutePath.substring(0, index);
			String fileNameWithPath = absolutePath.substring(index + 1, absolutePath.length());
			log.debug("Generating SAS token for container: {}, fileName: {}", containerName, fileNameWithPath);
			CloudBlobContainer container = azureBlobClient.getContainerReference(containerName);
			CloudBlockBlob blob = (CloudBlockBlob) container.getBlobReferenceFromServer(fileNameWithPath);
			SharedAccessBlobPolicy sasConstraints = new SharedAccessBlobPolicy();
			sasConstraints.setSharedAccessStartTime(new Date(System.currentTimeMillis()));
			sasConstraints
					.setSharedAccessExpiryTime(new Date(System.currentTimeMillis() + (azureSASExpiryinSecs * 1000)));
			sasConstraints.setPermissionsFromString("r");
			log.debug("SAS token expiry set to {} seconds", azureSASExpiryinSecs);
			String sasBlobToken = blob.generateSharedAccessSignature(sasConstraints, null);
			sasUrl = sasBlobToken;
			log.debug("SAS token generated successfully for absolutePath: {}", absolutePath);
		} catch (Exception e) {
			log.error("Error while generating SAS token for absolutePath: {}", absolutePath, e);
		}
		return sasUrl;
	}

	/**
	 * HMAC hash generation using SHA256 and a secret key.
	 * 
	 * @param key
	 * @param input
	 * @return
	 */
	private static String getHMAC256(String key, String input) {
		log.trace("Entering getHMAC256 method");
		Mac sha256_HMAC = null;
		String hash = null;
		try {
			sha256_HMAC = Mac.getInstance("HmacSHA256");
			SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(), "HmacSHA256");
			sha256_HMAC.init(secret_key);
			Encoder encoder = Base64.getEncoder();
			hash = new String(encoder.encode(sha256_HMAC.doFinal(input.getBytes("UTF-8"))));
			log.debug("HMAC256 hash generated successfully");
		} catch (Exception e) {
			log.error("Exception while generating hash for the SAS token", e);
		}

		return hash;
	}

	/**
	 * Checks if the file is an image belonging to one of the mentioned
	 * imageFormats.
	 * 
	 * @param filePath
	 * @return
	 */
	public Boolean isFileAnImage(String filePath) {
		log.trace("Entering isFileAnImage method for filePath: {}", filePath);
		Boolean isFileAnImage = false;
		if (filePath.split("[\\.]").length > 1) {
			String extension = filePath.substring(filePath.lastIndexOf('.') + 1, filePath.length());
			isFileAnImage = fileStoreConfig.getImageFormats().contains(extension);
			log.debug("File isImage: {} for extension: {}", isFileAnImage, extension);
		} else {
			log.debug("File path does not contain extension: {}", filePath);
		}
		return isFileAnImage;
	}

}