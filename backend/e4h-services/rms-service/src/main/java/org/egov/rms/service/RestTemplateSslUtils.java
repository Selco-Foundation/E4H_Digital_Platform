package org.egov.rms.service;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

public class RestTemplateSslUtils {

    public static RestTemplate restTemplateAcceptingAllCerts() throws Exception {
        // 1) Trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) { }
                }
        };

        // 2) Install the all-trusting trust manager
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new SecureRandom());

        // 3) Create an HttpsURLConnection verifier that allows all hostnames
        HostnameVerifier allowAllHosts = (hostname, session) -> true;

        // 4) Configure a SimpleClientHttpRequestFactory to use the SSL socket factory
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory() {
            @Override
            protected void prepareConnection(java.net.HttpURLConnection connection, String httpMethod) {
                if (connection instanceof HttpsURLConnection) {
                    HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;
                    httpsConnection.setSSLSocketFactory(sslContext.getSocketFactory());
                    httpsConnection.setHostnameVerifier(allowAllHosts);
                }
                try {
                    super.prepareConnection(connection, httpMethod);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        };

        return new RestTemplate(requestFactory);
    }

    // usage example
//    public static void main(String[] args) throws Exception {
//        RestTemplate rt = restTemplateAcceptingAllCerts();
//        // now use rt.exchange(...) or rt.postForObject(...)
//    }
}
