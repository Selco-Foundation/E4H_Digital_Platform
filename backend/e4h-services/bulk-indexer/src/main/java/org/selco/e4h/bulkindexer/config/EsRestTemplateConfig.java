package org.selco.e4h.bulkindexer.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Collection;

/**
 * Builds the RestTemplate used to talk to Elasticsearch.
 *
 * <p>An ES8 cluster with security enabled serves https with a certificate issued by the cluster's
 * own CA, which the JVM default trust store does not know — connections fail with
 * {@code PKIX path building failed}.
 *
 * <p>The fix is to trust that CA explicitly: mount the cluster's {@code ca.crt} and set
 * {@code egov.bulkindexer.es.ca-certificate.path}. Note this is deliberately <em>not</em> a
 * trust-all {@code X509TrustManager} and deliberately <em>not</em> {@code SSLContext.setDefault} —
 * the custom trust applies only to connections made by this RestTemplate, so certificate
 * verification stays intact and an attacker with a self-signed certificate is still rejected.
 *
 * <p>Leaving the property empty falls back to the JVM default trust store, which is correct when
 * ES is reached over plain http or presents a publicly-trusted certificate.
 */
@Slf4j
@Configuration
public class EsRestTemplateConfig {

    @Value("${egov.bulkindexer.es.ca-certificate.path:}")
    private String caCertificatePath;

    @Value("${egov.bulkindexer.es.connect-timeout-ms}")
    private int connectTimeoutMs;

    @Value("${egov.bulkindexer.es.read-timeout-ms}")
    private int readTimeoutMs;

    @Bean
    public RestTemplate restTemplate() {
        if (caCertificatePath == null || caCertificatePath.isBlank()) {
            log.info("No ES CA certificate configured — using the JVM default trust store. "
                    + "If ES serves https with a cluster-issued certificate, set "
                    + "egov.bulkindexer.es.ca-certificate.path or PKIX validation will fail.");
            return new RestTemplate(timeouts(new SimpleClientHttpRequestFactory()));
        }
        try {
            SSLSocketFactory socketFactory = socketFactoryTrusting(caCertificatePath);
            log.info("Trusting Elasticsearch certificates issued by the CA at {}", caCertificatePath);
            return new RestTemplate(timeouts(new CaPinnedRequestFactory(socketFactory)));
        } catch (Exception e) {
            // Fail fast: booting with unusable TLS config would silently drop every batch.
            throw new IllegalStateException(
                    "Failed to load the Elasticsearch CA certificate from " + caCertificatePath, e);
        }
    }

    private SimpleClientHttpRequestFactory timeouts(SimpleClientHttpRequestFactory factory) {
        // Without these a hung cluster stalls the consumer thread indefinitely.
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);
        return factory;
    }

    /** Applies the CA-pinned socket factory to https connections only. */
    private static final class CaPinnedRequestFactory extends SimpleClientHttpRequestFactory {

        private final SSLSocketFactory socketFactory;

        private CaPinnedRequestFactory(SSLSocketFactory socketFactory) {
            this.socketFactory = socketFactory;
        }

        @Override
        protected void prepareConnection(HttpURLConnection connection, String httpMethod)
                throws IOException {
            if (connection instanceof HttpsURLConnection httpsConnection) {
                httpsConnection.setSSLSocketFactory(socketFactory);
            }
            super.prepareConnection(connection, httpMethod);
        }
    }

    /**
     * Builds a socket factory trusting exactly the CA certificate(s) in a PEM file — typically the
     * {@code ca.crt} key of the Elasticsearch cluster's certificate secret.
     */
    private static SSLSocketFactory socketFactoryTrusting(String pemPath) throws Exception {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null);

        Collection<? extends Certificate> certificates;
        try (InputStream in = new BufferedInputStream(Files.newInputStream(Path.of(pemPath)))) {
            certificates = certificateFactory.generateCertificates(in);
        }
        if (certificates.isEmpty()) {
            throw new IllegalStateException("No X.509 certificates found in " + pemPath);
        }
        int index = 0;
        for (Certificate certificate : certificates) {
            trustStore.setCertificateEntry("es-ca-" + index++, certificate);
        }

        TrustManagerFactory trustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
        return sslContext.getSocketFactory();
    }
}
