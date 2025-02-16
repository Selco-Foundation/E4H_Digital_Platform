package org.egov.im.service.factory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.im.service.handler.message.DefaultMessageHandler;
import org.egov.im.service.handler.message.MessageBuilderHandler;
import org.egov.im.util.NotificationUtil;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowMessageHandlerFactory {

    private final Map<String, MessageBuilderHandler> handlers;
    private final NotificationUtil notificationUtil;

    public MessageBuilderHandler getHandler(String actionStatus) {
        return handlers.getOrDefault(actionStatus, new DefaultMessageHandler());
    }

}
