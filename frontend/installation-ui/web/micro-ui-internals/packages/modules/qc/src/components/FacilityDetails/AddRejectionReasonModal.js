import React, { useEffect, useState } from "react";
import { Dropdown, DustbinIcon, TextArea } from "@egovernments/digit-ui-react-components";
import CustomCloseSvg from "../CustomCloseSvg";

const isOtherReason = (reason) => {
  return [reason?.label, reason?.name, reason?.code, reason?.i18nKey]
    .some((value) => ["other", "others"].includes(value?.toString?.().trim().toLowerCase()));
};

const sortReasonsWithOtherLast = (a, b) => {
  const isAOther = isOtherReason(a);
  const isBOther = isOtherReason(b);

  if (isAOther && !isBOther) return 1;
  if (!isAOther && isBOther) return -1;

  return a.label.localeCompare(b.label);
};

const AddRejectionReasonModal = ({ t, onClose, onSave, rejectionReasons }) => {

  const tenantId = Digit.ULBService.getCurrentTenantId();
  const [reasonOptions, setReasonOptions] = useState([]);
  const [reasonMenu, setReasonMenu] = useState([]);
  const [reasons, setReasons] = useState([{ id: Date.now(), reason: "", comment: "" }]);
  const [validationErrors, setValidationErrors] = useState({});

  const { data: mdmsData } = Digit.Hooks.useCustomMDMS(
    tenantId,
    "Installation",
    [
      {
        name: "RejectionReasons",
      },
    ],
    {
      enabled: !!tenantId,
    }
  );

  const addReason = () => {
    setReasons([...reasons, { id: Date.now(), reason: "", comment: "" }]);
  };

  useEffect(() => {
    setReasonOptions(
      (mdmsData?.["Installation"]?.["RejectionReasons"] || [])
        .map((rejectionReason) => ({...rejectionReason, label: rejectionReason.name}))
        .sort(sortReasonsWithOtherLast)
    )
  }, [mdmsData]);

  useEffect(() => {
    const savedRejectionCodes = rejectionReasons.map(r => r.reason);
    const newReasonMenu = reasonOptions.filter(option => !savedRejectionCodes.includes(option.label));
    newReasonMenu.sort(sortReasonsWithOtherLast);
    setReasonMenu(newReasonMenu);
  }, [rejectionReasons, reasonOptions]);

  const updateReasonsMenu = (selectedReasons) => {
    const newReasonMenu = reasonOptions.filter(option => !selectedReasons.includes(option.label));
    newReasonMenu.sort(sortReasonsWithOtherLast);
    setReasonMenu(newReasonMenu);
  }

  const updateReason = (id, key, value) => {
    const newReasons = reasons.map(r => r.id === id ? { ...r, [key]: value } : r);
    const selectedReasons = newReasons.map(r => r.reason);
    setReasons(newReasons);
    if (validationErrors[id]) {
      setValidationErrors({ ...validationErrors, [id]: "" });
    }
    updateReasonsMenu(selectedReasons);
  };

  const deleteReason = (id) => {
    const newReasons = reasons.filter(r => r.id !== id);
    const selectedReasons = newReasons.map(r => r.reason);
    setReasons(newReasons);
    if (validationErrors[id]) {
      const newValidationErrors = { ...validationErrors };
      delete newValidationErrors[id];
      setValidationErrors(newValidationErrors);
    }
    updateReasonsMenu(selectedReasons);
  };

  const handleSave = () => {
    const newValidationErrors = reasons.reduce((errors, reason) => {
      if (isOtherReason({ label: reason.reason }) && !reason.comment?.trim()) {
        errors[reason.id] = t("OTHER_ERRMSG");
      }
      return errors;
    }, {});

    if (Object.keys(newValidationErrors).length > 0) {
      setValidationErrors(newValidationErrors);
      return;
    }

    onSave(reasons);
    onClose();
  };

  return (
    <div style={styles.backdrop}>
      <div style={styles.modal}>
        <div style={styles.header}>
          <h2 style={{
            margin: 0,
            fontSize: "24px",
            color: "#0B4B66",
            fontWeight: "bold",
          }}>
            {t("CS_ACTION_ADD_REJECTION_REASON")}
          </h2>
          <CustomCloseSvg style={{ cursor: "pointer" }} onClick={onClose} />
        </div>

        {reasons.map((reason, index, reasonsArray) => (
          <div key={reason.id} style={styles.reasonBlock}>
            <div style={styles.reasonHeader}>
              <strong>{t("CORE_COMMON_REASON")} {index + 1}</strong>
              {reasonsArray.length > 1 && (
                <button onClick={() => deleteReason(reason.id)} style={styles.trashBtn}>
                  <DustbinIcon />
                </button>
              )}
            </div>
            <div className={"custom-dropdown"}>
              <Dropdown
                t={t}
                option={reasonMenu}
                selected={reasonMenu?.find((opt) => opt.label === reason.reason)}
                optionKey={"label"}
                select={(e) => updateReason(reason.id, 'reason', e.label)}
              />
            </div>
            <TextArea
              name={"comment"}
              onChange={(e) => updateReason(reason.id, 'comment', e.target.value)}
              value={reason.comment}
              placeholder={t("ES_ADDITIONAL_DETAILS_PLACEHOLDER")}
              style={{ fontFamily: "Roboto" }}
            />
            {validationErrors[reason.id] && (
              <div style={styles.errorText}>{validationErrors[reason.id]}</div>
            )}
          </div>
        ))}

        <button
          onClick={addReason}
          style={styles.addBtn}
        >
          <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
            <span
              style={{
                width: "25px",
                height: "25px",
                borderRadius: "5px",
                background: "#C1440E",
                color: "white",
                fontSize: "20px",
                fontWeight: "bold",
                cursor: "pointer",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
              }}
            >
              +
            </span>
            <span style={{
              fontSize: "16px",
              fontWeight: "bold",
            }}>
              {t("CS_ACTION_ADD_REASON")}
            </span>
          </div>
        </button>

        <div style={styles.footer}>
          <button onClick={onClose} style={styles.cancelBtn}>{t("CS_COMMON_CANCEL")}</button>
          <button onClick={handleSave} style={styles.saveBtn}>{t("CS_COMMON_SAVE")}</button>
        </div>
      </div>
    </div>
  );
};

