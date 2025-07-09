package org.egov.filestore.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class Properties {

    @Value("${filename.length}")
    private Integer filenameLength;

    @Value("${filename.useletters}")
    private Boolean useLetters;

    @Value("${filename.usenumbers}")
    private Boolean useNumbers;

    @Value("${video.upload.retry}")
    private Integer videoUploadRetry;

    // MinIO HTTP Client Optimization Properties
    @Value("${minio.connection.pool.max}")
    private Integer minioConnectionPoolMax;

    @Value("${minio.connection.pool.keepalive.minutes}")
    private Integer minioConnectionPoolKeepAliveMinutes;

    @Value("${minio.connect.timeout.seconds}")
    private Integer minioConnectTimeoutSeconds;

    @Value("${minio.write.timeout.seconds}")
    private Integer minioWriteTimeoutSeconds;

    @Value("${minio.read.timeout.seconds}")
    private Integer minioReadTimeoutSeconds;

    @Value("${minio.retry.delay.ms}")
    private Integer minioRetryDelayMs;
}