import React, {useEffect, useMemo, useState} from "react";
import {Dropdown, MultiSelectDropdown} from "@egovernments/digit-ui-react-components";

const DistrictSelector = ({
  data = {},
  setValue,
  props,
  setError,
  clearErrors,
}) => {

  const [selectedDistricts, setSelectedDistricts] = useState(data.districts || []);
  const [blockMenu, setBlockMenu] = useState([]);
  const [selectedBlocks, setSelectedBlocks] = useState(data.blocks || []);
  const { t, name, boundaryData } = props;

  useEffect(() => {
    setSelectedDistricts(data.districts || []);
  }, [data.districts]);

  useEffect(() => {
    if (selectedBlocks?.length) {
      setValue(name, selectedBlocks);
    } else {
      setValue(name, undefined);
    }
  }, [selectedBlocks, setError, clearErrors, name]);

  useEffect(() => {
    if (boundaryData && selectedDistricts) {
      const selectedDistrictCodes = selectedDistricts.map((district) => district.code);

      const newBlockMenu = boundaryData.blocks
        .filter((block) => selectedDistrictCodes.includes(block.districtCode))
        .map((block) => ({
          ...block,
          name: t(`BLOCK_${block.code.toUpperCase()}`),
        }));
      setBlockMenu(newBlockMenu);

      const newSelectedBlocks = selectedBlocks.filter((block) => selectedDistrictCodes.includes(block.districtCode));
      setSelectedBlocks(newSelectedBlocks);
    }
  }, [t, boundaryData, selectedDistricts])

  const handleDistrictSelection = (districts) => {
    setSelectedBlocks(districts);
  }

  return (
    <div className={"employee-select-wrap"}>
      <MultiSelectDropdown
        options={blockMenu}
        optionsKey={"name"}
        onSelect={(e) => {
          handleDistrictSelection(e?.map(row=>{return row?.[1] ? row[1] : null}).filter(e=>e))
        }}
        selected={selectedBlocks}
      />
    </div>
  );
};

export default DistrictSelector;
