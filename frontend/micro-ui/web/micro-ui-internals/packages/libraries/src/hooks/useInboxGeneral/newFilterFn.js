const SECOND = 1000;
const MINUTE = 60 * SECOND;
const HOUR = 60 * MINUTE;
const DAY = 24 * HOUR;

export const filterFunctions = {
  Incident: (filtersArg) => {
    let { uuid } = Digit.UserService.getUser()?.info || {};

    let searchFilters = {};
    const workflowFilters = {};

    const {
      applicationNumber, mobileNumber, limit,
      offset, sortBy, sortOrder, total,
      applicationStatus, services, incidentType,
      facility, assignee, nearingSLA, state, district, block, isSystemFunctional
    } = filtersArg || {};

    if (filtersArg?.IncidentWrappers) {
      searchFilters.applicationNumber = filtersArg?.incidentId;
    }
    
    if (applicationStatus) {
      let convertStatus=[applicationStatus];
      if(applicationStatus.includes(",")){
        convertStatus=applicationStatus.split(',')
      }
      workflowFilters.status = convertStatus;
      // if (applicationStatus?.some((e) => e.nonActionableRole)) {
      //   searchFilters.fetchNonActionableRecords = true;
      // }
    }

    if(incidentType){
      let convertIncidentType=[incidentType];
      if(incidentType.includes(",")){
        convertIncidentType=incidentType.split(',')
      }
      searchFilters.incidentType=convertIncidentType;
    }


    if (facility) {
      let convertFacility = [facility];
      if(facility.includes(",")){
        convertFacility = facility.split(',');
      }
      searchFilters.facility = convertFacility;

    } else if (block) {
      let convertBlock = [block];
      if (block.includes(",")) {
        convertBlock = block.split(",");
      }
      searchFilters.block = convertBlock;

    } else if (district) {
      let convertDistrict = [district];
      if (district.includes(",")) {
        convertDistrict = district.split(",");
      }
      searchFilters.district = convertDistrict;

    } else if (state) {
      let convertState = [state];
      if (state.includes(",")) {
        convertState = state.split(",");
      }
      searchFilters.state = convertState;
    }

    if (isSystemFunctional) {
      let convertIsSystemFunctional = [isSystemFunctional];
      if (isSystemFunctional.includes(",")) {
        convertIsSystemFunctional = isSystemFunctional.split(",");
      }
      searchFilters.systemFunctional = convertIsSystemFunctional;
    }
    
    if (filtersArg?.uuid && filtersArg?.uuid.code === "ASSIGNED_TO_ME") {
      workflowFilters.assignee = uuid;
    }
    if (mobileNumber) {
      searchFilters.mobileNumber = mobileNumber;
    }
    if (services) {
      workflowFilters.businessService = services;
    }
    searchFilters["tenantId"] = Digit.ULBService.getCurrentTenantId();
    if (nearingSLA) {
      searchFilters["nearingSLA"] = 3 * DAY;
    }
    //searchFilters["sortOrder"] = "DESC";
   // searchFilters["creationReason"] = ["CREATE", "MUTATION", "UPDATE"];
    workflowFilters["moduleName"] = "Incident";
    workflowFilters["tenantId"]=Digit.ULBService.getCurrentTenantId();

    // if (limit) {
    //   searchFilters.limit = limit;
    // }
    // if (offset) {
    //   searchFilters.offset = offset;
    // }

    // workflowFilters.businessService = "PT.CREATE";
    // searchFilters.mobileNumber = "9898568989";
    return { searchFilters, workflowFilters, limit, offset, sortBy, sortOrder, applicationNumber, assignee};
  },
};
