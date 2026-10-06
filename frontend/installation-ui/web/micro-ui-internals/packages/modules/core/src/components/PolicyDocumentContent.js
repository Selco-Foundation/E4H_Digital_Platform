import React from "react";
import { useTranslation } from "react-i18next";
import { Button } from "@egovernments/digit-ui-components";

const PolicyDocumentContent = ({ documentData }) => {
  const { t } = useTranslation();
  const contents = documentData?.contents || [];

  if (!contents.length) {
    return <div>{t("ES_COMMON_NO_DATA")}</div>;
  }

  const handleScrollToElement = (id) => {
    const element = document.getElementById(id);
    if (element) {
      element.scrollIntoView({ behavior: "smooth" });
    }
  };

  return (
    <React.Fragment>
      <div>
        <div className="privacy-table">{t("DIGIT_TABLE_OF_CONTENTS")}</div>
        <ul>
          {contents.map((content, index) => (
            <li key={index} style={{ display: "flex", alignItems: "center" }}>
              <span style={{ marginRight: "0.5rem" }}>{index + 1}. </span>
              <Button
                label={t(content.header)}
                variation={"link"}
                size={"medium"}
                onClick={(e) => {
                  e.preventDefault();
                  handleScrollToElement(content?.header);
                }}
                style={{ justifyContent: "flex-start" }}
              ></Button>
            </li>
          ))}
        </ul>
      </div>
      {contents.map((content, index) => (
        <div key={index} id={content?.header}>
          <div
            style={{
              fontWeight: "bold",
              paddingLeft: content?.isSpaceRequired ? "1rem" : "0",
            }}
          >
            {t(content.header)}
          </div>
          {(content.descriptions || []).map((description, subIndex) => (
            <div key={subIndex} style={{ paddingLeft: description.isSpaceRequired ? "1rem" : "0", marginBottom: "0.5rem" }}>
              <div
                style={{
                  fontWeight: description?.isBold ? 700 : 400,
                  display: "flex",
                  alignItems: "center",
                }}
              >
                {description.type === "points" && <span style={{ marginRight: "0.5rem", listStyleType: "disc" }}>&#8226;</span>}
                {description.type === "step" && <span style={{ marginRight: "0.5rem", listStyleType: "decimal" }}>{subIndex + 1}. </span>}
                {t(description.text)}
              </div>
              {description?.subDescriptions && description?.subDescriptions.length > 0 && (
                <div className="policy-subdescription">
                  {description.subDescriptions.map((subDesc, subSubIndex) => (
                    <div key={subSubIndex} className="policy-subdescription-points">
                      {subDesc.type === "points" && (
                        <span style={{ marginRight: "0.5rem", listStyleType: "disc", paddingLeft: "1rem" }}>&#8226;</span>
                      )}
                      {subDesc.type === "step" && (
                        <span style={{ marginRight: "0.5rem", listStyleType: "decimal", paddingLeft: "1rem" }}>{subSubIndex + 1}. </span>
                      )}
                      {subDesc.type === null && <span style={{ marginRight: "0.5rem", paddingLeft: "1rem" }}> </span>}
                      {t(subDesc.text)}
                    </div>
                  ))}
                </div>
              )}
            </div>
          ))}
        </div>
      ))}
    </React.Fragment>
  );
};

export default PolicyDocumentContent;
