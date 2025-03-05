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

}