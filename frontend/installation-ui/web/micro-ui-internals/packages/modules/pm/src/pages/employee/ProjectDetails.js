import React, { useEffect, useState } from "react";
import { EditIcon, Loader } from "@egovernments/digit-ui-react-components";
import { useHistory } from "react-router-dom";
import useProject from "../../hooks/useProject";
import { populateWorkingProject } from "../../redux/actions";
import { useDispatch } from "react-redux";
import { useTranslation } from "react-i18next";
import CustomFileIcon from "../../components/File/CustomFileIcon";

const ProjectDetails = () => {

  const { t } = useTranslation();
  const history = useHistory();
  const url = window.location.href;
  const projectId = url.split("project/")[1].split("/")[0];
  const [createdProject, setCreatedProject] = useState(null);
  const [file, setFile] = useState(null);
  const dispatch = useDispatch();
  const [mobileView, setMobileView] = useState(window.innerWidth <= 640);

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
      if (project.status) {
        setFile({
          name: `${project.name}.xlsx`,
        });
      }
      dispatch(populateWorkingProject(project));
      setCreatedProject(project);
    }
  }, [projectData])

  const PropertyCard = (infoName, infoValue) => (
    <div style={{ width: "500px", maxWidth: "100%", display: "flex", gap: "20px", alignItems: "center", marginBottom: "15px" }}>
      <div style={{ fontFamily: "Roboto", fontWeight: 700, width: "40%", fontSize: "16px", lineHeight: "100%", color: "#0B0C0C" }}>
        {infoName}
      </div>
      <div style={{ fontFamily: "Roboto", fontWeight: 400, fontSize: "14px", lineHeight: "137%", color: "#0B0C0C" }}>
        {infoValue}
      </div>
    </div>
  )

  const formatDate = (timestamp) => {
    const date = new Date(timestamp);
    const month = date.toLocaleString("en-US", { month: "long" });
    const day = String(date.getDate()).padStart(2, "0");
    const year = date.getFullYear();
    return `${day} ${month} ${year}`;
  };

  const handleEditProjectNavigation = (key) => {
    history.push(`/${window.contextPath}/employee/pm/project/create?projectId=${createdProject.id}&key=${key}`);
  }

  if (projectDataLoading) {
    return <Loader />
  }

  return (
    <div style={{padding: mobileView ? "15px" : "0px"}}>
      <div
        style={{
          backgroundColor: "#FFFFFF",
          borderRadius: "5px",
          boxShadow: "0px 0px 4px rgba(0, 0, 0, 0.25)",
          padding: "20px",
          marginBottom: "20px",
        }}
      >
        <div
          style={{
            display: "flex",
            justifyContent: "space-between",
            marginBottom: "20px",
          }}
        >
          <h2
            style={{
              fontSize: "24px",
              fontFamily: "Roboto",
              fontWeight: 700,
              color: "#0B4B66",
              margin: "0",
            }}
          >
            {t("PM_PROJECT_PROJECT_DETAILS")}
          </h2>
          <button
            type="button"
            style={{
              padding: "0px",
              backgroundColor: "white",
            }}
            onClick={() => handleEditProjectNavigation(1)}
          >
            <EditIcon />
          </button>
        </div>
        <div>
          {PropertyCard(t("PM_PROJECT_INFO_TYPE_OF_PROJECT"), createdProject?.projectType)}
          {PropertyCard(t("PM_PROJECT_INFO_PROJECT_DATES"), `${formatDate(createdProject?.startDate)} - ${formatDate(createdProject?.endDate)}`)}
          {PropertyCard(t("PM_PROJECT_INFO_JUSTIFICATION_CODE"), createdProject?.additionalDetails?.justificationCode)}
        </div>
      </div>

      <div
        style={{
          backgroundColor: "#FFFFFF",
          borderRadius: "5px",
          boxShadow: "0px 0px 4px rgba(0, 0, 0, 0.25)",
          padding: "20px",
          marginBottom: "20px",
        }}
      >
        <div
          style={{
            display: "flex",
            justifyContent: "space-between",
            marginBottom: "20px",
          }}
        >
          <h2
            style={{
              fontSize: "24px",
              fontFamily: "Roboto",
              fontWeight: 700,
              color: "#0B4B66",
              margin: "0",
            }}
          >
            {t("PM_PROJECT_INFO_GEOGRAPHY_DETAILS")}
          </h2>
          <button
            type="button"
            style={{
              padding: "0px",
              backgroundColor: "white",
            }}
            onClick={() => handleEditProjectNavigation(2)}
          >
            <EditIcon />
          </button>
        </div>
        <div>
          {PropertyCard(t("PM_PROJECT_INFO_STATE"), t(`STATE_${createdProject?.additionalDetails?.geographyDetails?.state?.code?.toUpperCase()}`))}
          {PropertyCard(
            t("PM_PROJECT_INFO_DISTRICTS"),
            <span style={{ color: "#C84C0E", textDecoration: "underline" }}>
              {`${createdProject?.additionalDetails?.geographyDetails?.districts?.length} ${t("CORE_COMMON_SELECTED")}`}
            </span>
          )}
          {PropertyCard(
            t("PM_PROJECT_INFO_BLOCKS"),
            <span style={{ color: "#C84C0E", textDecoration: "underline" }}>
              {`${createdProject?.additionalDetails?.geographyDetails?.blocks?.length} ${t("CORE_COMMON_SELECTED")}`}
            </span>
          )}
        </div>
      </div>
      {file && (
        <div
          style={{
            backgroundColor: "#FFFFFF",
            borderRadius: "5px",
            boxShadow: "0px 0px 4px rgba(0, 0, 0, 0.25)",
            padding: "20px",
          }}
        >
          <div
            style={{
              display: "flex",
              justifyContent: "space-between",
              marginBottom: "20px",
            }}
          >
            <h2
              style={{
                fontSize: "24px",
                fontFamily: "Roboto",
                fontWeight: 700,
                color: "#0B4B66",
                margin: "0",
              }}
            >
              {t("PM_PROJECT_INFO_FACILITY_DATA")}
            </h2>
            <button
              type="button"
              style={{
                padding: "0px",
                backgroundColor: "white",
              }}
              onClick={() => handleEditProjectNavigation(3)}
            >
              <EditIcon />
            </button>
          </div>
          <div>
            <div
              style={{
                display: "flex",
                alignItems: "center",
                backgroundColor: "#EEEEEE",
                border: "1px solid #C5C5C5",
                borderRadius: "5px",
                width: "295px",
                height: "75px",
                padding: "20px 20px",
                gap: "10px",
              }}
            >
              {<CustomFileIcon file={file} width={"30px"} height={"30px"} />}
              <div>
                <div style={{ fontWeight: "bold", fontSize: "16px" }}>
                  {file.name}
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ProjectDetails;
