package org.egov.wf;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.cache2k.Cache2kBuilder;
import org.cache2k.extra.spring.SpringCache2kCacheManager;
import org.egov.tracer.config.TracerConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableCaching
@Import({ TracerConfiguration.class })
public class Main {

    @Value("${app.timezone}")
    private String timeZone;

    @Value("${cache.expiry.workflow.minutes}")
    private int workflowExpiry;

    @Bean
    public ObjectMapper objectMapper(){
        return new ObjectMapper()
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .setTimeZone(TimeZone.getTimeZone(timeZone));
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    @Profile("!test")
    public CacheManager cacheManager(){
        return new SpringCache2kCacheManager().addCaches(b->b.name("businessService").expireAfterWrite(workflowExpiry, TimeUnit.MINUTES)
                .entryCapacity(10)).addCaches(b->b.name("roleTenantAndStatusesMapping").expireAfterWrite(workflowExpiry, TimeUnit.MINUTES)
                .entryCapacity(10));
    }

    @Bean
    public RestTemplate restTemplate() {
        trustSelfSignedSSL();
        return new RestTemplate();
    }

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
//            ex.printStackTrace();
        }
    }

}
