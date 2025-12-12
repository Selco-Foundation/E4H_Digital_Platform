import { Dropdown } from "@selco/digit-ui-react-components";
import React, { useState, useEffect } from "react";
import { CustomButton, Menu } from "@selco/digit-ui-react-components";
import { useHistory } from "react-router-dom";

const stringReplaceAll = (str = "", searcher = "", replaceWith = "") => {
  if (searcher == "") return str;
  while (str?.includes(searcher)) {
    str = str?.replace(searcher, replaceWith);
  }
  return str;
};

const ChangeCity = (prop) => {
  const [dropDownData, setDropDownData] = useState(null);
  const [selectCityData, setSelectCityData] = useState([]);
  let sortSelectCityData=[];
  if(selectCityData.length>0){
    sortSelectCityData=selectCityData.sort((a, b) => a.label.localeCompare(b.label));
  }
  const history = useHistory();
  const jurisdictionBoundaries = Digit.SessionStorage.get("Jurisdiction.Boundaries");
  const [facilityOptions, setFacilityOptions] = useState([]);
  const [facilityBoundaries, setFacilityBoundaries] = useState([]);
  const [facilityBoundaryCodes, setFacilityBoundaryCodes] = useState(["-"]);

  const { data: boundaryData } = Digit.Hooks.im.useBoundary(Digit.Utils.BoundaryUtil.aggregateBoundaryCodes(jurisdictionBoundaries) || []);
  const { data: facilityData } = Digit.Hooks.im.useFacility(facilityBoundaryCodes);
  const { t } = prop;

  useEffect(() => {
    if (boundaryData) {
      setFacilityBoundaries(boundaryData.facilities);
      setFacilityBoundaryCodes(boundaryData.facilities?.map((facility) => facility?.code) || ["-"]);
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

  const handleChangeCity = (city) => {
    setDropDownData(city);
    Digit.SessionStorage.set("Jurisdiction.CurrentBoundary", city.type === "UNIFIED" ? jurisdictionBoundaries : {
      [city.type]: city.code.split(","),
    })
    if (window.location.href.includes(`/${window.contextPath}/employee/`)) {
      const redirectPath = location.state?.from || `/${window.contextPath}/employee`;
      history.replace(redirectPath);
    }
    window.location.reload();
  };

  useEffect(() => {
    const jurisdictionBoundaryCodes = Digit.Utils.BoundaryUtil.aggregateBoundaryCodes(jurisdictionBoundaries);
    const jurisdictionBoundaryTypes = Digit.Utils.BoundaryUtil.aggregateBoundaryTypes(jurisdictionBoundaries);
    const isOnlyFacilityType = jurisdictionBoundaryTypes.length === 1 && jurisdictionBoundaryTypes[0] === "facility";
    let filteredArray = [
      {
        code: jurisdictionBoundaryCodes?.join(","),
        label: (jurisdictionBoundaryCodes?.length === 1 &&  isOnlyFacilityType) ? t(`Boundary_${jurisdictionBoundaryCodes?.[0]}`) : t("CORE_COMMON_ALL"),
        type: "UNIFIED",
      }
    ];
    if (facilityOptions) {
      facilityOptions.forEach((facility) => {
        if (filteredArray.every((boundary) => boundary.code !== facility.code)) {
          filteredArray.push({
            code: facility.code,
            label: t(`Boundary_${facility.code}`),
            type: "facility",
          })
        }
      })
    }
    filteredArray.sort((a, b) => a.label.localeCompare(b.label));
    const jurisdictionCurrentBoundary = Digit.SessionStorage.get("Jurisdiction.CurrentBoundary");
    const jurisdictionCurrentBoundaryCodes = Digit.Utils.BoundaryUtil.aggregateBoundaryCodes(jurisdictionCurrentBoundary);
    const selectedBoundary = filteredArray?.find(select => select?.code === jurisdictionCurrentBoundaryCodes?.join(","));
    setSelectCityData(filteredArray);
    setDropDownData(selectedBoundary);
  }, [facilityOptions, t]);

  // if (isDropdown) {
  return (
    <div style={prop?.mobileView ? {color: "#767676", width:"95%", marginBottom: "1rem"} : {width:"300px"}}>
      <Dropdown
        option={sortSelectCityData}
        optionCardStyles={{ display: "unset" }}
        selected={dropDownData}
        optionKey={"label"}
        select={handleChangeCity}
      />
    </div>
  );
  // } else {
  //   return (
  //     <React.Fragment>
  //       <div style={{ marginBottom: "5px" }}>City</div>
  //       <div className="language-selector" style={{display: "flex", flexWrap: "wrap"}}>
  //         {selectCityData?.map((city, index) => (
  //           <div className="language-button-container" key={index}>
  //             <CustomButton
  //               selected={city.value === Digit.SessionStorage.get("Employee.tenantId")}
  //               text={city.label}
  //               onClick={() => handleChangeCity(city)}
  //             ></CustomButton>
  //           </div>
  //         ))}
  //       </div>
  //     </React.Fragment>
  //   );
  // }
};

export default ChangeCity;
