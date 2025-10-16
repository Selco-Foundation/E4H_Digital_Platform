import {Request} from "@egovernments/digit-ui-libraries";
import { CustomRequest } from "../components/CustomRequest";

export const ActivityService = {

  fetchActivityAssignments: async (queryFilter, limit = 10, offset = 0) => {
    const endpoint = "/activity/v1/activities/assignment/_search";
    const headers = {
      "Content-Type": "application/json"
    }
    const params = {
      tenantId: Digit.ULBService.getCurrentTenantId(),
      offset,
      limit,
    }

    return await Request({
      url: endpoint,
      data: queryFilter,
      userService: true,
      method: "POST",
      auth: true,
      params : params,
      headers: headers,
    });
  },

  fetchActivityFacilities: async (queryFilter, limit = 10, offset = 0) => {
    const endpoint = "/activity/v1/activities/_search";
    const headers = {
      "Content-Type": "application/json"
    }
    const params = {
      tenantId: Digit.ULBService.getCurrentTenantId(),
      offset,
      limit,
    }

    return await Request({
      url: endpoint,
      data: queryFilter,
      userService: true,
      method: "POST",
      auth: true,
      params : params,
      headers: headers,
    });
  },

  updateActivityFacilityWorkflow : async (activityFacilityId, action, comments, workflowComment, documents = []) => {
    const endpoint = "/activity/v1/activities/workflow/update";
    const queryObj = {
      activityFacilityId: activityFacilityId,
      workflow: {
        action: action,
        comment: workflowComment,
        documents: documents
      },
      transactions: [
        {
          comments: [...comments]
        }
      ]
    }
    const headers = {
      "Content-Type" : "application/json"
    }

    return await Request({
      url : endpoint,
      data : queryObj,
      method : "POST",
      userService : true,
      auth : true,
      headers : headers,
    });
  },

  bulkApproveActivityFacilities: async (filters, mainCheck, activityFacilityIds) => {
    const endpoint = "/activity/v1/activities/bulk/workflow/update";

    const queryObj = {
      workflow: {
        action: "APPROVE",
        comments: "Approved by QC"
      }
    }

    queryObj.isAllSelected = mainCheck;

    if (mainCheck) {
      const currentFilters = {
        searchCriteria: {
          statuses: ["SUBMITTED_BY_SUPERVISOR"],
          fieldPlanIds: filters.project.fieldPlanId,
          activityIds: filters.project.activityId,
        }
      }

      if (filters.facilitySearch.name) {
        currentFilters.searchCriteria.facilityName = filters.facilitySearch.name;
      }

      if (filters.facilityFilterQuery.boundary?.length) {
        currentFilters.searchCriteria.boundaryCodes = filters.facilityFilterQuery.boundary;
      }

      if (filters.facilityFilterQuery.status?.length) {
        currentFilters.searchCriteria.statuses = filters.facilityFilterQuery.status;
      }

      queryObj.filters = currentFilters;

    } else {
      queryObj.activityFacilityIds = activityFacilityIds;
    }

    return CustomRequest({
      url : endpoint,
      data : queryObj,
      method : "POST",
      userService : true,
      auth : true,
    });
  },

}