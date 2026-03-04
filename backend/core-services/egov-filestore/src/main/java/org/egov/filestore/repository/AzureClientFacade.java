package org.egov.filestore.repository;

import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@Order(2)
public class AzureClientFacade implements ApplicationRunner{
	
	@Value("${azure.defaultEndpointsProtocol}")
	private String defaultEndpointsProtocol;
	
	@Value("${azure.accountName}")
	private String accountName;
	
	@Value("${azure.accountKey}")
	private String accountKey;
	
	@Value("${isAzureStorageEnabled}")
	private Boolean isAzureEnabled;
	
	private static CloudBlobClient cloudBlobClient;
	
	@Override
	public void run(ApplicationArguments arg0) throws Exception {
		log.trace("Entering run method for AzureClientFacade");
		if(isAzureEnabled) {
			log.info("Azure storage is enabled, initializing Azure client");
			initializeAzureClient();
		} else {
			log.info("Azure storage is disabled, skipping Azure client initialization");
		}
	}
	
	/**
	 * Intializes the azure client
	 * 
	 */
	public void initializeAzureClient() {
		log.trace("Entering initializeAzureClient method");
		log.info("Initializing Azure Blob Storage client");
		StringBuilder storageConnectionString = new StringBuilder();
		storageConnectionString.append("DefaultEndpointsProtocol=").append(defaultEndpointsProtocol).append(";")
				.append("AccountName=").append(accountName).append(";").append("AccountKey=").append(accountKey);
		CloudStorageAccount storageAccount = null;
		CloudBlobClient blobClient = null;
		try {
			log.debug("Parsing Azure storage connection string");
			storageAccount = CloudStorageAccount.parse(storageConnectionString.toString());
			if(null != storageAccount) {
				log.debug("Creating Azure Blob client");
				blobClient = storageAccount.createCloudBlobClient();
				log.info("Azure Blob Storage client initialized successfully");
			} else {
				log.error("Failed to parse Azure storage account");
			}
		}catch(Exception e) {
			log.error("Error initializing Azure client", e);
			throw new CustomException("WG_WF_CLIENT_INITIALIZE_ERROR",e.getMessage());
		}	
		cloudBlobClient = blobClient;
	}
	
	public CloudBlobClient getAzureClient() {
		log.trace("Entering getAzureClient method");
		if (cloudBlobClient == null) {
			log.warn("Azure client is null, may need initialization");
		}
		return cloudBlobClient;
	}
	
	
}
