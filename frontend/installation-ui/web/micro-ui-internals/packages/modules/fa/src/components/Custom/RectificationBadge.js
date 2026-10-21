import React from "react";
import { useTranslation } from "react-i18next";

// Identical copies live in pm, qc and fa (components/Custom/RectificationBadge.js) — keep them in sync.

const TRUTHY_FLAG_VALUES = ["true", "yes"];

const isTruthyFlag = (value) => value === true || TRUTHY_FLAG_VALUES.includes(value?.toString?.().trim().toLowerCase());

// Accepts both shapes the flag arrives in:
//   activity facility additionalDetails  -> { isRectificationSite: true, systemType, ... }
//   field plan facility additionalDetails -> { fields: [{ key: "isRectificationSite", value: true }] }
export const isRectificationSite = (additionalDetails) => {
  if (!additionalDetails) {
    return false;
  }

  if (Object.prototype.hasOwnProperty.call(additionalDetails, "isRectificationSite")) {
    return isTruthyFlag(additionalDetails.isRectificationSite);
  }

  const field = additionalDetails.fields?.find?.((item) => item?.key === "isRectificationSite");
  return isTruthyFlag(field?.value);
};

export const RECTIFICATION_COLORS = {
  badgeBackground: "#F3EFFA",
  badgeBorder: "#D8CCF0",
  badgeText: "#5B3A8E",
  rowBackground: "#FAF8FD",
  headerBorder: "#ECE5F7",
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
    stroke={RECTIFICATION_COLORS.badgeText}
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
        border: `1px solid ${RECTIFICATION_COLORS.badgeBorder}`,
        borderRadius: "4px",
        backgroundColor: RECTIFICATION_COLORS.badgeBackground,
        color: RECTIFICATION_COLORS.badgeText,
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
export const RectificationCardBadge = ({ show }) => {
  if (!show) {
    return null;
  }

  return <RectificationBadge style={{ position: "absolute", top: "16px", right: "16px" }} />;
};

// Row tint for DIGIT <Table>: spread into the object returned from getCellProps(cell).
export const getRectificationCellStyle = (cell) => (
  cell?.row?.original?.isRectificationSite ? { backgroundColor: RECTIFICATION_COLORS.rowBackground } : {}
);

// Tinted treatment for a facility InfoCard; spread over its base style. `position: relative` anchors RectificationCardBadge.
export const getRectificationCardStyle = (show) => (
  show
    ? { position: "relative", background: RECTIFICATION_COLORS.rowBackground, border: `1px solid ${RECTIFICATION_COLORS.badgeBorder}` }
    : {}
);

export default RectificationBadge;
