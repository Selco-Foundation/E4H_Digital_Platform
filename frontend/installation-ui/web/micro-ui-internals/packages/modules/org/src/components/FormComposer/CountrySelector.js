import React, { useEffect, useState } from "react";
import CustomDropdown from "../Custom/CustomDropdown";

const CountrySelector = ({ data = {}, setValue, props }) => {
  const { t, name, boundaryData, disable } = props;
  const [countryMenu, setCountryMenu] = useState([]);
  const [selectedCountry, setSelectedCountry] = useState(data[name]);

  useEffect(() => {
    if (boundaryData?.countries) {
      setCountryMenu(
        boundaryData.countries
          .map((country) => ({
            ...country,
            name: t(`Boundary_${country.code}`),
          }))
        .sort((a, b) => a?.name?.localeCompare(b?.name))
      );
    }
  }, [t, boundaryData]);

  useEffect(() => {
    setValue(name, selectedCountry);
  }, [name, selectedCountry, setValue]);

  const handleCountrySelection = (country) => {
    setSelectedCountry(country);
  };

  return (
    <div className={"employee-select-wrap"}>
      <CustomDropdown
        disable={disable}
        t={t}
        option={countryMenu}
        selected={selectedCountry}
        select={handleCountrySelection}
        optionKey={"name"}
        optionsCardStyle={{
          zIndex: 10000000,
          maxHeight: "400px",
        }}
      />
    </div>
  );
};

export default CountrySelector;


