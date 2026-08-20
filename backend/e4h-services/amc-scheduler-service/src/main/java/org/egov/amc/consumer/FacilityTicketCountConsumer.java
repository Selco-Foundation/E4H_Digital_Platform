package org.egov.amc.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.amc.service.FacilityTicketCountService;
import org.egov.amc.web.models.FacilityTicketCountEvent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * Consumes the facility ticket totals im-services publishes on every ticket create, and hands them to
 * {@link FacilityTicketCountService} to stamp onto the facility's latest non-DRAFT AMC visit.
 */
@Component
@Slf4j
public class FacilityTicketCountConsumer {

    private final FacilityTicketCountService facilityTicketCountService;
    private final ObjectMapper mapper;

    public FacilityTicketCountConsumer(FacilityTicketCountService facilityTicketCountService,
                                       @Qualifier("objectMapper") ObjectMapper mapper) {
        this.facilityTicketCountService = facilityTicketCountService;
        this.mapper = mapper;
    }

    @KafkaListener(topics = {"${amc.facility.ticket.count.topic}"})
    public void listen(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            FacilityTicketCountEvent event = mapper.convertValue(record, FacilityTicketCountEvent.class);
            facilityTicketCountService.applyTicketCount(event);
        } catch (Exception e) {
            // Swallowed deliberately: the next ticket raised for the facility republishes the total, so a
            // failure here self-corrects. Rethrowing would stall the partition on a single bad record.
            log.error("Failed to apply facility ticket count from topic {}: {}", topic, record, e);
        }
    }
}
