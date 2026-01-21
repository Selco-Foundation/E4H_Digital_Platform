// import React, {useState} from "react";
// import {Toast} from "@egovernments/digit-ui-react-components";
// import {useTranslation} from "react-i18next";
//
// import OrganizationModal from "../OrganizationModal/index";
// import {OrganizationService} from "../../services/Organization";
// import {useQueryClient} from "react-query";
//
// const OrganizationAdminActions = () => {
//   const {t} = useTranslation();
//   const [showModal, setShowModal] = useState(false);
//   const [toast, setToast] = useState(null);
//   const [isSubmitting, setIsSubmitting] = useState(false);
//
//   const queryClient = useQueryClient();
//
//   const handleSubmit = async (formData) => {
//     try {
//       setIsSubmitting(true);
//
//       const tenantId = Digit.ULBService.getCurrentTenantId();
//
//       const payload = {
//         organisations: [
//           {
//             tenantId,
//             name: formData.orgName,
//             code: formData.orgCode,
//             orgStatus: formData.orgStatus ? formData.orgStatus.code : "ACTIVE",
//             orgType: formData.orgType ? formData.orgType.code : "VENDOR",
//
//             orgPocName: formData.orgPocName || null,
//             orgPocPhone: formData.orgPocPhone || null,
//             orgPocEmail: formData.orgPocEmail || null,
//             orgPocUsername: formData.orgPocPhone || null,
//
//             isActive: true,
//
//             orgAddress: [
//               {
//                 tenantId,
//                 boundaryType: null,
//                 boundaryCode: null,
//                 hqAddress: formData.hqAddress,
//                 geoLocation: {
//                   latitude: Number(formData.latitude),
//                   longitude: Number(formData.longitude),
//                 },
//               },
//             ],
//           },
//         ],
//       };
//
//       await OrganizationService.createOrganization(payload);
//
//       queryClient.invalidateQueries(["ORGANIZATIONS"]);
//       setShowModal(false);
//       setToast({key: "success", message: t("ORG_CREATE_SUCCESS")});
//     } catch (e) {
//       setToast({
//         key: "error",
//         message: (e && e.message) || t("ORG_CREATE_FAILED"),
//       });
//     } finally {
//       setIsSubmitting(false);
//       setTimeout(() => setToast(null), 3000);
//     }
//   };
//
//   return (
//     <div style={{display: "flex", alignItems: "center", gap: "12px"}}>
//       <button
//         type="button"
//         className="submit-bar"
//         style={{
//           borderRadius: "0px",
//           padding: "0px 22px",
//           backgroundColor: "#f47738",
//           display: "flex",
//           alignItems: "center",
//           height: "40px",
//           opacity: isSubmitting ? 0.7 : 1,
//           pointerEvents: isSubmitting ? "none" : "auto",
//         }}
//         onClick={() => setShowModal(true)}
//       >
//         <span style={{fontWeight: 400, fontSize: "18px", color: "#ffffff"}}>
//           {t("ADD_ORGANIZATION")}
//         </span>
//       </button>
//
//       {showModal ? (
//         <OrganizationModal
//           t={t}
//           setShowModal={setShowModal}
//           onSubmit={handleSubmit}
//           onClose={() => setShowModal(false)}
//         />
//       ) : null}
//
//       {toast ? (
//         <Toast
//           error={toast.key === "error"}
//           label={toast.message}
//           onClose={() => setToast(null)}
//         />
//       ) : null}
//     </div>
//   );
// };
//
// export default OrganizationAdminActions;

import React, {useState} from "react";
import {Toast} from "@egovernments/digit-ui-react-components";
import {useTranslation} from "react-i18next";

import OrganizationModal from "../OrganizationModal/index";
import {OrganizationService} from "../../services/Organization";
import {useQueryClient} from "react-query";

const extractApiError = (e, t) => {
  const msg =
    e?.response?.data?.Errors?.[0]?.message ||
    e?.response?.data?.error?.message ||
    e?.response?.data?.message ||
    e?.message ||
    t("ORG_CREATE_FAILED");
  return msg;
};

const OrganizationAdminActions = () => {
  const {t} = useTranslation();
  const [showModal, setShowModal] = useState(false);

  // global toast (optional, keep for success)
  const [toast, setToast] = useState(null);

  // ✅ modal toast (FA-style)
  const [formToast, setFormToast] = useState(null);

  const [isSubmitting, setIsSubmitting] = useState(false);
  const queryClient = useQueryClient();

  const handleSubmit = async (formData) => {
    try {
      setFormToast(null);
      setIsSubmitting(true);

      const tenantId = Digit.ULBService.getCurrentTenantId();

      const payload = {
        organisations: [
          {
            tenantId,
            name: formData.orgName,
            code: formData.orgCode,
            orgStatus: formData.orgStatus ? formData.orgStatus.code : "ACTIVE",
            orgType: formData.orgType ? formData.orgType.code : "VENDOR",

            orgPocName: formData.orgPocName || null,
            orgPocPhone: formData.orgPocPhone || null,
            orgPocEmail: formData.orgPocEmail || null,
            orgPocUsername: formData.orgPocPhone || null,

            isActive: true,

            orgAddress: [
              {
                tenantId,
                boundaryType: null,
                boundaryCode: null,
                hqAddress: formData.hqAddress,
                geoLocation: {
                  latitude: Number(formData.latitude),
                  longitude: Number(formData.longitude),
                },
              },
            ],
          },
        ],
      };

      await OrganizationService.createOrganization(payload);

      queryClient.invalidateQueries(["ORGANIZATIONS"]);

      setShowModal(false);
      setToast({key: "success", message: t("ORG_CREATE_SUCCESS")});
    } catch (e) {
      const msg = extractApiError(e, t);

      // ✅ show toast inside modal (FA-style)
      setFormToast({ key: "error", label: msg });
    } finally {
      setIsSubmitting(false);

      // auto-dismiss global toast only
      if (toast) setTimeout(() => setToast(null), 3000);
    }
  };

  return (
    <div style={{display: "flex", alignItems: "center", gap: "12px"}}>
      <button
        type="button"
        className="submit-bar"
        style={{
          borderRadius: "0px",
          padding: "0px 22px",
          backgroundColor: "#f47738",
          display: "flex",
          alignItems: "center",
          height: "40px",
          opacity: isSubmitting ? 0.7 : 1,
          pointerEvents: isSubmitting ? "none" : "auto",
        }}
        onClick={() => {
          setFormToast(null);
          setShowModal(true);
        }}
      >
        <span style={{fontWeight: 400, fontSize: "18px", color: "#ffffff"}}>
          {t("ADD_ORGANIZATION")}
        </span>
      </button>

      {showModal ? (
        <OrganizationModal
          t={t}
          setShowModal={setShowModal}
          onSubmit={handleSubmit}
          onClose={() => setShowModal(false)}
          // ✅ FA-style props
          isSubmitting={isSubmitting}
          formToast={formToast}
          setFormToast={setFormToast}
        />
      ) : null}

      {toast ? (
        <Toast
          error={toast.key === "error"}
          label={toast.message}
          onClose={() => setToast(null)}
        />
      ) : null}
    </div>
  );
};

export default OrganizationAdminActions;