import React, {useEffect, useMemo, useState} from "react";
import {Dropdown, MultiSelectDropdown} from "@egovernments/digit-ui-react-components";
import _ from "lodash";

const DistrictSelector = ({
  setValue,
  props,
}) => {

  const { t, name, districtsIdentifier, boundaryData, defaultValues = {} } = props;
  const [selectedDistricts, setSelectedDistricts] = useState([]);
  const [blockMenu, setBlockMenu] = useState([]);
  const [selectedBlocks, setSelectedBlocks] = useState([]);

  useEffect(() => {
    setSelectedDistricts(defaultValues[districtsIdentifier] || []);
  }, [defaultValues[districtsIdentifier]]);

  useEffect(() => {
    if (defaultValues[name] && !_.isEqual(_.sortBy(defaultValues[name], "code"), _.sortBy(selectedBlocks, "code"))) {
      setSelectedBlocks(defaultValues[name]);
    }
  }, [defaultValues[name]]);

  useEffect(() => {
    if (selectedBlocks?.length) {
      setValue(name, selectedBlocks);
    } else {
      setValue(name, undefined);
    }
  }, [name, selectedBlocks]);

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

  const handleBlockSelection = (blocks) => {
    setSelectedBlocks(blocks);
  }

  return (
    <div className={"employee-select-wrap"}>
      <MultiSelectDropdown
        options={blockMenu}
        optionsKey={"name"}
        onSelect={(e) => {
          handleBlockSelection(e?.map(row=>{return row?.[1] ? row[1] : null}).filter(e=>e))
        }}
        selected={selectedBlocks}
      />
    </div>
  );
};

export default DistrictSelector;
