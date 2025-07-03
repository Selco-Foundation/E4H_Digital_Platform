package org.egov.processor.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.tracer.config.TracerConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

@Component
@Data
@Import({TracerConfiguration.class})
@NoArgsConstructor
@AllArgsConstructor
public class ProcessorConfiguration {

    @Value("${egov.filestore.host}")
    private String fileStoreHost;

    @Value("${egov.filestore.hls.upload.endpoint}")
    private String fileStoreHlsUploadEndpoint;

    @Value("${egov.filestore.upload.endpoint}")
    private String fileStoreUploadEndpoint;

    @Value("${ffprobe.path}")
    private String ffprobePath;
}
