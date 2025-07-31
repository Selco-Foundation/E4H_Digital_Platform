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
  const [districtMenu, setDistrictMenu] = useState([]);
  const [blockMenu, setBlockMenu] = useState([]);
  const [phcMenu, setPhcMenu] = useState([]);
  const [systemFunctionalityMenu, setSystemFunctionalityMenu] = useState([]);

  const assignedToOptions = useMemo(
    () => [
      { code: "ASSIGNED_TO_ME", name: t("ASSIGNED_TO_ME") },
      { code: "ASSIGNED_TO_ALL", name: t("ASSIGNED_TO_ALL") },
    ],
    [t]
  );
  const loggedInUser = Digit.UserService.getUser();
  const isAssignedToMe = searchParams?.filters?.wfFilters?.assignee?.[0]?.code === uuid;

  const state = Digit.ULBService.getStateId();
  const { data: mdmsData } = Digit.Hooks.pgr.useMDMS(state, "Incident", ["District", "Block", "SystemFunctionality"]);
  const { data: phcData } = Digit.Hooks.pgr.useMDMS(state, "tenant", ["tenants"]);
  const isNonHcrUser = Digit.UserService.getUser()?.info.roles.some(role => (role.code !== "EMPLOYEE" && role.code !== "COMPLAINANT"));

