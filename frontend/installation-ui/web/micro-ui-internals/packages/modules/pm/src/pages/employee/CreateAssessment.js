import React, { useEffect, useState } from "react";
import { Loader } from "@egovernments/digit-ui-react-components";
import { useTranslation } from "react-i18next";
import { useDispatch } from "react-redux";
import useProject from "../../hooks/useProject";
import { populateWorkingProject } from "../../redux/actions";

const CreateAssessment = () => {

  const { t } = useTranslation();
  const [mobileView, setMobileView] = useState(window.innerWidth <= 640);
  const [createdProject, setCreatedProject] = useState(null);
  const dispatch = useDispatch();
  const url = window.location.href;
  const projectId = url.split("project/")[1].split("/")[0];

  const { isLoading: projectDataLoading, data: projectData } = useProject({
    id: [projectId],
  });

  useEffect(() => {
    const handleResize = () => setMobileView(window.innerWidth <= 640);
    window.addEventListener("resize", handleResize);

    return () => window.removeEventListener("resize", handleResize);
  }, []);

  useEffect(() => {
    const project = projectData?.projects?.[0];
    if (project) {
      dispatch(populateWorkingProject(project));
      setCreatedProject(project);
    }
  }, [projectData]);

  if (projectDataLoading) {
    return <Loader />;
  }

  return (
    <div style={{ padding: mobileView ? "15px" : "0px" }}>
      {createdProject?.name && (
        <div style={{ fontSize: "40px", fontWeight: "bold", fontFamily: "Roboto Condensed", marginBottom: "20px", color: "#0B0C0C" }}>
          {createdProject?.name}
        </div>
      )}
      <div
        style={{
          border: "1px dashed #D6D5D4",
          borderRadius: "6px",
          padding: "60px 20px",
          textAlign: "center",
          fontSize: "20px",
          fontWeight: "500",
          fontFamily: "Roboto",
          color: "#0B0C0C",
        }}
      >
        {t("PM_CREATE_ASSESSMENT_PLAN_COMING_SOON")}
      </div>
    </div>
  );
};

export default CreateAssessment;
