package com.example.hfr.repository;


import com.example.hfr.kafka.Producer;
import com.example.hfr.web.models.FacilityCreateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class FacilityRepository {

    @Autowired
    private Producer producer;

    @Value("${facility.create.topic}")
    private String createTopic;

    public void pushCreateFacility(FacilityCreateRequest request) {
        producer.push(createTopic, request);
    }
}
