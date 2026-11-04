import React from "react";
import { useTranslation } from "react-i18next";

// Identical copies live in pm, qc and fa (components/Custom/RectificationBadge.js) — keep them in sync.

// Same truthy values field-planner/ingestion accept for rectificationSite.
const TRUTHY_FLAG_VALUES = ["true", "yes", "1"];

const isTruthyFlag = (value) => value === true || TRUTHY_FLAG_VALUES.includes(value?.toString?.().trim().toLowerCase());

// Accepts both shapes the backend sends `rectificationSite` in:
//   activity facility additionalDetails  -> { rectificationSite: true, systemType, ... } (set at scheduling)
//   field plan facility additionalDetails -> { fields: [{ key: "rectificationSite", value: "Yes" }] }
export const isRectificationSite = (additionalDetails) => {
  if (!additionalDetails) {
    return false;
  }

  if (Object.prototype.hasOwnProperty.call(additionalDetails, "rectificationSite")) {
    return isTruthyFlag(additionalDetails.rectificationSite);
  }

  const field = additionalDetails.fields?.find?.((item) => item?.key === "rectificationSite");
  return isTruthyFlag(field?.value);
};

// DIGIT palette Info pair (alertInfo / alertInfoBg). Rectification is extra information, not a status.
export const RECTIFICATION_COLORS = {
  info: "#0057BD",
  infoBackground: "#DEEFFF",
  cardBadgeBackground: "#FFFFFF",
};

// "compact" sits inline with 15px table text without growing the row; "default" is for cards.
const BADGE_SIZES = {
  default: { height: "22px", padding: "2px 8px 2px 6px", fontSize: "12px", lineHeight: "16px", iconSize: 12 },
  compact: { height: "20px", padding: "1px 6px 1px 5px", fontSize: "11px", lineHeight: "16px", iconSize: 11 },
};

const WrenchIcon = ({ size = 12 }) => (
  <svg
    width={size}
    height={size}
    viewBox="0 0 24 24"
    fill="none"
    stroke={RECTIFICATION_COLORS.info}
    strokeWidth="2.2"
    strokeLinecap="round"
    strokeLinejoin="round"
    aria-hidden="true"
    style={{ flexShrink: 0 }}
  >
    <path d="M14.7 6.3a4 4 0 0 0-5.4 5.4L3 18l3 3 6.3-6.3a4 4 0 0 0 5.4-5.4l-2.5 2.5-2.4-.6-.6-2.4z" />
  </svg>
);

const RectificationBadge = ({ size = "default", style = {}, className = "" }) => {
  const { t } = useTranslation();
  const dimensions = BADGE_SIZES[size] || BADGE_SIZES.default;

  return (
    <span
      className={`rectification-badge ${className}`.trim()}
      style={{
        display: "inline-flex",
        alignItems: "center",
        gap: "4px",
        height: dimensions.height,
        boxSizing: "border-box",
        padding: dimensions.padding,
        border: `1px solid ${RECTIFICATION_COLORS.info}`,
        borderRadius: "4px",
        backgroundColor: RECTIFICATION_COLORS.infoBackground,
        color: RECTIFICATION_COLORS.info,
        fontSize: dimensions.fontSize,
        lineHeight: dimensions.lineHeight,
        fontWeight: 500,
        letterSpacing: "0.2px",
        whiteSpace: "nowrap",
        width: "fit-content",
        flexShrink: 0,
        ...style,
      }}
    >
      <WrenchIcon size={dimensions.iconSize} />
      {t("COMMON_LABEL_RECTIFICATION")}
    </span>
  );
};

// Table cells: places a compact badge on the same line after `children`, so the row keeps its height.
// Rows are not tinted: an alertInfoBg row would drop the orange facility links below AA contrast.
export const WithRectificationBadge = ({ show, children }) => {
  if (!show) {
    return children;
  }

  return (
    <div style={{ display: "flex", alignItems: "center", gap: "8px", flexWrap: "nowrap" }}>
      {children}
      <RectificationBadge size="compact" />
    </div>
  );
};

// Detail screens: pins the badge to the top-right corner of a `position: relative` card.
// White fill so it stays visible on the alertInfoBg card.
export const RectificationCardBadge = ({ show }) => {
  if (!show) {
    return null;
  }

  return (
    <RectificationBadge
      style={{ position: "absolute", top: "16px", right: "16px", backgroundColor: RECTIFICATION_COLORS.cardBadgeBackground }}
    />
  );
};

// Info treatment for a facility InfoCard; spread over its base style. `position: relative` anchors RectificationCardBadge.
export const getRectificationCardStyle = (show) => (
  show
    ? { position: "relative", background: RECTIFICATION_COLORS.infoBackground, border: `1px solid ${RECTIFICATION_COLORS.info}` }
    : {}
);

export default RectificationBadge;
