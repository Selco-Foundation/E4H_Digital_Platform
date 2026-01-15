package org.egov.inbox.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.inbox.config.InboxConfiguration;
import org.egov.inbox.web.model.InboxSearchCriteria;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
@Slf4j
public class ElasticSearchRepository {

    private InboxConfiguration config;

    private ElasticSearchQueryBuilder queryBuilder;

    @Autowired
    private RestTemplate restTemplate;

    private ObjectMapper mapper;

    @Autowired
    public ElasticSearchRepository(InboxConfiguration config, ElasticSearchQueryBuilder queryBuilder, RestTemplate restTemplate, ObjectMapper mapper) {
        this.config = config;
        this.queryBuilder = queryBuilder;
        this.restTemplate = restTemplate;
        this.mapper = mapper;
    }


    /**
     * Searches records from elasticsearch based on the fuzzy search criteria
     *
     * @param criteria
     * @return
     */
    public Object elasticSearchApplications(InboxSearchCriteria criteria, List<String> uuids) {


        String url = getESURL(criteria);

        String searchQuery = queryBuilder.getSearchQuery(criteria, uuids);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(searchQuery, headers);
        ResponseEntity response = null;
        try {
            response = restTemplate.postForEntity(url, requestEntity, Object.class);

        } catch (HttpClientErrorException e) {
            log.error("HTTP client error while searching ES: status={} body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new CustomException("ES_SEARCH_ERROR", "Failed to fetch data from ES: " + e.getMessage());
        } catch (HttpServerErrorException e) {
            log.error("HTTP server error while searching ES: status={} body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new CustomException("ES_SEARCH_ERROR", "Failed to fetch data from ES: " + e.getMessage());
        } catch (ResourceAccessException e) {
            log.error("Network error while searching ES: ", e);
            throw new CustomException("ES_SEARCH_ERROR", "Network error while fetching data from ES: " + e.getMessage());
        } catch (RestClientException e) {
            log.error("Error while searching ES: ", e);
            throw new CustomException("ES_SEARCH_ERROR", "Failed to fetch data from ES: " + e.getMessage());
        }

        return response.getBody();

    }


    /**
     * Generates elasticsearch search url from application properties
     *
     * @return
     */
    private String getESURL(InboxSearchCriteria criteria) {

        StringBuilder builder = new StringBuilder(config.getIndexServiceHost());
        if (criteria.getProcessSearchCriteria().getModuleName().equals("ws-services"))
            builder.append(config.getEsWSIndex());
        else if (criteria.getProcessSearchCriteria().getModuleName().equals("sw-services")) {
            builder.append(config.getEsSWIndex());
        }
        builder.append(config.getIndexServiceHostSearchEndpoint());

        return builder.toString();
    }

}