import React, {useEffect, useState} from "react";
import {MultiSelectDropdown} from "@egovernments/digit-ui-components";

const BlockSelector = ({
  data = {},
  setValue,
  props,
}) => {

  const { t, name, districtsIdentifier, boundaryData, selectedOptions = [] } = props;
  const [selectedDistricts, setSelectedDistricts] = useState(data[districtsIdentifier] || []);
  const [blockMenu, setBlockMenu] = useState([]);
  const [selectedBlocks, setSelectedBlocks] = useState(
    data[name]?.sort((a, b) => a.code?.localeCompare(b.code)) || []
  );

  useEffect(() => {
    setSelectedDistricts(data[districtsIdentifier] || []);
  }, [data[districtsIdentifier]]);

  useEffect(() => {
    if (selectedBlocks.length) {
      setValue(name, selectedBlocks);
    } else {
      setValue(name, undefined);
    }
  }, [name, selectedBlocks]);

  useEffect(() => {
    if (boundaryData && selectedDistricts) {
      const selectedDistrictCodes = selectedDistricts.map((district) => district.code);

      const newBlockMenu = boundaryData.blocks
        ?.filter((block) => selectedDistrictCodes.includes(block.districtCode))
        .map((block) => ({
          ...block,
          name: `BLOCK_${block.code.toUpperCase()}`,
        }));
      setBlockMenu(newBlockMenu);

      const newSelectedBlocks = selectedBlocks.filter((block) => selectedDistrictCodes.includes(block.districtCode));
      setSelectedBlocks(newSelectedBlocks.sort((a, b) => a.code?.localeCompare(b.code)));
    }
  }, [t, boundaryData, selectedDistricts])

  const handleBlockSelection = (blocks = []) => {
    const selectedBlockCodes = blocks.map((block) => block.code);
    const selectedOptionsExcludedInSelection = selectedOptions.filter((option) => !selectedBlockCodes.includes(option.code));
    const newSelectedBlocks = [...blocks, ...selectedOptionsExcludedInSelection];
    setSelectedBlocks(newSelectedBlocks.sort((a, b) => a.code?.localeCompare(b.code)));
  }

  return (
    <div className={"employee-select-wrap"}>
      <MultiSelectDropdown
        options={blockMenu}
        optionsKey={"name"}
        isSearchable={true}
        onSelect={() => {
          // Triggering state update here causes render issues since dropdown within remains open
        }}
        onClose={(e) => {
          handleBlockSelection(e?.map(row=>{return row?.[1] ? row[1] : null}).filter(e=>e));
        }}
        defaultLabel={selectedBlocks.length ? `${selectedBlocks.length} Selected` : ""}
        selected={selectedBlocks}
        addSelectAllCheck={true}
        frozenData={[...selectedOptions]}
        selectAllLabel={t("PM_ACTION_SELECT_ALL_BLOCKS")}
      />
    </div>
  );
};

export default BlockSelector;
