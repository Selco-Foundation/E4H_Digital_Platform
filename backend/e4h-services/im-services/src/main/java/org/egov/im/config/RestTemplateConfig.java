package org.egov.im.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Configuration
public class RestTemplateConfig {

    @Value("${rest.template.connect.timeout:5000}")
    private int connectTimeout;

    @Value("${rest.template.read.timeout:10000}")
    private int readTimeout;

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        
        log.info("RestTemplate configured with connect timeout: {}ms, read timeout: {}ms", 
                connectTimeout, readTimeout);
        
        return new RestTemplate(factory);
    }
} 