import React from "react";

const SystemParameterReport = ({ pdf, onRemove }) => {
  return (
    <div>
      <div style={{ padding: "20px" }}>
        <div style={{
          border: '1px solid #eee',
          borderRadius: '6px',
          padding: '16px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          width: '20%',
          position: 'relative'
        }}>
          <div style={{ display: 'flex', alignItems: 'center' }}>
            <img src="https://cdn-icons-png.flaticon.com/512/337/337946.png" alt="pdf icon" width="40" style={{ marginRight: '12px' }} />
            <div>
              <div style={{ fontWeight: 'bold', fontSize: '16px' }}>{pdf.name}</div>
              <div style={{ color: '#666', fontSize: '14px' }}>{pdf.size}</div>
            </div>
          </div>
          <button
            onClick={onRemove}
            style={{
              background: 'none',
              border: '1px solid #eee',
              fontSize: '16px',
              color: '#555',
              position: 'absolute',
              top: '6px',
              right: '6px',
              cursor: 'pointer'
            }}
            aria-label="Remove PDF"
          >
            X
          </button>
        </div>
      </div>
    </div>
  );
};

export default SystemParameterReport;
