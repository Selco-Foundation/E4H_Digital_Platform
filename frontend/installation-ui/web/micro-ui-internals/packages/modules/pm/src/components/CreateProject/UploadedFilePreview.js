import React, { useEffect, useState } from "react";
import CustomFileIcon from "./CustomFileIcon";
import CustomUploadIcon from "../Custom/CustomUploadIcon";
import CustomDownloadIcon from "../Custom/CustomDownloadIcon";
import CustomCloseSvg from "../Custom/CustomCloseSvg";

const UploadedFilePreview = ({ t, file, onReupload, onRemove }) => {

  const [mobileView, setMobileView] = useState(window.innerWidth <= 780);

  useEffect(() => {
    const handleResize = () => setMobileView(window.innerWidth <= 780);
    window.addEventListener("resize", handleResize);

    return () => window.removeEventListener("resize", handleResize);
  }, []);

  const handleDownload = () => {
    const url = URL.createObjectURL(file);
    const link = document.createElement("a");
    link.href = url;
    link.download = file.name;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  };

  return (
    <div
      style={{
        display: "flex",
        alignItems: "center",
        justifyContent: "space-between",
        border: "1px solid #ddd",
        borderRadius: "4px",
        padding: "10px 40px 10px 24px",
        marginTop: "16px",
        backgroundColor: "#fafafa",
        fontFamily: "Arial, sans-serif",
        position: "relative",
        flexDirection: mobileView ? "column" : "row",
        gap: mobileView ? "15px" : "0px"
      }}
    >
      <div style={{ display: "flex", alignItems: "center", gap: "12px" }}>
        {<CustomFileIcon file={file} width={"30px"} height={"30px"} />}
        <span
          style={{
            fontSize: "16px",
            color: "#787878",
            fontWeight: "bold",
          }}
        >
          {file.name}
        </span>
      </div>

      <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
        <button
          type={"button"}
          onClick={onReupload}
          style={{
            border: "1px solid #C84C0E",
            padding: "6px 12px",
            borderRadius: "4px",
            backgroundColor: "transparent",
            cursor: "pointer",
            display: "flex",
            alignItems: "center",
          }}
        >
          <CustomUploadIcon
            fill={"#C84C0E"}
            height={"20px"}
            width={"20px"}
            styles={{marginRight: "5px"}}
          />
          <span style={{ color: "#C84C0E", fontSize: "16px", fontWeight: "500", fontFamily: "Roboto" }}>
            {t("CORE_COMMON_RE_UPLOAD")}
          </span>
        </button>
        <button
          type={"button"}
          onClick={handleDownload}
          style={{
            border: "1px solid #c44d2d",
            color: "#c44d2d",
            padding: "6px 12px",
            borderRadius: "4px",
            backgroundColor: "transparent",
            cursor: "pointer",
            display: "flex",
            alignItems: "center",
          }}
        >
          <CustomDownloadIcon
            fill={"#C84C0E"}
            height={"15px"}
            width={"15px"}
            styles={{marginRight: "5px"}}
          />
          <span style={{ color: "#C84C0E", fontSize: "16px", fontWeight: "500", fontFamily: "Roboto" }}>
            {t("CORE_COMMON_DOWNLOAD")}
          </span>
        </button>
      </div>
      <button
        style={{
          cursor: "pointer",
          position: "absolute",
          top: "0",
          right: "0",
          height: "24px",
          width: "24px",
          color: "white",
          display: "flex",
          border: "1px solid #D6D5D4",
          alignItems: "center",
          padding: "0"
        }}
        onClick={(e) => {
          e.stopPropagation();
          onRemove();
        }}
      >
        <CustomCloseSvg height={"24"} width={"24"} fill="#EEEEEE" iconFill={"#0B4B66"}/>
      </button>
    </div>
  );
};

export default UploadedFilePreview;
