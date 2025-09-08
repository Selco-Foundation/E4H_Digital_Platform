import React, {useEffect, useMemo, useState} from "react";
import {MultiSelectDropdown} from "@egovernments/digit-ui-react-components";

const DistrictSelector = ({
  data,
  setValue,
  props,
}) => {

  const { t, name, districtsIdentifier, boundaryData, defaultValues = {} } = props;
  const [selectedDistricts, setSelectedDistricts] = useState([]);
  const [blockMenu, setBlockMenu] = useState([]);
  const [selectedBlocks, setSelectedBlocks] = useState([]);
  const [loadDefaultValues, setLoadDefaultValues] = useState(true);

  useEffect(() => {
    if (Object.keys(defaultValues).length) {
      setLoadDefaultValues(true);
    } else {
      setLoadDefaultValues(false);
    }
  }, [defaultValues]);

  useEffect(() => {
    setSelectedDistricts(data[districtsIdentifier] || []);
  }, [data[districtsIdentifier]]);

  useEffect(() => {
    if (loadDefaultValues) {
      setSelectedDistricts(defaultValues[districtsIdentifier] || []);
      setSelectedBlocks(defaultValues[name] || []);
      setLoadDefaultValues(false);
    }
  }, [loadDefaultValues]);

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

      if (!loadDefaultValues) {
        const newSelectedBlocks = selectedBlocks.filter((block) => selectedDistrictCodes.includes(block.districtCode));
        setSelectedBlocks(newSelectedBlocks);
      }
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
