package org.selco.e4h.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.selco.e4h.web.models.CarbonEmissionKafkaMessage;
import org.selco.e4h.service.CarbonEmissionBatchService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CarbonEmissionListener {

    private final CarbonEmissionBatchService batchService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${kafka.topics.carbon-emission-calculate:carbon-emission-calculate}",
            groupId = "${spring.kafka.consumer.group-id}-carbon")
    public void onMessage(String payload) {
        try {
            CarbonEmissionKafkaMessage message = objectMapper.readValue(payload, CarbonEmissionKafkaMessage.class);
            if (message.getMonth() == null || message.getYear() == null) {
                log.error("Invalid carbon-emission-calculate message: {}", payload);
                return;
            }
            RequestInfo requestInfo = new RequestInfo();
            requestInfo.setMsgId("co2-carbon-emission-calculate");
            batchService.process(message, requestInfo);
        } catch (Exception e) {
            log.error("Failed to process carbon-emission-calculate message: {}", payload, e);
        }
    }
}
