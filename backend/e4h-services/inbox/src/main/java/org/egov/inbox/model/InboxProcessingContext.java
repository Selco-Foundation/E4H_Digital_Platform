package org.egov.inbox.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.RequestInfo;
import org.egov.inbox.web.model.Inbox;
import org.egov.inbox.web.model.InboxResponse;
import org.egov.inbox.web.model.InboxSearchCriteria;
import org.egov.inbox.web.model.workflow.ProcessInstanceSearchCriteria;
import org.json.JSONArray;

/**
 * Context class to hold all processing state during inbox data fetching
 * This class encapsulates all the intermediate state and data structures
 * used throughout the inbox data fetching process
 */
public class InboxProcessingContext {
    public InboxSearchCriteria criteria;
    public RequestInfo requestInfo;
    public ProcessInstanceSearchCriteria processCriteria;
    public HashMap moduleSearchCriteria;
    public Integer flag;
    public Integer totalCount;
    public Integer nearingSlaProcessCount;
    public List<String> inputStatuses;
    public String dsoId;
    public StringBuilder assigneeUuid;
    public List<String> roles;
    public String originalModuleName;
    public List<HashMap<String, Object>> statusCountMap;
    public List<String> businessServiceName;
    public Map<String, String> srvMap;
    public List<Inbox> inboxes;
    public InboxResponse response;
    public JSONArray businessObjects;
    public Map<String, Long> businessServiceSlaMap;
    public Map<String, List<String>> tenantAndApplnNumbersMap;
    public String businessIdParam;
    public HashMap<String, String> statusIdNameMap;
}
