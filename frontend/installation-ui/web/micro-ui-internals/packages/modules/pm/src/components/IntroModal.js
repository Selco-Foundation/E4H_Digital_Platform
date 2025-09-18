import React, {useEffect, Fragment} from "react";
import ReactDOM from "react-dom";
import {SubmitBar} from "@egovernments/digit-ui-react-components";
import CustomArrowRight from "../components/Custom/CustomArrowRight";
import CustomCloseSvg from "../components/Custom/CustomCloseSvg";
import {welcome_1} from "../media/welcome_1";
import {welcome_2} from "../media/welcome_2";
import {welcome_3} from "../media/welcome_3";

const overlayRootId = "project-intro-overlay-root";

const ensureOverlayRoot = () => {
  let node = document.getElementById(overlayRootId);
  if (!node) {
    node = document.createElement("div");
    node.id = overlayRootId;
    document.body.appendChild(node);
  }
  return node;
};

const styles = {
  overlay: {
    position: "fixed",
    inset: 0,
    background: "rgba(0,0,0,0.05)",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    zIndex: 10000000
  },
  container: {
    background: "#fff",
    width: "100%",
    maxWidth: "82rem", // ~1312px
    borderRadius: "0",
    border: "1px solid #EEE",
    boxShadow: "0 10px 25px rgba(0,0,0,0.05)",
    overflow: "hidden",
    fontFamily: "Roboto, system-ui, -apple-system, Segoe UI, Helvetica, Arial",
    margin: "10px"
  },
  header: {
    display: "flex",
    alignItems: "flex-start",
    justifyContent: "space-between",
    padding: "1.5rem 1.5rem 0.75rem 1.5rem",
  },
  titleWrap: {marginRight: "1rem", flex: 1},
  title: {
    margin: 0,
    color: "#0B4B66",
    fontSize: "40px",
    fontWeight: 700,
    lineHeight: 1.15,
    letterSpacing: "0",
  },
  subtitle: {
    margin: "0.25rem 0 0 0",
    color: "#111827",
    fontSize: "20px",
    lineHeight: 1.4,
    fontWeight: 500,
  },
  closeBtn: {
    border: "none",
    background: "transparent",
    fontSize: "24px",
    color: "#000",
    cursor: "pointer",
    lineHeight: 1,
  },
  body: {padding: "1.5rem"},
  intro: {
    color: "#000000",
    fontSize: "16px",
    margin: 0,
  },
  grid: {
    display: "grid",
    gap: "2rem 1rem",
    marginTop: "2rem",
    gridTemplateColumns: "1fr",
  },
  gridLg: "@media (min-width: 768px)",
  gridXl: "@media (min-width: 1024px)",
  item: {
    display: "flex",
    alignItems: "center",
    gap: "0.75rem",
    paddingRight: "1.25rem",
    borderRight: "1px solid #E5E7EB",
  },
  itemNoBorder: {borderRight: "none"},
  itemTitle: {
    margin: 0,
    padding: 0,
    color: "#0B4B66",
    fontSize: "1rem",
    fontWeight: 700,
  },
  itemSub: {
    margin: "0.25rem 0 0 0",
    color: "#6B7280",
    fontSize: "0.85rem",
  },
  footer: {
    display: "flex",
    justifyContent: "flex-end",
    padding: "0 3rem 2rem 1.5rem",
    marginTop: "0.5rem",
  }
};

const gridStyles = `
    @media (min-width: 768px) {
      #pm-modal-grid { grid-template-columns: repeat(2, 1fr) !important; }
    }
    @media (min-width: 1024px) {
      #pm-modal-grid { grid-template-columns: repeat(3, 1fr) !important; }
    }
`;

const IntroModal = ({open, onClose, t, action}) => {
  useEffect(() => {
    const onKey = (e) => e.key === "Escape" && onClose?.();
    if (open) document.addEventListener("keydown", onKey);
    return () => document.removeEventListener("keydown", onKey);
  }, [open, onClose]);

  if (!open) return null;

  const root = ensureOverlayRoot();

  const handleOverlayClick = (e) => {
    if (e.target === e.currentTarget) onClose?.();
  };

  return ReactDOM.createPortal(
    <Fragment>
      <style>{gridStyles}</style>
      <div style={styles.overlay} onClick={handleOverlayClick} role="dialog" aria-modal="true">
        <div style={styles.container} onClick={(e) => e.stopPropagation()}>
          <div style={styles.header}>
            <div style={styles.titleWrap}>
              <h2 style={styles.title}>
                {t("PM_BEFORE_CREATING_PROJECT_TITLE")}
              </h2>
              <p style={styles.subtitle}>
                {t("PM_BEFORE_CREATING_PROJECT_SUBTITLE")}
              </p>
            </div>
            <button aria-label="Close" style={styles.closeBtn} onClick={onClose}>
              <CustomCloseSvg/>
            </button>
          </div>

          <div style={styles.body}>
            <p style={styles.intro}>
              {t("PM_BEFORE_CREATING_PROJECT_DESC")}
            </p>

            <div id="pm-modal-grid" style={styles.grid}>
              <div style={styles.item}>
                <img width={120} src={welcome_1} alt={t("PM_TEAM_MEMBER_DETAILS")} loading="lazy"/>
                <div>
                  <h3 style={styles.itemTitle}>
                    {t("PM_TEAM_MEMBER_DETAILS")}
                  </h3>
                  <p style={styles.itemSub}>
                    {t("PM_TEAM_MEMBER_FIELDS")}
                  </p>
                </div>
              </div>

              <div style={styles.item}>
                <img width={120} src={welcome_2} alt={t("PM_HEALTH_FACILITIES_LIST")} loading="lazy"/>
                <div>
                  <h3 style={styles.itemTitle}>
                    {t("PM_HEALTH_FACILITIES_LIST")}
                  </h3>
                  <p style={styles.itemSub}>
                    {t("PM_HEALTH_FACILITIES_DESC")}
                  </p>
                </div>
              </div>

              <div style={{...styles.item, ...styles.itemNoBorder}}>
                <img width={120} src={welcome_3} alt={t("PM_SAVE_REPORTS")} loading="lazy"/>
                <div>
                  <h3 style={styles.itemTitle}>{t("PM_SAVE_REPORTS")}</h3>
                  <p style={styles.itemSub}>
                    {t("PM_SAVE_REPORTS_DESC")}
                  </p>
                </div>
              </div>
            </div>
          </div>


          <div style={styles.footer}>
            <SubmitBar
              label={t("PROCEED")}
              onSubmit={action}
              style={{
                boxShadow: "none",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                gap: "10px",
                width: "155px",
              }}
              submitIcon={<CustomArrowRight/>}
            />
          </div>
        </div>
      </div>
    </Fragment>,
    root
  );
};

export default IntroModal;
