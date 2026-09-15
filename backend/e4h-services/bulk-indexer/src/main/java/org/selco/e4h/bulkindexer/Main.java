package org.selco.e4h.bulkindexer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.tracer.config.TracerConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.web.client.RestTemplate;

import java.util.TimeZone;

@Import({TracerConfiguration.class})
@EnableKafka
@SpringBootApplication
@PropertySource("classpath:application.properties")
@Configuration
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    /**
     * TLS verification is left at the JVM default on purpose.
     *
     * <p>An ES8 cluster with security enabled serves https with a self-signed certificate. Rather
     * than installing a trust-all {@code X509TrustManager} (which would also disable verification
     * for every other outbound call this JVM makes, including the mapping-config fetch), mount the
     * cluster CA and point the JVM at it:
     *
     * <pre>
     *   JAVA_TOOL_OPTIONS: >-
     *     -Djavax.net.ssl.trustStore=/etc/es-certs/truststore.jks
     *     -Djavax.net.ssl.trustStorePassword=$(TRUSTSTORE_PASSWORD)
     * </pre>
     *
     * For a cluster reachable over plain http inside the namespace, set
     * {@code egov.bulkindexer.es.host.name=http://elasticsearch-data-v1} and TLS is not involved.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .setTimeZone(TimeZone.getTimeZone("UTC"));
        return objectMapper;
    }
}
