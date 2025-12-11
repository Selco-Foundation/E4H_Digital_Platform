import React, { useEffect, useMemo, useState } from "react";
import { Dropdown, RadioButtons, ActionBar, RemoveableTag, RoundedLabel } from "@selco/digit-ui-react-components";
import { ApplyFilterBar, CloseSvg } from "@selco/digit-ui-react-components";
import { useTranslation } from "react-i18next";
import Status from "./Status";

let pgrQuery = {};
let wfQuery = {};

const Filter = (props) => {
  const { userName } = Digit.UserService.getUser().info;
  const { searchParams } = props;
  const { t } = useTranslation();
  const [districtMenu, setDistrictMenu] = useState([]);
  const [blockMenu, setBlockMenu] = useState([]);
  const [facilityMenu, setFacilityMenu] = useState([]);
  const [facilityOptions, setFacilityOptions] = useState([]);
  const [facilityBoundaries, setFacilityBoundaries] = useState([]);
  const [facilityBoundaryCodes, setFacilityBoundaryCodes] = useState(["-"]);
  const [systemFunctionalityMenu, setSystemFunctionalityMenu] = useState([]);

  const assignedToOptions = useMemo(
    () => [
      { code: "ASSIGNED_TO_ME", name: t("ASSIGNED_TO_ME") },
      { code: "ASSIGNED_TO_ALL", name: t("ASSIGNED_TO_ALL") },
    ],
    [t]
  );
  const loggedInUser = Digit.UserService.getUser();
  const isAssignedToMe = searchParams?.filters?.wfFilters?.assignee?.[0]?.code === userName;

  const jurisdictionCurrentBoundary = Digit.SessionStorage.get("Jurisdiction.CurrentBoundary") || {};
  const jurisdictionCurrentBoundaryCodes = Digit.Utils.BoundaryUtil.aggregateBoundaryCodes(jurisdictionCurrentBoundary);
  const { data: boundaryData } = Digit.Hooks.im.useBoundary(jurisdictionCurrentBoundaryCodes);
  const { data: facilityData } = Digit.Hooks.im.useFacility(facilityBoundaryCodes);

  useEffect(() => {
    if (boundaryData) {
      setFacilityBoundaries(boundaryData.facilities);
      setFacilityBoundaryCodes(boundaryData.facilities?.map((facility) => facility?.code));
    }
  }, [boundaryData]);

  useEffect(() => {
    if (facilityBoundaries?.length && facilityData?.facilities?.length) {
      const facilityBoundaryCodeToParentMap = new Map();
      for (let facilityBoundary of facilityBoundaries) {
        facilityBoundaryCodeToParentMap.set(facilityBoundary.code, facilityBoundary.parentCode);
      }
      setFacilityOptions(facilityData?.facilities?.map((facility) => ({
        code: facility.boundaryCode,
        id: facility.facilityId,
        parentCode: facilityBoundaryCodeToParentMap.get(facility.boundaryCode),
      })));
    }
  }, [facilityBoundaries, facilityData]);

  const state = Digit.ULBService.getStateId();
  const { data: mdmsData } = Digit.Hooks.pgr.useMDMS(state, "Incident", ["SystemFunctionality"]);
  const isNonHcrUser = Digit.UserService.getUser()?.info.roles.some(role => (role.code !== "EMPLOYEE" && role.code !== "COMPLAINANT"));

const isCodePresent = (array, codeToCheck) =>{
  return array.some(item => item.code === codeToCheck);
}

  const [selectAssigned, setSelectedAssigned] = useState(
    (isAssignedToMe || isCodePresent(loggedInUser?.info?.roles, "COMPLAINT_RESOLVER")) ? assignedToOptions[0] : assignedToOptions[1]
  );

  const [pgrfilters, setPgrFilters] = useState(
    searchParams?.filters?.pgrfilters || {
      incidentType: [],
      facility: [],
      district: [],
      block: [],
      isSystemFunctional: [],
      applicationStatus: [],
    }
  );

  const [wfFilters, setWfFilters] = useState(
    searchParams?.filters?.wfFilters ||
    (isCodePresent(loggedInUser?.info?.roles, "COMPLAINT_RESOLVER") ?
      { assignee: [{ code: userName }] } :
      { assignee: [{ code: "" }] })
  );

  useEffect(() => {
    const refactorDistrictMenu = () => {
      const response = boundaryData?.districts;
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
          .map((district) => ({
            code: district.code,
            name: t(`Boundary_${district.code}`),
          }))
          .sort((a, b) => a.name.localeCompare(b.name));

        setDistrictMenu(newDistrictMenu);
        setBlockMenu([]);
        setFacilityMenu([]);
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
  }, [boundaryData, t]);

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
      district: prevFilters.district.map((district) => ({ ...district, name: t(`Boundary_${district.code}`), })),
      block: prevFilters.block.map((block) => ({ ...block, name: t(`Boundary_${block.code}`), })),
      facility: prevFilters.facility.map((facility) => ({ ...facility, name: t(`Boundary_${facility.code}`), })),
      isSystemFunctional: prevFilters.isSystemFunctional.map((systemFunctionality) => ({
        ...systemFunctionality,
        name: t(systemFunctionality.nonLocalizedName)
      })),
    }));
  }, [t]);

  useEffect(() => {
    const selectedDistrict = pgrfilters.district?.[0];
    if (selectedDistrict && boundaryData) {
      const newBlockMenu = boundaryData.blocks
        .filter((block) => block?.parentCode === selectedDistrict.code)
        .map((block) => ({
          code: block?.code,
          name: t(`Boundary_${block?.code}`),
          districtCode: block?.parentCode,
        }))
        .sort((a, b) => a?.name?.localeCompare(b?.name));

      setBlockMenu(newBlockMenu);
    }

    const selectedBlock = pgrfilters.block?.[0];
    if (selectedBlock && boundaryData) {
      const newFacilityMenu = facilityOptions
        .filter((facility) => facility?.parentCode === selectedBlock.code)
        .map((facility) => ({
          code: facility?.code,
          name: t(`Boundary_${facility?.code}`),
          blockCode: facility?.parentCode,
        }))
        .sort((a, b) => a?.name?.localeCompare(b?.name));

      setFacilityMenu(newFacilityMenu);
    }

  }, [pgrfilters, boundaryData, facilityOptions, t]);

  useEffect(() => {
    const code = selectAssigned.code === "ASSIGNED_TO_ME" ? userName : "";
    setWfFilters(prevFilters => ({ ...prevFilters, assignee: [{ code: code }] }));
  }, [selectAssigned]);

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

  function onSelectHealthCare(value, key) {
    if(!value) return
    if (!ifExists(pgrfilters[key], value)) {
      setPgrFilters({ ...pgrfilters, [key]: [...pgrfilters[key], value] });
    }
  }

  const onRemove = (index, key) => {
    let afterRemove = pgrfilters[key].filter((value, i) => {
      return i !== index;
    });

    if (key === "district") {
      setBlockMenu([]);
      setFacilityMenu([]);
      setPgrFilters({ ...pgrfilters, district: [], block: [], facility: [] });
    } else if (key === "block") {
      setFacilityMenu([]);
      setPgrFilters({ ...pgrfilters, block: [], facility: [] });
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
      setPgrFilters({ ...pgrfilters, district: [selectedDistrict], block: [], facility: [] });
    }
  };

  const handleBlockChange = (selectedBlock) => {
    const previouslySelectedBlock = pgrfilters.block[0];

    if (previouslySelectedBlock?.code !== selectedBlock.code) {
      setPgrFilters({ ...pgrfilters, block: [selectedBlock], facility: [] });
    }
  };

  const handleSystemFunctionalityChange = (selectedSystemFunctionality) => {
    setPgrFilters({...pgrfilters, isSystemFunctional: [selectedSystemFunctionality]});
  }

  function clearAll() {
    let pgrReset = {
      incidentType: [],
      facility: [],
      district: [],
      block: [],
      isSystemFunctional: [],
      applicationStatus: []
    };
    let wfRest = { assigned: [{ code: [] }] };
    setBlockMenu([]);
    setFacilityMenu([]);
    setPgrFilters(pgrReset);
    setWfFilters(wfRest);
    pgrQuery = {};
    wfQuery = {};
    setSelectedAssigned(
      (isAssignedToMe || isCodePresent(loggedInUser?.info?.roles, "COMPLAINT_RESOLVER")) ? assignedToOptions[0] : assignedToOptions[1]
    );
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
                      facilityMenu,
                      null,
                      onSelectHealthCare,
                      "name",
                      onRemove,
                      "facility"
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