const styles = {
  backdrop: {
    position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
    backgroundColor: 'rgba(0,0,0,0.4)', display: 'flex', justifyContent: 'center', alignItems: 'center',
    zIndex: 10000000
  },
  modal: {
    backgroundColor: '#fff', borderRadius: 4, width: 400, maxHeight: '90vh', overflowY: 'auto',
    boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)', position: 'relative'
  },
  header: {
    display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12, margin: 16
  },
  closeBtn: {
    background: 'none', border: 'none', fontSize: 20, cursor: 'pointer'
  },
  reasonBlock: {
    border: '1px solid #ccc', borderRadius: 4, padding: "12px 12px 0px 12px", marginBottom: 12, margin: 16
  },
  reasonHeader: {
    display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8
  },
  trashBtn: {
    background: 'none', border: 'none', fontSize: 18, cursor: 'pointer'
  },
  select: {
    width: '100%', padding: 8, marginBottom: 8, fontSize: 14
  },
  textarea: {
    width: '100%', padding: 8, fontSize: 14, minHeight: 60, resize: 'vertical', border: '1px solid #ccc'
  },
  addBtn: {
    display: 'flex', alignItems: 'center', justifyContent: 'center',
    backgroundColor: 'white', color: '#C1440E', border: 'none', padding: '8px 12px', borderRadius: 4,
    cursor: 'pointer', fontSize: 14, marginBottom: 12, marginRight: "auto", marginLeft: "auto"
  },
  footer: {
    display: 'flex', justifyContent: 'space-between', width: "100%", padding: 16,
    boxShadow: "0px 0px 4px #00000026"
  },
  cancelBtn: {
    backgroundColor: '#fff', color: '#C1440E', border: '2px solid #C1440E', padding: '8px 20px',
    fontWeight: 'bold', borderRadius: 2, cursor: 'pointer', width: "40%"
  },
  saveBtn: {
    backgroundColor: '#C1440E', color: '#fff', border: 'none', padding: '8px 20px',
    fontWeight: 'bold', borderRadius: 2, cursor: 'pointer', width: "40%"
  },
  errorText: {
    color: '#d4351c', fontSize: 12, marginTop: -8, marginBottom: 8
  }
};

export default AddRejectionReasonModal;
