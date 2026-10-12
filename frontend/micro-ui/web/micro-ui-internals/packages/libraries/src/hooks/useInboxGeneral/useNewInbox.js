import { useTranslation } from "react-i18next";
import { useQuery, useQueryClient } from "react-query";
import React from "react";
import { filterFunctions } from "./newFilterFn";
import { getSearchFields } from "./searchFields";
import { InboxGeneral } from "../../services/elements/InboxService";
import {PGRService} from "../../services/elements/PGR"

const inboxConfig = (tenantId, filters) => ({
  Incident: {
    services: ["Incident"],
    searchResponseKey: "IncidentWrappers",
    businessIdsParamForSearch: "incidentId",
    businessIdAliasForSearch: "incidentId",
    fetchFilters: filterFunctions.Incident,
    _searchFn: () => PGRService.search({ tenantId, filters }),
  },
});

const callMiddlewares = async (data, middlewares) => {
  let applyBreak = false;
  let itr = -1;
  let _break = () => (applyBreak = true);
  let _next = async (data) => {
    if (!applyBreak && ++itr < middlewares.length) {
      let key = Object.keys(middlewares[itr])[0];
      let nextMiddleware = middlewares[itr][key];
      let isAsync = nextMiddleware.constructor.name === "AsyncFunction";
      if (isAsync) return await nextMiddleware(data, _break, _next);
      else return nextMiddleware(data, _break, _next);
    } else return data;
  };
  let ret = await _next(data);
  return ret || [];
};

const useNewInboxGeneral = ({ tenantId, ModuleCode, filters, middleware = [], config = {} }) => {
  const client = useQueryClient();
  const { t } = useTranslation();
  const { fetchFilters, searchResponseKey, businessIdAliasForSearch, businessIdsParamForSearch } = inboxConfig()[ModuleCode];
  const { userName, roles } = Digit.UserService.getUser()?.info || {};
  const usesAssignedToMe = ModuleCode === "Incident" && roles?.some((role) =>
    ["COMPLAINT_FACILITATOR_1", "COMPLAINT_FACILITATOR_2"].includes(role.code)
  );
  // The home card passes an array; the inbox passes the selected username.
  const assignee = Array.isArray(filters?.assignee) ? filters.assignee[0]?.code : filters?.assignee;
  const assignedToMe = !!(usesAssignedToMe && assignee && assignee === userName);
  const requestFilters = usesAssignedToMe ? { ...filters, assignee: undefined } : filters;
  const { workflowFilters, searchFilters, limit, offset, sortBy, sortOrder, applicationNumber } = fetchFilters(requestFilters);
  const jurisdictionCurrentBoundaries = Digit.SessionStorage.get("Jurisdiction.CurrentBoundary") || {
    country: ["-"],
  };

  const query = useQuery(
    ["INBOX", workflowFilters, searchFilters, ModuleCode, limit, offset, sortBy, sortOrder, applicationNumber, assignedToMe],
    () =>
      InboxGeneral.Search({
        inbox: {
          tenantId,
          ...(assignedToMe ? { assignedToMe } : {}),
          processSearchCriteria: workflowFilters,
          jurisdictionSearchCriteria: jurisdictionCurrentBoundaries,
          moduleSearchCriteria: {
            ...searchFilters,
            sortBy, sortOrder,
            applicationNumber,
          }, limit, offset
        },
      }),
    {
      select: (data) => {
        const { statusMap, totalCount } = data;
        if (data.items.length) {
          return data.items?.map((obj) => ({
           
            nearingSlaCount,
            statusMap,
            totalCount,
          }));
        } else {
          return [{ statusMap, totalCount, dataEmpty: true }];
        }
      },
      retry: true,
      staleTime: 30000,
      cacheTime: 300000,
      refetchOnWindowFocus: false,
      refetchOnMount: false,
      ...config,
    }
  );

  return {
    ...query,
    searchResponseKey,
    businessIdsParamForSearch,
    businessIdAliasForSearch,
    searchFields: getSearchFields(true)[ModuleCode],
  };
};

export default useNewInboxGeneral;
