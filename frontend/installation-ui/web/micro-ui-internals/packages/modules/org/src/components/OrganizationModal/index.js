// import React from "react";
// import { PopUp, Button, Toast } from "@egovernments/digit-ui-react-components";
// import OrganizationForm from "./OrganizationForm";
//
// const OrganizationModal = ({
//                              t,
//                              heading,
//                              onClose,
//                              onSubmit,
//                              formToast,
//                              setFormToast,
//                              setShowModal,
//                            }) => {
//   const handleClose = () => {
//     if (typeof onClose === "function") return onClose();
//     if (typeof setShowModal === "function") return setShowModal(false);
//   };
//
//   return (
//     <PopUp>
//       <div className="popup-module" style={{ position: "relative" }}>
//         <div
//           style={{
//             display: "flex",
//             alignItems: "center",
//             justifyContent: "space-between",
//             padding: "30px 30px 0 30px",
//           }}
//         >
//           <h1 style={{ margin: 0, fontWeight: 700 }}>
//             {heading || t("ADD_ORGANIZATION")}
//           </h1>
//
//           <Button
//             variation="secondary"
//             label={t("CORE_COMMON_CLOSE") || "Close"}
//             onButtonClick={handleClose}
//             style={{ minWidth: "120px", height: "40px" }}
//           />
//         </div>
//
//         <div style={{ padding: "0 30px 30px 30px" }}>
//           {formToast ? (
//             <Toast
//               error={formToast.key === "error"}
//               label={formToast.label}
//               onClose={() =>
//                 typeof setFormToast === "function" ? setFormToast(null) : null
//               }
//             />
//           ) : null}
//
//           <OrganizationForm t={t} onSubmit={onSubmit} />
//         </div>
//       </div>
//     </PopUp>
//   );
// };
//
// export default OrganizationModal;

import React from "react";
import { PopUp, Button, Toast, Loader } from "@egovernments/digit-ui-react-components";
import OrganizationForm from "./OrganizationForm";

const OrganizationModal = ({
                             t,
                             heading,
                             onClose,
                             onSubmit,
                             formToast,
                             setFormToast,
                             setShowModal,
                             isSubmitting, // ✅ new
                           }) => {
  const handleClose = () => {
    if (isSubmitting) return; // ✅ like FA: don’t close mid-submit
    if (typeof onClose === "function") return onClose();
    if (typeof setShowModal === "function") return setShowModal(false);
  };

  return (
    <PopUp>
      <div className="popup-module" style={{ position: "relative" }}>
        {/* ✅ Loader overlay on top of popup */}
        {isSubmitting ? (
          <div
            style={{
              position: "absolute",
              inset: 0,
              background: "rgba(255,255,255,0.75)",
              zIndex: 9999,
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
            }}
          >
            <Loader />
          </div>
        ) : null}

        <div
          style={{
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
            padding: "30px 30px 0 30px",
          }}
        >
          <h1 style={{ margin: 0, fontWeight: 700 }}>
            {heading || t("ADD_ORGANIZATION")}
          </h1>

          <Button
            variation="secondary"
            label={t("CORE_COMMON_CLOSE") || "Close"}
            onButtonClick={handleClose}
            style={{
              minWidth: "120px",
              height: "40px",
              opacity: isSubmitting ? 0.6 : 1,
              pointerEvents: isSubmitting ? "none" : "auto",
            }}
          />
        </div>

        <div style={{ padding: "0 30px 30px 30px" }}>
          {/* ✅ Toast inside modal (FA-style) */}
          {formToast ? (
            <Toast
              error={formToast.key === "error"}
              label={formToast.label}
              onClose={() => (typeof setFormToast === "function" ? setFormToast(null) : null)}
            />
          ) : null}

          <OrganizationForm t={t} onSubmit={onSubmit} />
        </div>
      </div>
    </PopUp>
  );
};

export default OrganizationModal;