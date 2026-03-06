package facility;


import org.egov.tracer.config.TracerConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@Import({TracerConfiguration.class})
@SpringBootApplication
//@EnableCaching
public class Main {

    /**
     * ES8 cluster default configuration with security enabled forces the use of https for communication to the ES cluster.
     * This function is used to accept the self signed certificates from the ES8 cluster so SSLCertificateException is not thrown.
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

    public static void main(String[] args) throws Exception {
        SpringApplication.run(facility.Main.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        trustSelfSignedSSL();
        return new RestTemplate();
    }

}
