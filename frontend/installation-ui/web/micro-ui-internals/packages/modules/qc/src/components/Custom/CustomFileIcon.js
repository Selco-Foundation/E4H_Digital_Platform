import React from "react";
import { ImageIcon } from "@egovernments/digit-ui-react-components";
import { PdfIcon, Excel } from "@egovernments/digit-ui-svg-components";
import CustomDocumentIcon from "./CustomDocumentIcon";

const CustomFileIcon = ({ fileName, height, width }) => {
  const ext = fileName?.split(".").pop().toLowerCase();

  if (ext === "pdf") return <PdfIcon height={height} width={width} />;
  if (["png", "jpg", "jpeg"].includes(ext)) return <ImageIcon height={height} width={width} />;
  if (["csv", "xlsx"].includes(ext)) return <Excel height={height} width={width} />;

  return <CustomDocumentIcon height={height} width={width} />;
}

export default CustomFileIcon;