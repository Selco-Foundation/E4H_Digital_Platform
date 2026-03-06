import React, { useEffect, useState } from "react";
import CustomDropdown from "../Custom/CustomDropdown";

const BlockSelector = ({ data = {}, setValue, props }) => {
  const { t, name, districtIdentifier, boundaryData, disable } = props;
  const [selectedDistrict, setSelectedDistrict] = useState(data[districtIdentifier]);
  const [blockMenu, setBlockMenu] = useState([]);
  const [selectedBlock, setSelectedBlock] = useState(data[name]);

  useEffect(() => {
    setSelectedDistrict(data[districtIdentifier]);
  }, [data, districtIdentifier]);

  useEffect(() => {
    if (boundaryData?.blocks && selectedDistrict?.code) {
      const newBlockMenu = boundaryData.blocks
        .filter((block) => block.parentCode === selectedDistrict.code)
        .map((block) => ({
          ...block,
          name: t(`Boundary_${block.code}`),
        }))
        .sort((a, b) => a?.name?.localeCompare(b?.name));
      setBlockMenu(newBlockMenu);

      if (selectedBlock && selectedBlock.parentCode !== selectedDistrict.code) {
        setSelectedBlock(null);
      }
    } else {
      setBlockMenu([]);
      setSelectedBlock(null);
    }
  }, [t, boundaryData, selectedDistrict, selectedBlock]);

  useEffect(() => {
    setValue(name, selectedBlock);
  }, [name, selectedBlock, setValue]);

  const handleBlockSelection = (block) => {
    setSelectedBlock(block);
  };

  return (
    <div className={"employee-select-wrap"}>
      <CustomDropdown
        disable={disable || !selectedDistrict}
        t={t}
        option={blockMenu}
        selected={selectedBlock}
        select={handleBlockSelection}
        optionKey={"name"}
        optionsCardStyle={{
          zIndex: 10000000,
          maxHeight: "400px"
        }}
      />
    </div>
  );
};

export default BlockSelector;


