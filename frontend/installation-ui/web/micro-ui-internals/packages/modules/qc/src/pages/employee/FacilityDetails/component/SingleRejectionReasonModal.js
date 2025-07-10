import React, { useState } from 'react';

const reasonOptions = [
  "Serial Number Incorrect",
  "Model Number Incorrect",
  "Image Not Clear",
  "Incorrect Brand"
];

const SingleRejectionReasonModal = ({ name, onClose, onUpdate, onDelete, existingReason }) => {

  const [reason, setReason] = useState(existingReason);

  const updateReason = (id, key, value) => {
    setReason({ ...reason, [key]: value });
  };

  const handleDeletion = (id) => {
    onDelete(reason)
    onClose();
  };

  const handleSave = () => {
    onUpdate(reason);
    onClose();
  };

  return (
    <div style={styles.backdrop}>
      <div style={styles.modal}>
        <div style={styles.header}>
          <h2 style={{ margin: 0 }}>Rejection Reason</h2>
          <button onClick={onClose} style={styles.closeBtn}>✕</button>
        </div>
          <div key={reason.id} style={styles.reasonBlock}>
            <div style={styles.reasonHeader}>
              <strong>{name}</strong>
              <button onClick={handleDeletion} style={styles.trashBtn}>🗑</button>
            </div>
            <select
              value={reason.reason}
              onChange={(e) => updateReason(reason.id, 'reason', e.target.value)}
              style={styles.select}
            >
              <option value="">Select a reason</option>
              {reasonOptions.map((opt, i) => (
                <option key={i} value={opt}>{opt}</option>
              ))}
            </select>
            <textarea
              placeholder="Additional details for selected reason..."
              value={reason.comment}
              onChange={(e) => updateReason(reason.id, 'comment', e.target.value)}
              style={styles.textarea}
            />
          </div>
        <div style={styles.footer}>
          <button onClick={onClose} style={styles.cancelBtn}>Cancel</button>
          <button onClick={handleSave} style={styles.saveBtn}>Save</button>
        </div>
      </div>
    </div>
  );
};

const styles = {
  backdrop: {
    position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
    backgroundColor: 'rgba(0,0,0,0.4)', display: 'flex', justifyContent: 'center', alignItems: 'center',
    zIndex: 1000
  },
  modal: {
    backgroundColor: '#fff', borderRadius: 4, width: 400, maxHeight: '90vh', overflowY: 'auto',
    boxShadow: '0 4px 12px rgba(0,0,0,0.15)', padding: 16, position: 'relative'
  },
  header: {
    display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12
  },
  closeBtn: {
    background: 'none', border: 'none', fontSize: 20, cursor: 'pointer'
  },
  reasonBlock: {
    border: '1px solid #ccc', borderRadius: 4, padding: 12, marginBottom: 12
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
    backgroundColor: '#C1440E', color: '#fff', border: 'none', padding: '8px 12px', borderRadius: 4,
    cursor: 'pointer', fontSize: 14, marginBottom: 12
  },
  footer: {
    display: 'flex', justifyContent: 'space-between'
  },
  cancelBtn: {
    backgroundColor: '#fff', color: '#C1440E', border: '2px solid #C1440E', padding: '8px 20px',
    fontWeight: 'bold', borderRadius: 2, cursor: 'pointer'
  },
  saveBtn: {
    backgroundColor: '#C1440E', color: '#fff', border: 'none', padding: '8px 20px',
    fontWeight: 'bold', borderRadius: 2, cursor: 'pointer'
  }
};

export default SingleRejectionReasonModal;
