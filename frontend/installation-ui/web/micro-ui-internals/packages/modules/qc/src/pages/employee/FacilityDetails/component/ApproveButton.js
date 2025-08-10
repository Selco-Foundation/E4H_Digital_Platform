import React from 'react';

const ApproveButton = ({ onClick }) => {
  return (
    <div style={{
      position: 'fixed',
      bottom: 0,
      right: 0,
      padding: '12px 24px',
      display: 'flex',
      justifyContent: 'flex-end',
      zIndex: 1000,
    }}>
      <button
        onClick={onClick}
        style={{
          backgroundColor: '#C1440E',
          color: '#fff',
          border: 'none',
          padding: '10px 24px',
          fontSize: '16px',
          fontWeight: 'bold',
          borderRadius: '2px',
          cursor: 'pointer'
        }}
      >
        Approve
      </button>
    </div>
  );
};

export default ApproveButton;
