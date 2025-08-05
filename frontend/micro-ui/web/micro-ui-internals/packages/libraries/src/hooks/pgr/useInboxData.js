import { useQuery, useQueryClient } from "react-query";
import React, { useEffect, useRef } from "react";
import { useTranslation } from "react-i18next";

const useInboxData = (searchParams) => {
  const { t } = useTranslation();
  const commonFilters = { start: 1, end: 10 };
  const { limit, offset, nearingSLA } = searchParams;

  sessionStorage.setItem("limit", JSON.stringify(limit));
  sessionStorage.setItem("offset", JSON.stringify(offset));

  const appFilters = {
    ...commonFilters,
    ...searchParams?.search,
    ...searchParams?.filters?.pgrQuery,
    limit,
    offset,
    nearingSLA,
  };

  sessionStorage.setItem("appFilters", JSON.stringify(appFilters));
  sessionStorage.setItem("searchParams", JSON.stringify(searchParams));

  const wfFilters = {
    ...commonFilters,
    ...searchParams?.filters?.wfQuery,
    ...(searchParams?.filters?.wfFilters?.assignee?.[0]?.code !== ""
      ? { assignee: searchParams?.filters?.wfFilters?.assignee?.[0]?.code }
      : {}),
  };

  const { assignee } = wfFilters;

  const { data, isSuccess, refetch } = Digit.Hooks.useNewInboxGeneral({
    tenantId: Digit.ULBService.getCurrentTenantId(),
    ModuleCode: "Incident",
    filters: { ...appFilters, assignee, sortOrder: "DESC", services: ["Incident"] },
    config: {
      select: (data) => ({ data } || "-"),
      enabled: Digit.Utils.pgrAccess(),
      staleTime: 30000,
      cacheTime: 300000,
    },
  });

  const filteredData = isSuccess && data ? filterData(data) : { total: 0, items: [], statusArray: [] };

  const prevSearchParams = useRef(searchParams);
  useEffect(() => {
    const currentParamsStr = JSON.stringify({
      limit: searchParams.limit,
      offset: searchParams.offset,
      nearingSLA: searchParams.nearingSLA,
      filters: searchParams.filters,
      search: searchParams.search
    });
    const prevParamsStr = JSON.stringify({
      limit: prevSearchParams.current?.limit,
      offset: prevSearchParams.current?.offset,
      nearingSLA: prevSearchParams.current?.nearingSLA,
      filters: prevSearchParams.current?.filters,
      search: prevSearchParams.current?.search
    });
    
    if (currentParamsStr !== prevParamsStr) {
      refetch();
      prevSearchParams.current = searchParams;
    }
  }, [searchParams.limit, searchParams.offset, searchParams.nearingSLA, JSON.stringify(searchParams.filters), JSON.stringify(searchParams.search), refetch]);

  const fetchInboxData = () => {
    const currentUser = JSON.parse(sessionStorage.getItem("Digit.User"))?.value?.info;
    const currentUserUuid = currentUser?.uuid;
    const currentTenant = Digit.SessionStorage.get("Employee.tenantId");
    const stateTenantId = Digit.ULBService.getStateId();

    const combinedRes = combineResponses(filteredData.items, currentUserUuid, currentTenant, stateTenantId, currentUser, t);

    return {
      combinedRes,
      total: filteredData.total,
      statusArray: filteredData.statusArray,
    };
  };

  const result = fetchInboxData()

  return {data:result};
};

const filterData = (data) => {
  const filteredItems = data.data.items;
  const totalItems = data.data.totalCount;
  const statusArray = data.data.statusMap;
  return { total: totalItems, items: filteredItems, statusArray: statusArray };
};

const combineResponses = (items, currentUserUuid, currentTenant, stateTenantId, currentUser, t) => {
  const closedStates = ["RESOLVED", "CLOSEDAFTERRESOLUTION", "REJECTED"];
  const roleStatusMapping = {
    PENDINGFORASSIGNMENT: "COMPLAINT_ASSESSOR",
    PENDING_ASSIGNMENT_OUT_OF_WARRANTY: "COMPLAINT_FACILITATOR_1",
    PENDING_ASSIGNMENT_SPARE_PART_NEEDED: "COMPLAINT_FACILITATOR_2",
  };

  const currentUserRoles = currentUser?.roles?.map((r) => r.code) || [];

  return items.map(({ businessObject, ProcessInstance }) => {
    const incident = businessObject?.incident || {};
    const reporterUuid = incident?.reporter?.uuid;
    const assignee = ProcessInstance?.assignes?.[0];
    const assigneeUuid = assignee?.uuid;

    let slaValue = "-";

    if (closedStates.includes(incident.applicationStatus)) {
      slaValue = "-";
    } else if (currentUserUuid === reporterUuid && currentTenant !== stateTenantId) {
      const totalSla = businessObject?.totalSlaRemaining;
      slaValue = totalSla < 0 ? t("SLA_OVERDUE") : Math.ceil(totalSla / (8 * 60 * 60 * 1000));
    } else if (assigneeUuid && currentUserUuid === assigneeUuid) {
      const sla = businessObject?.slaRemaining;
      slaValue = Math.ceil(sla / (8 * 60 * 60 * 1000));
    } else if (!assigneeUuid) {
      const requiredRole = roleStatusMapping[incident.applicationStatus];
      if (requiredRole && currentUserRoles.includes(requiredRole)) {
        const sla = businessObject?.slaRemaining;
        slaValue = Math.ceil(sla / (8 * 60 * 60 * 1000));
      }
    }

    return {
      incidentId: incident.incidentId,
      incidentType: incident.incidentType,
      incidentSubType: incident.incidentSubType,
      phcType: incident.phcType,
      status: incident.applicationStatus,
      taskOwner: assignee?.name || "-",
      sla: `${slaValue}`,
      tenantId: incident.tenantId,
    };
  });
};

export default useInboxData;