const isCodePresent = (array, codeToCheck) =>{
  return array.some(item => item.code === codeToCheck);
}

  const [selectAssigned, setSelectedAssigned] = useState(
    (isAssignedToMe || isCodePresent(loggedInUser?.info?.roles, "COMPLAINT_RESOLVER")) ? assignedToOptions[0] : assignedToOptions[1]
  );

  const [selectedComplaintType, setSelectedComplaintType] = useState(null);
  const [selectedHealthCare, setSelectedHealthCare] = useState(null);
  
  const [pgrfilters, setPgrFilters] = useState(
    searchParams?.filters?.pgrfilters || {
      incidentType: [],
      phcType: [],
      district: [],
      block: [],
      isSystemFunctional: [],
      applicationStatus: [],
    }
  );

  const [wfFilters, setWfFilters] = useState(
    searchParams?.filters?.wfFilters ||
    (isCodePresent(loggedInUser?.info?.roles, "COMPLAINT_RESOLVER") ?
      { assignee: [{ code: uuid }] } :
      { assignee: [{ code: "" }] })
  );

  useEffect(() => {
    const refactorDistrictMenu = () => {
      const response = mdmsData?.Incident?.District;
      if (response) {
        const uniqueDistricts = {};
        const newDistrictMenu = response
          .filter((district) => {
            if (!uniqueDistricts[district.code]) {
              uniqueDistricts[district.code] = true;
              return true;
            }
            return false;
          })
          .sort((a, b) => a.name.localeCompare(b.name))
          .map((district) => ({
            districtCode: district.code,
            code: district.name,
            nonLocalizedName: district.name,
            name: t(district.name),
          }));

        setDistrictMenu(newDistrictMenu);
        setBlockMenu([]);
        setPhcMenu([]);
      }
    };

    const refactorSystemFunctionalMenu = () => {
      const response = mdmsData?.Incident?.SystemFunctionality;
      if (response) {
        const newSystemFunctionalityMenu = response
          .filter((systemFunctionality) => systemFunctionality.active)
          .sort((a, b) => a.name.localeCompare(b.name))
          .map((systemFunctionality) => ({
            code: systemFunctionality.code,
            nonLocalizedName: systemFunctionality.name,
            name: t(systemFunctionality.name),
          }));

        setSystemFunctionalityMenu(newSystemFunctionalityMenu);
      }
    }

    refactorDistrictMenu();
    refactorSystemFunctionalMenu();
  }, [state, mdmsData, t]);

  useEffect(() => {

    setSelectedAssigned(
      (isAssignedToMe || isCodePresent(loggedInUser?.info?.roles, "COMPLAINT_RESOLVER")) ? assignedToOptions[0] : assignedToOptions[1]
    )

    setPgrFilters((prevFilters) => ({
      ...prevFilters,
      incidentType: prevFilters.incidentType.map((type) => ({
        ...type,
        name: t(`SERVICEDEFS.${type.code === "" ? "OTHER" : type.code.toUpperCase()}`),
      })),
      district: prevFilters.district.map((district) => ({ ...district, name: t(district.nonLocalizedName) })),
      block: prevFilters.block.map((block) => ({ ...block, name: t(block.nonLocalizedName) })),
      phcType: prevFilters.phcType.map((type) => ({ ...type, name: t(type.nonLocalizedName) })),
      isSystemFunctional: prevFilters.isSystemFunctional.map((systemFunctionality) => ({
        ...systemFunctionality,
        name: t(systemFunctionality.nonLocalizedName)
      })),
    }));
  }, [t]);

  useEffect(() => {
    const selectedDistrict = pgrfilters.district?.[0];
    if (selectedDistrict) {
      const newBlockMenu = mdmsData?.Incident?.Block
        .filter((block) => block?.districtCode === selectedDistrict.districtCode)
        .sort((a, b) => a.name.localeCompare(b.name))
        .map((block) => ({
          blockCode: block.code,
          code: block.name,
          nonLocalizedName: block.name,
          name: t(block.name),
        }));

      setBlockMenu(newBlockMenu);
    }

    const selectedBlock = pgrfilters.block?.[0];
    if (selectedBlock) {
      const newPhcMenu = phcData?.tenant?.tenants
        .filter((centre) => centre?.city?.blockCode === selectedBlock.blockCode)
        .sort((a, b) => a.name.localeCompare(b.name))
        .map((centre) => ({
          ...centre,
          nonLocalizedName: centre?.name,
          name: t(centre?.name),
          centreType: t(centre?.centreType),
        }));

      setPhcMenu(newPhcMenu);
    }

  }, [pgrfilters, mdmsData, phcData, t]);
  
  const tenantId = Digit.ULBService.getCurrentTenantId();
  // let localities = Digit.Hooks.pgr.useLocalities({ city: tenantId });
  const { data: localities } = Digit.Hooks.useBoundaryLocalities(tenantId, "admin", {}, t);
  
  let serviceDefs = Digit.Hooks.pgr.useServiceDefs(tenantId, "Incident");
  const menu = Digit.Hooks.pgr.useComplaintTypes({ stateCode: tenantId })
  let sortedMenu=[];
  if(menu!==null){
    let othersItem = menu.find(item => item.key==="");
    let remainingOptions = menu.filter(item => item.key!=="");
    remainingOptions.sort((a, b) => a.name.localeCompare(b.name));
    if (othersItem) {
      remainingOptions.push(othersItem);
    }
    sortedMenu = remainingOptions
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

    if (key === "district") {
      setBlockMenu([]);
      setPhcMenu([]);
      setPgrFilters({ ...pgrfilters, district: [], block: [], phcType: [] });
    } else if (key === "block") {
      setPhcMenu([]);
      setPgrFilters({ ...pgrfilters, block: [], phcType: [] });
    } else {
      setPgrFilters({ ...pgrfilters, [key]: afterRemove });
    }
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

  const handleDistrictChange = (selectedDistrict) => {
    const previouslySelectedDistrict = pgrfilters.district[0];

    if (previouslySelectedDistrict?.code !== selectedDistrict.code) {
      setPgrFilters({ ...pgrfilters, district: [selectedDistrict], block: [], phcType: [] });
    }
  };

  const handleBlockChange = (selectedBlock) => {
    const previouslySelectedBlock = pgrfilters.block[0];

    if (previouslySelectedBlock?.code !== selectedBlock.code) {
      setPgrFilters({ ...pgrfilters, block: [selectedBlock], phcType: [] });
    }
  };

  const handleSystemFunctionalityChange = (selectedSystemFunctionality) => {
    setPgrFilters({...pgrfilters, isSystemFunctional: [selectedSystemFunctionality]});
  }

  function clearAll() {
    let pgrReset = {
      incidentType: [],
      phcType: [],
      district: [],
      block: [],
      isSystemFunctional: [],
      applicationStatus: []
    };
    let wfRest = { assigned: [{ code: [] }] };
    setBlockMenu([]);
    setPhcMenu([]);
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
                null,
                complaintType,
                "name",
                onRemove,
                "incidentType"
              )}
            </div>
            {isNonHcrUser && (
              <div>
                <div>
                  {
                    GetSelectOptions(
                      t("CS_DISTRICT"),
                      districtMenu,
                      null,
                      handleDistrictChange,
                      "name",
                      onRemove,
                      "district"
                    )
                  }
                </div>
                <div>
                  {
                    GetSelectOptions(
                      t("CS_BLOCK"),
                      blockMenu,
                      null,
                      handleBlockChange,
                      "name",
                      onRemove,
                      "block"
                    )
                  }
                </div>
                <div>
                  {
                    GetSelectOptions(
                      t("CS_HEALTH_CARE"),
                      phcMenu,
                      null,
                      onSelectHealthCare,
                      "name",
                      onRemove,
                      "phcType"
                    )
                  }
                </div>
              </div>
            )}
            <div>
              {
                GetSelectOptions(
                  t("CS_SYSTEM_FUNCTIONAL"),
                  systemFunctionalityMenu,
                  null,
                  handleSystemFunctionalityChange,
                  "name",
                  onRemove,
                  "isSystemFunctional"
                )
              }
            </div>
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
