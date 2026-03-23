package org.egov.asset.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.asset.config.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

import static org.egov.asset.config.ServiceConstants.*;

@Slf4j
@Component
public class UrlShortenerUtil {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private Configuration configs;


    public String getShortenedUrl(String url) {
        log.trace("UrlShortenerUtil::getShortenedUrl called");
        log.debug("Shortening URL | url={}", url);
        if (StringUtils.isBlank(url)) {
            log.warn("Empty URL provided for shortening");
            return url;
        }

        HashMap<String, String> body = new HashMap<>();
        body.put(URL, url);
        try {
            String endpoint = configs.getUrlShortnerHost() + configs.getUrlShortnerEndpoint();
            log.debug("Calling URL shortener service | endpoint={}", endpoint);
            String res = restTemplate.postForObject(endpoint, body, String.class);

            if (StringUtils.isEmpty(res)) {
                log.warn("Empty response from URL shortener service | url={}", url);
                return url;
            } else return res;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("{}: {} - HTTP error: {}", URL_SHORTENING_ERROR_CODE, URL_SHORTENING_ERROR_MESSAGE, url, e);
            return url;
        } catch (ResourceAccessException e) {
            log.error("{}: {} - Connection error: {}", URL_SHORTENING_ERROR_CODE, URL_SHORTENING_ERROR_MESSAGE, url, e);
            return url;
        } catch (RestClientException e) {
            log.error("{}: {} - Error: {}", URL_SHORTENING_ERROR_CODE, URL_SHORTENING_ERROR_MESSAGE, url, e);
            return url;
        }
    }


}