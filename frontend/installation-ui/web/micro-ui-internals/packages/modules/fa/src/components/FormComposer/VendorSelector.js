import React, { useEffect, useMemo, useState } from "react";
import { Dropdown } from "@egovernments/digit-ui-react-components";
import useVendorOrganisations from "../../hooks/useVendorOrganisations";

// Organisation names are free text, so the dropdown gets an identity t(): only the "none" option
// is a localization key, and it is translated before it goes in.
const untranslated = (value) => value;

/**
 * Add Facility's optional vendor organisation: one active vendor organisation, or none. More are
 * linked afterwards from the facility's Vendor Details.
 */
const VendorSelector = ({ data = {}, setValue, props }) => {
  const { t, name } = props;
  const { isLoading, data: vendors } = useVendorOrganisations();
  const [selected, setSelected] = useState(data[name] || null);

  const options = useMemo(() => [{ code: "", name: t("FA_NO_VENDOR_ORGANISATION") }, ...vendors], [vendors, t]);

  useEffect(() => {
    setValue(name, selected);
  }, [name, selected, setValue]);

  return (
    <Dropdown
      style={{ display: "flex", justifyContent: "space-between" }}
      disable={isLoading}
      t={untranslated}
      option={options}
      selected={selected}
      select={setSelected}
      optionKey={"name"}
      placeholder={t(isLoading ? "FA_LOADING_VENDOR_ORGANISATIONS" : "FA_SELECT_VENDOR_ORGANISATION")}
      optionsCardStyle={{ zIndex: 10000000, maxHeight: "300px" }}
    />
  );
};

export default VendorSelector;
