package org.egov.web.notification.sms.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jayway.jsonpath.*;
import lombok.extern.slf4j.*;
import org.apache.http.conn.ssl.*;
import org.apache.http.impl.client.*;
import org.egov.web.notification.sms.config.*;
import org.egov.web.notification.sms.models.*;
import org.springframework.asm.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.core.*;
import org.springframework.core.env.*;
import org.springframework.http.*;
import org.springframework.http.client.*;
import org.springframework.http.converter.*;
import org.springframework.http.converter.json.*;
import org.springframework.util.*;
import org.springframework.web.client.*;

import javax.annotation.*;
import javax.net.ssl.*;
import java.io.*;
import java.lang.reflect.Type;
import java.net.*;
import java.security.*;
import java.util.*;

@Slf4j
abstract public class BaseSMSService implements SMSService, SMSBodyBuilder {

    private static final String SMS_RESPONSE_NOT_SUCCESSFUL = "Sms response not successful";

    @Autowired
    protected RestTemplate restTemplate;

    @Autowired
    protected SMSProperties smsProperties;

    @Autowired
    protected Environment env;

    @PostConstruct
    public void init() {
        log.trace("Initializing BaseSMSService message converters");
        List<HttpMessageConverter<?>> converters = restTemplate.getMessageConverters();
        converters.remove(converters.stream().filter(c -> c.getClass().equals(MappingJackson2HttpMessageConverter.class)).findFirst().get());
        converters.add(new MappingJackson2HttpMessageConverter() {
            @Override
            protected void writeInternal(Object object, Type type, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
                if (object.getClass().equals(LinkedMultiValueMap.class)) {
                    LinkedMultiValueMap<?, ?> map = (LinkedMultiValueMap<?, ?>) object;
                    object = map.toSingleValueMap();
                }
                super.writeInternal(object, type, outputMessage);
            }
        });
        log.debug("BaseSMSService message converters initialized");
    }

    @Override
    public void sendSMS(Sms sms) {
        log.trace("sendSMS method invoked");
        log.info("Processing SMS send request for mobile number: {}", sms.getMobileNumber() != null ? sms.getMobileNumber().substring(0, Math.min(3, sms.getMobileNumber().length())) + "****" : "null");
        
        if (!sms.isValid()) {
            log.error("SMS validation failed - mobile number or message is empty");
            return;
        }

        String mobileNumber = sms.getMobileNumber();
        if (smsProperties.isNumberBlacklisted(mobileNumber)) {
            log.warn("SMS request rejected - mobile number is blacklisted");
            return;
        }

        if (!smsProperties.isNumberWhitelisted(mobileNumber)) {
            log.warn("SMS request rejected - mobile number is not in whitelist");
            return;
        }

        log.debug("SMS validation passed, proceeding to submit to external service");
        submitToExternalSmsService(sms);
        log.info("SMS send request processed successfully");
    }

    protected abstract void submitToExternalSmsService(Sms sms);

    protected <T> ResponseEntity<T> executeAPI(URI uri, HttpMethod method, HttpEntity<?> requestEntity, Class<T> type) {
        log.trace("executeAPI method invoked for URI: {}", uri);
        log.info("Executing {} request to SMS provider", method);
        
        ResponseEntity<T> res = (ResponseEntity<T>) restTemplate.exchange(uri, method, requestEntity, String.class);
        int statusCode = res.getStatusCodeValue();
        log.debug("Received response from SMS provider with status code: {}", statusCode);
        
        if (!isResponseValidated(res)) {
            log.error("SMS provider response validation failed - response does not contain expected content");
            throw new RuntimeException(SMS_RESPONSE_NOT_SUCCESSFUL);
        }

        if (smsProperties.getSmsErrorCodes().size() > 0 && isResponseCodeInKnownErrorCodeList(res)) {
            log.error("SMS provider returned known error code: {}", statusCode);
            throw new RuntimeException(SMS_RESPONSE_NOT_SUCCESSFUL);
        }

        if (smsProperties.getSmsSuccessCodes().size() > 0 && !isResponseCodeInKnownSuccessCodeList(res)) {
            log.warn("SMS provider response code {} not in known success codes list", statusCode);
            throw new RuntimeException(SMS_RESPONSE_NOT_SUCCESSFUL);
        }

        log.info("SMS provider API call completed successfully");
        return res;
    }

    protected boolean isResponseValidated(ResponseEntity<?> response) {
        log.trace("isResponseValidated method invoked");
        String responseString = response.getBody().toString();
        if (smsProperties.isVerifyResponse() && !responseString.contains(smsProperties.getVerifyResponseContains())) {
            log.debug("Response validation failed - expected content not found");
            return false;
        }
        log.debug("Response validation passed");
        return true;
    }

    protected boolean isResponseCodeInKnownErrorCodeList(ResponseEntity<?> response) {
        log.trace("isResponseCodeInKnownErrorCodeList method invoked");
        final String responseCode = Integer.toString(response.getStatusCodeValue());
        boolean isErrorCode = smsProperties.getSmsErrorCodes().stream().anyMatch(errorCode -> errorCode.equals(responseCode));
        log.debug("Response code {} is in error codes list: {}", responseCode, isErrorCode);
        return isErrorCode;
    }

