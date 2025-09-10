import React, {useEffect, useState} from "react";
import {MultiSelectDropdown} from "@egovernments/digit-ui-react-components";
import _ from "lodash";

const BlockSelector = ({
  data = {},
  setValue,
  props,
}) => {

  const { t, name, districtsIdentifier, boundaryData } = props;
  const [selectedDistricts, setSelectedDistricts] = useState(data[districtsIdentifier] || []);
  const [blockMenu, setBlockMenu] = useState([]);
  const [selectedBlocks, setSelectedBlocks] = useState(data[name] || []);

  useEffect(() => {
    setSelectedDistricts(data[districtsIdentifier] || []);
  }, [data[districtsIdentifier]]);

  useEffect(() => {
    if (selectedBlocks.filter((block) => block.code !== "ALL_BLOCKS").length) {
      setValue(name, selectedBlocks.filter((block) => block.code !== "ALL_BLOCKS"));
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
      setBlockMenu(
        newBlockMenu ? [
          {
            code: "ALL_BLOCKS",
            name: "PM_ACTION_SELECT_ALL_BLOCKS",
          },
          ...newBlockMenu
        ] : []
      );

      const newSelectedBlocks = selectedBlocks.filter((block) => selectedDistrictCodes.includes(block.districtCode));
      setSelectedBlocks(newSelectedBlocks);
    }
  }, [t, boundaryData, selectedDistricts])

  const handleBlockSelection = (blocks) => {
    const currentBlockSelection = blocks.map(block => block.code).sort((a, b) => a.code?.localeCompare(b.code));
    const previousBlockSelection = selectedBlocks.map((block) => block.code).sort((a, b) => a.code?.localeCompare(b.code));

    if (!_.isEqual(currentBlockSelection, previousBlockSelection)) {
      const currentSelectionSelectAll = currentBlockSelection.includes("ALL_BLOCKS");
      const previousSelectionSelectAll = previousBlockSelection.includes("ALL_BLOCKS");

      if (previousSelectionSelectAll) {
        if (currentSelectionSelectAll) {
          setSelectedBlocks(blocks.filter((block) => block.code !== "ALL_BLOCKS"));
        } else if (blocks.length === blockMenu.length - 1) {
          setSelectedBlocks([]);
        } else {
          setSelectedBlocks(blocks);
        }
      } else if (currentSelectionSelectAll || blocks.length === blockMenu.length - 1) {
        setSelectedBlocks(blockMenu);
      } else {
        setSelectedBlocks(blocks);
      }
    }
  }

  return (
    <div className={"employee-select-wrap"}>
      <MultiSelectDropdown
        options={blockMenu}
        optionsKey={"name"}
        onSelect={(e) => {
          handleBlockSelection(e?.map(row=>{return row?.[1] ? row[1] : null}).filter(e=>e))
        }}
        defaultLabel={`${selectedBlocks.filter((block) => block.code !== "ALL_BLOCKS").length || ""}`}
        selected={selectedBlocks}
      />
    </div>
  );
};

export default BlockSelector;
