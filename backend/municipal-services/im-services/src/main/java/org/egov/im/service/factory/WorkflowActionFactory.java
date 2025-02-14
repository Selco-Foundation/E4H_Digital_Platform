package org.egov.im.service.factory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.im.service.handler.WorkflowActionHandler;
import org.egov.im.service.handler.*;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowActionFactory {

    private final Map<String, WorkflowActionHandler> handlers;

    public WorkflowActionHandler getHandler(String actionStatus) {
        return handlers.getOrDefault(actionStatus, new DefaultHandler());
    }
}
