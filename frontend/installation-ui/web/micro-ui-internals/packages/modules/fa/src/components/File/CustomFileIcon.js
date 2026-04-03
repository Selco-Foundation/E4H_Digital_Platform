import React from "react";
import { DocumentIcon, ImageIcon } from "@egovernments/digit-ui-react-components";
import { PdfIcon, Excel } from "@egovernments/digit-ui-svg-components";

const CustomFileIcon = ({ file, height, width }) => {
  const ext = file?.name?.split(".").pop().toLowerCase();

  if (ext === "pdf") return <PdfIcon height={height} width={width} />;
  if (["png", "jpg", "jpeg"].includes(ext)) return <ImageIcon height={height} width={width} />;
  if (["csv", "xlsx"].includes(ext)) return <Excel height={height} width={width} />;

  return <DocumentIcon height={height} width={width} />;
}

export default CustomFileIcon;