import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";

const useComplaintTypes = ({ stateCode, enabled = true }) => {
  const [complaintTypes, setComplaintTypes] = useState(null);
  const { t } = useTranslation();

  useEffect(() => {
    if (!enabled) {
      setComplaintTypes(null);
      return;
    }
    let cancelled = false;
    (async () => {
      const res = await Digit.GetServiceDefinitions.getMenu(stateCode, t);
      let menu = res.filter((o) => o.key !== "");
      //menu.push({ key: "Others", name: t("SERVICEDEFS.OTHERS") });
      if (!cancelled) setComplaintTypes(menu);
    })();
    return () => { cancelled = true; };
  }, [t, stateCode, enabled]);

  return complaintTypes;
};

export default useComplaintTypes;
