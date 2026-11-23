package org.selco.e4h.bulkindexer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.tracer.config.TracerConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.TimeZone;

@Import({TracerConfiguration.class})
@EnableKafka
@SpringBootApplication
@PropertySource("classpath:application.properties")
@Configuration
public class Main {

    @Value("${egov.bulkindexer.es.connect-timeout-ms}")
    private int esConnectTimeoutMs;

    @Value("${egov.bulkindexer.es.read-timeout-ms}")
    private int esReadTimeoutMs;


    /**
     * ES8 cluster default configuration with security enabled forces the use of https for communication to the ES cluster.
     * This function is used to accept the self signed certificates from the ES8 cluster so SSLCertificateException is not t hrown.
     * The ideal way to solve this is to import the self signed certificates into the JKS.
     */

    public static void trustSelfSignedSSL() {
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            X509TrustManager tm = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            ctx.init(null, new TrustManager[]{tm}, null);
            SSLContext.setDefault(ctx);

            // Disable hostname verification
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession sslSession) {
                    return true;
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    /**
     * HTTP client for Elasticsearch.
     *
     * <p>TLS trust is whatever the JVM default SSLContext holds. An ES8 cluster with security
     * enabled presents a certificate issued by its own CA, which the default trust store does not
     * know, so calls fail with {@code PKIX path building failed} unless that default is changed at
     * startup — see {@code im-services-analytics}' {@code Main.trustSelfSignedSSL()}.
     *
     * <p>The timeouts are unrelated to TLS and deliberately kept: without them an unresponsive
     * cluster stalls the Kafka consumer thread indefinitely.
     */
    @Bean
    public RestTemplate restTemplate() {
        trustSelfSignedSSL();
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(esConnectTimeoutMs);
        requestFactory.setReadTimeout(esReadTimeoutMs);
        return new RestTemplate(requestFactory);
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .setTimeZone(TimeZone.getTimeZone("UTC"));
        return objectMapper;
    }
}
