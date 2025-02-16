package org.egov.im.service.handler.message;

import lombok.extern.slf4j.Slf4j;
import org.egov.im.config.IMConfiguration;
import org.egov.im.web.models.IncidentRequest;
import org.egov.im.web.models.IncidentWrapper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.egov.im.util.IMConstants.*;

@Slf4j
public abstract class MessageBuilderHandler {

    protected String messageForCitizen;
    protected String messageForEmployee;
    protected String messageForCRM;
    Map<String, List<String>> message = new HashMap<>();

    public abstract Map<String, List<String>> handle(IncidentRequest request,
                                              IncidentWrapper incidentWrapper,
                                              String localizationMessage,
                                              String localisedStatus,
                                              String topic);


    Map<String, List<String>> getMessage(IncidentWrapper incidentWrapper, IMConfiguration config) {
        Long createdTime = incidentWrapper.getIncident().getAuditDetails().getCreatedTime();
        LocalDate date = Instant.ofEpochMilli(createdTime > 10 ? createdTime : createdTime * 1000)
                .atZone(ZoneId.systemDefault()).toLocalDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_PATTERN);

        if (messageForCitizen != null) {
            messageForCitizen = messageForCitizen.replace("{ticket_type}", incidentWrapper.getIncident().getIncidentType());
            messageForCitizen = messageForCitizen.replace("{incidentId}", incidentWrapper.getIncident().getIncidentId());
            messageForCitizen = messageForCitizen.replace("{date}", date.format(formatter));
            messageForCitizen = messageForCitizen.replace("{download_link}", config.getMobileDownloadLink());
        }

        if (messageForEmployee != null) {
            messageForEmployee = messageForEmployee.replace("{ticket_type}", incidentWrapper.getIncident().getIncidentType());
            messageForEmployee = messageForEmployee.replace("{incidentId}", incidentWrapper.getIncident().getIncidentId());
            messageForEmployee = messageForEmployee.replace("{date}", date.format(formatter));
            messageForEmployee = messageForEmployee.replace("{download_link}", config.getMobileDownloadLink());
        }

        if (messageForCRM != null) {
            messageForCRM = messageForCRM.replace("{ticket_type}", incidentWrapper.getIncident().getIncidentType());
            messageForCRM = messageForCRM.replace("{incidentId}", incidentWrapper.getIncident().getIncidentId());
            messageForCRM = messageForCRM.replace("{date}", date.format(formatter));
            messageForCRM = messageForCRM.replace("{download_link}", config.getMobileDownloadLink());
        }
        if (messageForCitizen != null) {
            message.put(CITIZEN, List.of(messageForCitizen));
        }
        message.put(EMPLOYEE, Collections.singletonList(messageForEmployee));
        if (messageForCRM != null)
            message.put(CRM, List.of(messageForCRM));

        log.info("message being sent is  " + messageForEmployee + " , " + messageForCitizen + " , " + messageForCRM);
        return message;
    }
}
