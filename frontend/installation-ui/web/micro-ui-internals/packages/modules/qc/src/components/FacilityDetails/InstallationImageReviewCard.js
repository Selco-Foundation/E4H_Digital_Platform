import React from "react";
import Summary from "./Summary";

const getInstallationImageSection = (installationImage) => {
  return `INSTALLATION_IMAGE_${installationImage.code || installationImage.imageCode}`.toUpperCase();
};

const InstallationImageReviewCard = ({ t, installationImage, index }) => {
  const section = getInstallationImageSection(installationImage);

  return (
    <Summary
      t={t}
      sectionName={`InstallationImage${index + 1}`}
      section={section}
      customTitle={installationImage.description}
      titleFontSize="26px"
      renderContent={({ setImageToView }) => (
        <div style={{ padding: "20px" }}>
          <div style={{ display: "flex", gap: "10px", flexWrap: "wrap" }}>
            {installationImage.images.map((image, idx) => (
              <div key={idx} style={{ cursor: "pointer" }} onClick={() => setImageToView(image.fileUrl)}>
                <img src={image.fileUrl} alt={`Installation Image ${index + 1} - ${idx + 1}`} style={{ width: "100px", marginTop: "8px" }} />
              </div>
            ))}
          </div>
        </div>
      )}
    />
  );
};

export default InstallationImageReviewCard;
