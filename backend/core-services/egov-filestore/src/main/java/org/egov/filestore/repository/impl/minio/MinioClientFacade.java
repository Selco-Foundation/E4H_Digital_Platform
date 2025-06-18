package org.egov.filestore.repository.impl.minio;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.egov.filestore.config.Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(value = "isS3Enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class MinioClientFacade {

	@Autowired
	private MinioConfig minioConfig;
	
	@Autowired
	private Properties properties;
	
	@Bean
	private MinioClient getMinioClient() {
		log.info("Initializing the minio client with optimized configurations for load testing");
		
		// Create optimized HTTP client for better load handling with configurable settings
		OkHttpClient httpClient = new OkHttpClient.Builder()
				.connectionPool(new ConnectionPool(
						properties.getMinioConnectionPoolMax(), 
						properties.getMinioConnectionPoolKeepAliveMinutes(), 
						TimeUnit.MINUTES))
				.connectTimeout(properties.getMinioConnectTimeoutSeconds(), TimeUnit.SECONDS)
				.writeTimeout(properties.getMinioWriteTimeoutSeconds(), TimeUnit.SECONDS)
				.readTimeout(properties.getMinioReadTimeoutSeconds(), TimeUnit.SECONDS)
				.retryOnConnectionFailure(true) // Retry on connection failures
				.build();

		log.info("MinIO client configured with: connectionPool={}, connectTimeout={}s, writeTimeout={}s, readTimeout={}s", 
				properties.getMinioConnectionPoolMax(),
				properties.getMinioConnectTimeoutSeconds(),
				properties.getMinioWriteTimeoutSeconds(),
				properties.getMinioReadTimeoutSeconds());

		return MinioClient.builder()
				.endpoint(minioConfig.getEndPoint())
				.credentials(minioConfig.getAccessKey(), minioConfig.getSecretKey())
				.region(minioConfig.getRegion())
				.httpClient(httpClient) // Use the optimized HTTP client
				.build();
	} 
}
