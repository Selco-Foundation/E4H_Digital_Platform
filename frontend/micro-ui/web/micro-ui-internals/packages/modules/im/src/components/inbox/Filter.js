import React, { useEffect, useMemo, useState } from "react";
import { Dropdown, RadioButtons, ActionBar, RemoveableTag, RoundedLabel } from "@selco/digit-ui-react-components";
import { ApplyFilterBar, CloseSvg } from "@selco/digit-ui-react-components";
import { useTranslation } from "react-i18next";
import Status from "./Status";

let pgrQuery = {};
let wfQuery = {};

const Filter = (props) => {
  let { uuid } = Digit.UserService.getUser().info;
  const stateTenantId = Digit.ULBService.getStateId();
  const { searchParams } = props;
  const { t } = useTranslation();
  const isAssignedToMe = searchParams?.filters?.wfFilters?.assignee && searchParams?.filters?.wfFilters?.assignee[0]?.code ? true : false;

  const assignedToOptions = useMemo(
    () => [
      { code: "ASSIGNED_TO_ME", name: t("ASSIGNED_TO_ME") },
      { code: "ASSIGNED_TO_ALL", name: t("ASSIGNED_TO_ALL") },
    ],
    [t]
  );

const isCodePresent = (array, codeToCheck) =>{
  return array.some(item => item.code === codeToCheck);
}
console.log("vvvvv",isCodePresent(Digit.SessionStorage.get("User")?.info?.roles, "COMPLAINT_RESOLVER"))
const [selectAssigned, setSelectedAssigned] = useState(isCodePresent(Digit.SessionStorage.get("User")?.info?.roles, "COMPLAINT_RESOLVER") ? assignedToOptions[0] : assignedToOptions[1]);
useEffect(() => setSelectedAssigned(isCodePresent(Digit.SessionStorage.get("User")?.info?.roles, "COMPLAINT_RESOLVER") ? assignedToOptions[0] : assignedToOptions[1]), [t]);

  const [selectedComplaintType, setSelectedComplaintType] = useState(null);
  const [selectedHealthCare, setSelectedHealthCare] = useState(null);
  
  const [pgrfilters, setPgrFilters] = useState(
    searchParams?.filters?.pgrfilters || {
      incidentType: [],
      phcType: [],
      applicationStatus: [],
    }
  );
  let healthcareTenant = Digit.SessionStorage.get("Tenants").filter(item => item.code !== stateTenantId)

  const [wfFilters, setWfFilters] = useState(
    isCodePresent(Digit.SessionStorage.get("User")?.info?.roles, "COMPLAINT_RESOLVER") ? searchParams?.filters?.wfFilters:searchParams?.filters?.wfFilters?.["assignee"]?.[{"code":""}] || isCodePresent(Digit.SessionStorage.get("User")?.info?.roles, "COMPLAINT_RESOLVER") ? {
      assignee: [{ code: uuid }],
    }
    :
    {
      assignee: [{ code: "" }],
    }
  );
  
  
  const tenantId = Digit.ULBService.getCurrentTenantId();
  // let localities = Digit.Hooks.pgr.useLocalities({ city: tenantId });
  const { data: localities } = Digit.Hooks.useBoundaryLocalities(tenantId, "admin", {}, t);
  
  let serviceDefs = Digit.Hooks.pgr.useServiceDefs(tenantId, "Incident");
  const menu = Digit.Hooks.pgr.useComplaintTypes({ stateCode: tenantId })
  let sortedMenu=[];
  if(menu!==null){
    let othersItem = menu.find(item => item.name==="Other");
    let remainingOptions = menu.filter(item => item.name!=="Other");
    remainingOptions.sort((a, b) => a.name.localeCompare(b.name));
    if (othersItem) {
      remainingOptions.push(othersItem);
    }
    sortedMenu = remainingOptions
  }
  
  const state = Digit.ULBService.getStateId();
//   const { isMdmsLoading, data: mdmsData } = Digit.Hooks.pgr.useMDMS(state, "Incident", ["District","Block"]);
// const {  data: phcMenu  } = Digit.Hooks.pgr.useMDMS(state, "tenant", ["tenants"]);
const convertedData = Digit.SessionStorage.get("IM_TENANTS").map(item => ({
  name: item.label,
  code: item.value
}));
const healthcareMenu=Digit.SessionStorage.get("Employee.tenantId") !== stateTenantId ? Digit.SessionStorage.get("Tenants") : Digit.SessionStorage.get("Employee.tenantId") == stateTenantId ? isCodePresent(Digit.SessionStorage.get("User")?.info?.roles, "COMPLAINT_RESOLVER")?  healthcareTenant: Digit.SessionStorage.get("IM_TENANTS").filter((item) => item.code !==stateTenantId): Digit.SessionStorage.get("IM_TENANTS").filter((item) => item.code !==stateTenantId)
const translatedPhcMenu=healthcareMenu.map(item=>({
  ...item,
  code: t(item?.code),
  name:t(item?.name),
  centreType:t(item?.centreType)
}))
let sortedHealthCaremenu=[];
if(translatedPhcMenu.length>0){
  sortedHealthCaremenu=translatedPhcMenu.sort((a, b) => a.name.localeCompare(b.name));
}

  const onRadioChange = (value) => {
    setSelectedAssigned(value);
    uuid = value.code === "ASSIGNED_TO_ME" ? uuid : "";
    setWfFilters({ ...wfFilters, assignee: [{ code: uuid }] });
  };

  useEffect(() => {
    let count = 0;
    for (const property in pgrfilters) {
      if (Array.isArray(pgrfilters[property])) {
        count += pgrfilters[property].length;
        let params = pgrfilters[property].map((prop) => prop.code).join();
        if (params) {
          pgrQuery[property] = params;
        }
        else{
          delete pgrQuery?.[property]
        }
      }
    }
    for (const property in wfFilters) {
      if (Array.isArray(wfFilters[property])) {
        let params = wfFilters[property].map((prop) => prop.name).join();
        if (params) {
          wfQuery[property] = params;
        } else {
          wfQuery = {};
        }
      }
    }
    count += wfFilters?.assignee?.length || 0;

    if (props.type !== "mobile") {
      handleFilterSubmit();
    }

    Digit.inboxFilterCount = count;
  }, [pgrfilters, wfFilters]);

  const ifExists = (list, key) => {
    return list.filter((object) => object.code === key.code).length;
  };
  function applyFiltersAndClose() {
    handleFilterSubmit();
    props.onClose();
  }
  function complaintType(_type) {
    
    const type = { code: _type.key, name: _type.name };
    if (!ifExists(pgrfilters.incidentType, type)) {
      setPgrFilters({ ...pgrfilters, incidentType: [...pgrfilters.incidentType, type] });
    }
  }

  function onSelectHealthCare(value, type) {
    if(!value) return
    if (!ifExists(pgrfilters.phcType, value)) {
      setPgrFilters({ ...pgrfilters, phcType: [...pgrfilters.phcType, value] });
    }
  }

  useEffect(() => {
    if (pgrfilters.incidentType.length > 1) {
      setSelectedComplaintType({ i18nKey: `${pgrfilters.incidentType.length} selected` });
    } else {
      setSelectedComplaintType(pgrfilters.incidentType[0]);
    }
  }, [pgrfilters.incidentType]);

  useEffect(() => {
    if (pgrfilters.phcType.length > 1) {
      setSelectedHealthCare({ name: `${pgrfilters.phcType.length} selected` });     
    } else {
      setSelectedHealthCare(pgrfilters.phcType[0]);
    }
  }, [pgrfilters.locality]);

  const onRemove = (index, key) => {
    let afterRemove = pgrfilters[key].filter((value, i) => {
      return i !== index;
    });
    setPgrFilters({ ...pgrfilters, [key]: afterRemove });
  };
  const handleAssignmentChange = (e, type) => {
    if (e.target.checked) {
      setPgrFilters({ ...pgrfilters, applicationStatus: [...pgrfilters.applicationStatus, { code: type.code }] });
    } else {
      const filteredStatus = pgrfilters.applicationStatus.filter((value) => {
        return value.code !== type.code;
      });
      setPgrFilters({ ...pgrfilters, applicationStatus: filteredStatus });
    }
  };

  function clearAll() {
    let pgrReset = { incidentType: [], phcType: [], applicationStatus: [] };
    let wfRest = { assigned: [{ code: [] }] };
    setPgrFilters(pgrReset);
    setWfFilters(wfRest);
    pgrQuery = {};
    wfQuery = {};
    setSelectedAssigned("");
    setSelectedComplaintType(null);
    setSelectedHealthCare(null);
  }

  const handleFilterSubmit = () => {
    props.onFilterChange({ pgrQuery: pgrQuery, wfQuery: wfQuery, wfFilters, pgrfilters });
  };

  const GetSelectOptions = (lable, options, selected = null, select, optionKey, onRemove, key) => {
    selected = selected || { [optionKey]: "", code: "" };
   
    return (
      <div>
        <div className="filter-label">{lable}</div>
        {<Dropdown option={options} selected={selected} select={(value) => select(value, key)} optionKey={optionKey} />}

        <div className="tag-container">
          {pgrfilters[key].length > 0 &&
            pgrfilters[key].map((value, index) => {
              return <RemoveableTag key={index} text={`${value[optionKey]} ...`} onClick={() => onRemove(index, key)} />;
            })}
        </div>
      </div>
    );
  };

  return (
    <React.Fragment>
      <div className="filter">
        <div className="filter-card">
          <div className="heading">
            <div className="filter-label">{t("ES_COMMON_FILTER_BY")}:</div>
            <div className="clearAll" onClick={clearAll}>
              {t("ES_COMMON_CLEAR_ALL")}
            </div>
            {props.type === "desktop" && (
              <span className="clear-search" style={{color:"#7a2829"}} onClick={clearAll}>
                {t("ES_COMMON_CLEAR_ALL")}
              </span>
            )}
            {props.type === "mobile" && (
              <span onClick={props.onClose}>
                <CloseSvg />
              </span>
            )}
          </div>
          <div>
            <RadioButtons onSelect={onRadioChange} selectedOption={selectAssigned} optionsKey="name" options={assignedToOptions} />
            <div>
              {GetSelectOptions(
                t("CS_COMPLAINT_DETAILS_TICKET_TYPE"),
                sortedMenu,
                selectedComplaintType,
                complaintType,
                "name",
                onRemove,
                "incidentType"
              )}
            </div>
            <div>{GetSelectOptions(t("CS_HEALTH_CARE"), sortedHealthCaremenu, selectedHealthCare, onSelectHealthCare, "name", onRemove, "phcType")}</div>
            {<Status complaints={props.complaints} onAssignmentChange={handleAssignmentChange} pgrfilters={pgrfilters} />}
          </div>
        </div>
      </div>
      <ActionBar>
        {props.type === "mobile" && (
          <ApplyFilterBar
            labelLink={t("ES_COMMON_CLEAR_ALL")}
            buttonLink={t("ES_COMMON_FILTER")}
            onClear={clearAll}
            onSubmit={applyFiltersAndClose}
          />
        )}
      </ActionBar>
    </React.Fragment>
  );
};

export default Filter;
