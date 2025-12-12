import React, { useState, useEffect } from "react";
import { useTranslation } from "react-i18next";

const useEmployeeFilter = (tenantIdNew, roles, complaintDetails, boundaryCodes) => {
  const [employeeDetails, setEmployeeDetails] = useState(null);
  const { t } = useTranslation();
  useEffect(() => {
    (async () => {
      // const _roles = roles.join(",");
      let tenantId =  window.location.href.split("/")[9]
      const searchResponse = await Digit.PGRService.employeeSearch(tenantId, roles, boundaryCodes );
      const serviceDefs = await Digit.MDMSService.getServiceDefs(tenantId, "Incident");
      const incidentSubType = complaintDetails.incident.incidentSubType;
      const service = serviceDefs?.find((def) => def.serviceCode === incidentSubType);
      const department = service?.department;
      const employees = searchResponse.Employees.filter((employee) =>
        employee.assignments.map((assignment) => assignment.department)
      );

      //emplpoyess data sholld only conatin name uuid dept
      setEmployeeDetails([
        {
         
          employees: employees.map((employee) => {
          return { uuid: employee.user.uuid, name: employee.user.userName}})
        }
      ])
    })();
  }, [tenantIdNew, roles, t, complaintDetails]);


  return employeeDetails;
};

export default useEmployeeFilter;
