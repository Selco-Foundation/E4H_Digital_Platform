import React, { useEffect, useState } from "react";
import { Dropdown, DustbinIcon, TextArea } from "@egovernments/digit-ui-react-components";
import CustomCloseSvg from "../Custom/CustomCloseSvg";
import CustomDropdown from "../Custom/CustomDropdown";

const reasonOptions = [
  { label: "Uploaded images are not clear", code: "Uploaded images are not clear" },
  { label: "Battery/panel/inverter readings are not correct", code: "Battery/panel/inverter readings are not correct" },
  { label: "Corrective actions are not satisfactory", code: "Corrective actions are not satisfactory" },
  { label: "Remarks are not adequate", code: "Remarks are not adequate" }
];

const AddRejectionReasonModal = ({ t, onClose, handleConfirmRejection }) => {

  const [reasonMenu, setReasonMenu] = useState(reasonOptions)
  const [reasons, setReasons] = useState([{ id: Date.now(), reason: "", comment: "" }]);

  const addReason = () => {
    setReasons([...reasons, { id: Date.now(), reason: "", comment: "" }]);
  };

  const updateReasonsMenu = (selectedReasonCodes) => {
    const newReasonMenu = reasonOptions.filter(option => !selectedReasonCodes.includes(option.code));
    newReasonMenu.sort((a, b) => a.label.localeCompare(b.label));
    setReasonMenu(newReasonMenu);
  }

  const updateReason = (id, key, value) => {
    const newReasons = reasons.map(r => r.id === id ? { ...r, [key]: value } : r);
    const selectedReasonCodes = newReasons.map(r => r.reason);
    setReasons(newReasons);
    updateReasonsMenu(selectedReasonCodes);
  };

  const deleteReason = (id) => {
    const newReasons = reasons.filter(r => r.id !== id);
    const selectedReasonCodes = newReasons.map(r => r.reason);
    setReasons(newReasons);
    updateReasonsMenu(selectedReasonCodes);
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

        <div style={styles.body}>
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
              <CustomDropdown
                t={t}
                option={reasonMenu}
                selected={reasonOptions?.find((opt) => opt.code === reason.reason)}
                optionKey={"label"}
                select={(e) => updateReason(reason.id, 'reason', e.code)}
              />
              <TextArea
                name={"comment"}
                onChange={(e) => updateReason(reason.id, 'comment', e.target.value)}
                value={reason.comment}
                placeholder={t("ES_ADDITIONAL_DETAILS_PLACEHOLDER")}
                style={{ fontFamily: "Roboto" }}
              />
            </div>
          ))}
        </div>
        <button
          onClick={addReason}
          style={{
            ...styles.addBtn,
            opacity: reasons.length < reasonOptions?.length ? 1 : 0.5,
            cursor: reasons.length < reasonOptions?.length ? "pointer" : "default",
          }}
          disabled={reasons.length >= reasonOptions?.length}
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
          <button
            onClick={() => handleConfirmRejection(reasons)}
            style={{
              ...styles.saveBtn,
              opacity: !!reasons.filter((reason) => reason.reason.trim()).length ? 1 : 0.5,
              cursor: !!reasons.filter((reason) => reason.reason.trim()).length ? "pointer" : "default",
            }}
            disabled={!reasons.filter((reason) => reason.reason.trim()).length}
          >
            {t("CORE_COMMON_REJECT")}
          </button>
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
    backgroundColor: '#fff', borderRadius: 4, width: 400,
    boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)', position: 'relative'
  },
  body: {
    maxHeight: '60vh', overflowY: 'auto'
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
    boxShadow: "0px 0px 4px #00000026", overflowX: 'hidden'
  },
  cancelBtn: {
    backgroundColor: '#fff', color: '#C1440E', border: '2px solid #C1440E', padding: '8px 20px',
    fontWeight: 'bold', borderRadius: 2, cursor: 'pointer', width: "40%"
  },
  saveBtn: {
    backgroundColor: '#C1440E', color: '#fff', border: 'none', padding: '8px 20px',
    fontWeight: 'bold', borderRadius: 2, cursor: 'pointer', width: "40%"
  }
};

export default AddRejectionReasonModal;
