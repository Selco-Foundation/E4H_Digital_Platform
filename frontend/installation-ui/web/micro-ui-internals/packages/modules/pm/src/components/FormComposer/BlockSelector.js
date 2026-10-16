import React, {useEffect, useState} from "react";
import {MultiSelectDropdown} from "@egovernments/digit-ui-components";
import {inSelectionOrder} from "../../utilities/GeographyUtils";

const BlockSelector = ({
  data = {},
  setValue,
  props,
}) => {

  const { t, name, districtsIdentifier, boundaryData, selectedOptions = [], keepSelectionOrder = false } = props;
  // Alphabetical by code unless the caller needs the order the user picked them in.
  const ordered = (list) => (keepSelectionOrder ? list : list.sort((a, b) => a.code?.localeCompare(b.code)));
  const [selectedDistricts, setSelectedDistricts] = useState(data[districtsIdentifier] || []);
  const [blockMenu, setBlockMenu] = useState([]);
  const [selectedBlocks, setSelectedBlocks] = useState(
    ordered([...(data[name] || [])])
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
          name: `Boundary_${block.code}`,
        }));
      setBlockMenu(newBlockMenu);

      const newSelectedBlocks = selectedBlocks.filter((block) => selectedDistrictCodes.includes(block.districtCode));
      setSelectedBlocks(ordered(newSelectedBlocks));
    }
  }, [t, boundaryData, selectedDistricts])

  const handleBlockSelection = (blocks = []) => {
    const selectedBlockCodes = blocks.map((block) => block.code);
    const selectedOptionsExcludedInSelection = selectedOptions.filter((option) => !selectedBlockCodes.includes(option.code));
    const newSelectedBlocks = [...blocks, ...selectedOptionsExcludedInSelection];
    setSelectedBlocks(keepSelectionOrder ? inSelectionOrder(selectedBlocks, newSelectedBlocks) : ordered(newSelectedBlocks));
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
