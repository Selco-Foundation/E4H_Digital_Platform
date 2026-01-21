package org.egov.web.notification.sms.service.impl;


import lombok.extern.slf4j.*;
import org.egov.web.notification.sms.service.*;


import org.egov.web.notification.sms.models.Sms;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import org.springframework.http.*;

import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.*;


@Service
@Slf4j
@ConditionalOnProperty(value = "sms.provider.class", matchIfMissing = true, havingValue = "Generic")
public class GenericSMSServiceImpl extends BaseSMSService {

    @Value("${sms.url.dont_encode_url:true}")
    private boolean dontEncodeURL;


    protected void submitToExternalSmsService(Sms sms) {
        log.trace("submitToExternalSmsService method invoked for Generic SMS provider");
        try {
            String url = smsProperties.getUrl();
            log.debug("SMS provider URL: {}", url);
            log.debug("Request type: {}", smsProperties.requestType);

            if (smsProperties.requestType.equals("POST")) {
                log.info("Preparing POST request to SMS provider");
                HttpEntity<MultiValueMap<String, String>> request = getRequest(sms);
                log.debug("POST request entity created with {} body entries", request.getBody() != null ? request.getBody().size() : 0);

                executeAPI(URI.create(url), HttpMethod.POST, request, String.class);
                log.info("POST request to SMS provider completed");

            } else {
                log.info("Preparing GET request to SMS provider");
                final MultiValueMap<String, String> requestBody = getSmsRequestBody(sms);
                log.debug("GET request body contains {} query parameters", requestBody.size());

                URI final_url = UriComponentsBuilder.fromHttpUrl(url).queryParams(requestBody).build().encode().toUri();
                log.debug("Final GET URL constructed");

                executeAPI(final_url, HttpMethod.GET, null, String.class);
                log.info("GET request to SMS provider completed");
            }

        } catch (RestClientException e) {
            log.error("RestClientException occurred while sending SMS via Generic provider", e);
            throw e;
        }
    }


}