    protected boolean isResponseCodeInKnownSuccessCodeList(ResponseEntity<?> response) {
        log.trace("isResponseCodeInKnownSuccessCodeList method invoked");
        final String responseCode = Integer.toString(response.getStatusCodeValue());
        boolean isSuccessCode = smsProperties.getSmsSuccessCodes().stream().anyMatch(successCode -> successCode.equals(responseCode));
        log.debug("Response code {} is in success codes list: {}", responseCode, isSuccessCode);
        return isSuccessCode;
    }

    public MultiValueMap<String, String> getSmsRequestBody(Sms sms) {
        log.trace("getSmsRequestBody method invoked");
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        Map<String, String> configMap = smsProperties.getConfigMap();
        log.debug("Building SMS request body with {} configuration entries", configMap.size());

        for (String key : configMap.keySet()) {
            String value = configMap.get(key);
            resolveConfigEntry(key, value, sms).ifPresent(resolved -> map.add(key, resolved));
        }

        log.debug("SMS request body built with {} entries", map.size());
        return map;
    }

    private Optional<String> resolveConfigEntry(String key, String value, Sms sms) {
        if (!value.startsWith("$")) {
            return Optional.of(value);
        }
        return resolvePlaceholderValue(key, value, sms);
    }

    private Optional<String> resolvePlaceholderValue(String key, String value, Sms sms) {
        switch (value) {
            case "$username":
                return Optional.of(smsProperties.getUsername());
            case "$password":
                log.debug("Password placeholder resolved for key: {}", key);
                return Optional.of(smsProperties.getPassword());
            case "$senderid":
                return Optional.of(smsProperties.getSenderid());
            case "$mobileno":
                log.debug("Mobile number placeholder resolved");
                return Optional.of(smsProperties.getMobileNumberPrefix() + sms.getMobileNumber());
            case "$message":
                log.debug("Message placeholder resolved, message length: {}",
                        sms.getMessage() != null ? sms.getMessage().length() : 0);
                return Optional.of(sms.getMessage());
            default:
                return resolveDynamicPlaceholder(value, sms);
        }
    }

    private Optional<String> resolveDynamicPlaceholder(String value, Sms sms) {
        String key = value.substring(1);
        if (env.containsProperty(key)) {
            return Optional.of(env.getProperty(key));
        }
        if (smsProperties.getExtraConfigMap().containsKey(key)) {
            return Optional.of(smsProperties.getExtraConfigMap().get(key));
        }
        if (smsProperties.getCategoryMap().containsKey(key)) {
            return resolveCategoryValue(key, sms);
        }
        return Optional.of(value);
    }

    private Optional<String> resolveCategoryValue(String placeholderKey, Sms sms) {
        Map<String, String> categoryValue = smsProperties.getCategoryMap().get(placeholderKey);
        if (categoryValue == null) {
            return Optional.empty();
        }
        if (sms.getCategory() == null && categoryValue.containsKey("*")) {
            return Optional.of(categoryValue.get("*"));
        }
        if (sms.getCategory() != null) {
            String categoryKey = sms.getCategory().toString();
            if (categoryValue.containsKey(categoryKey)) {
                return Optional.of(categoryValue.get(categoryKey));
            }
            if (categoryValue.containsKey("*")) {
                return Optional.of(categoryValue.get("*"));
            }
        }
        return Optional.empty();
    }

    protected HttpEntity<MultiValueMap<String, String>> getRequest(Sms sms) {
        log.trace("getRequest method invoked");
        final MultiValueMap<String, String> requestBody = getSmsRequestBody(sms);
        HttpHeaders headers = getHttpHeaders();
        log.debug("HTTP request entity created with {} headers", headers.size());
        return new HttpEntity<>(requestBody, headers);
    }

    protected HttpHeaders getHttpHeaders() {
        log.trace("getHttpHeaders method invoked");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(smsProperties.getContentType()));
        headers.setBasicAuth(smsProperties.getUsername(), smsProperties.getPassword());
        log.debug("HTTP headers created with content type: {}", smsProperties.getContentType());
        return headers;
    }

    @PostConstruct
    protected void setupSSL() {
        log.trace("setupSSL method invoked");
        if (!smsProperties.isVerifySSL()) {
            log.warn("SSL verification is disabled - using non-verifying SSL context");
            SSLContext ctx = null;
            try {
                ctx =  SSLContext.getInstance("SSL");
                ctx.init(null, null, SecureRandom.getInstance("SHA1PRNG"));
                log.debug("SSL context initialized without verification");
            } catch (NoSuchAlgorithmException e) {
                log.error("Failed to initialize SSL context - NoSuchAlgorithmException", e);
            } catch (KeyManagementException e) {
                log.error("Failed to initialize SSL context - KeyManagementException", e);
            }
            SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(ctx, new NoopHostnameVerifier());
            CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setHttpClient(httpClient);
            restTemplate.setRequestFactory(requestFactory);
            log.info("SSL setup completed with verification disabled");
        } else {
            log.debug("SSL verification is enabled - using default SSL context");
        }
    }

}
