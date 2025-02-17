package org.egov.im.service.handler.message;

import org.egov.im.web.models.IncidentRequest;
import org.egov.im.web.models.IncidentWrapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component("DEFAULTMESSAGEHANDLER")
public class DefaultMessageHandler extends MessageBuilderHandler {

    @Override
    public Map<String, List<String>> handle(IncidentRequest request,
                                            IncidentWrapper incidentWrapper,
                                            String localizationMessage,
                                            String localisedStatus, String topic) {
        return Map.of();
    }
}
