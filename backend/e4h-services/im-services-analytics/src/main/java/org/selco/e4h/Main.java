package org.selco.e4h;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.egov.tracer.config.TracerConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.TimeZone;

@Import({TracerConfiguration.class})
@EnableKafka
@EnableScheduling
@SpringBootApplication
@PropertySource("classpath:application.properties")
@Configuration
public class Main {

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

	public static void main(String[] args) throws Exception {
		SpringApplication.run(Main.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
		trustSelfSignedSSL();
		return new RestTemplate();
	}

	@Bean
	@Qualifier("objectMapper")
	public ObjectMapper objectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).setTimeZone(TimeZone.getTimeZone("UTC"));
		objectMapper.registerModule(new JavaTimeModule());
		return objectMapper;
	}

}
