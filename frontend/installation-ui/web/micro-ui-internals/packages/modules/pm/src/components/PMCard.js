import React, { Fragment, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { AdminPanelSettings } from "@egovernments/digit-ui-svg-components";
import { AddExpense, ListAlt } from "@egovernments/digit-ui-svg-components";
import { Modal, CardText, Button } from "@egovernments/digit-ui-react-components";
import { PersonIcon, PropertyHouse, DocumentIconSolid } from "@egovernments/digit-ui-react-components";

const PMCard = () => {
    const history = useHistory();
    const { t } = useTranslation();
    const { info } = Digit.UserService.getUser();
    const currentUserRoles = info?.roles?.map(role => role.code);
    const [showModal, setShowModal] = useState(false);

    if(!currentUserRoles?.includes("PROJECT_MANAGER")) {
        return null;
    }

    const userType = "employee";

    const handleCreateProjectRedirection = () => {
        setShowModal(true);
    };

    const handleViewProjectRedirection = () => {
        console.debug("clicked 2");
    };

    const handleProceedToCreate = () => {
        setShowModal(false);
        history.push(`/${window?.contextPath}/employee/pm/project/create`);
    };

    const handleCloseModal = () => {
        setShowModal(false);
    };

    return (
        <Fragment>
            <div
                style={{
                    marginLeft: "0px",
                    margin: userType === "citizen" ? "8px" : "0px",
                    gap: userType === "citizen" ? "" : "0 24px",
                    boxShadow: "1px 1px 4px 0px rgba(0,0,0,0.2)",
                    backgroundColor: "white",
                    borderRadius: "4px",
                    width: "320px",
                    maxWidth: "95%",
                    minHeight: "297px",
                    position: "relative",
                    padding: "24px",
                }}
            >
                <div
                    style={{
                        marginBottom: "20px",
                        padding: "8px 0px 25px 0px",
                        display: "flex",
                        gap: "10px",
                        alignItems: "center",
                        lineHeight: "35px",
                        borderBottom: "1px solid #D6D5D4",
                    }}
                >
                    <AdminPanelSettings height={"28px"} width={"28px"} />
                    <div
                        style={{
                            fontFamily: "Roboto",
                            fontWeight: "700",
                            fontSize: "24px",
                            lineHeight: "100%",
                            letterSpacing: "0px",
                            color: "#0B4B66",
                            width: "70%",
                        }}
                    >
                        {t("CS_COMMON_PROJECTS")}
                    </div>
                </div>
                <div>
                    <div
                        style={{
                            display: "flex",
                            gap: "8px",
                            alignItems: "center",
                            color: "#C84C0E",
                            cursor: "pointer",
                            marginBottom: "8px",
                            fontSize: "16px",
                            fontWeight: "bold",
                            fontFamily: "Roboto",
                        }}
                        onClick={handleCreateProjectRedirection}
                    >
                        <AddExpense />
                        <span>
              {t("QC_ACTION_CREATE_PROJECT")}
            </span>
                    </div>
                    <div
                        style={{
                            display: "flex",
                            gap: "8px",
                            alignItems: "center",
                            color: "#C84C0E",
                            cursor: "pointer",
                            fontSize: "16px",
                            fontWeight: "bold",
                            fontFamily: "Roboto",
                        }}
                        onClick={handleViewProjectRedirection}
                    >
                        <ListAlt />
                        <span>
              {t("QC_ACTION_VIEW_PROJECTS")}
            </span>
                    </div>
                </div>
            </div>

            {/* Modal for Create Project Requirements */}
            {showModal && (
                <Modal
                    headerBarMain={
                        <div style={{
                            fontSize: "24px",
                            fontWeight: "700",
                            color: "#0B4B66",
                            padding: "0 16px 0 0",
                            lineHeight: "1.2"
                        }}>
                            Before Creating a Project
                        </div>
                    }
                    headerBarEnd={
                        <button
                            onClick={handleCloseModal}
                            style={{
                                background: "transparent",
                                border: "none",
                                fontSize: "24px",
                                cursor: "pointer",
                                color: "#666",
                                width: "32px",
                                height: "32px",
                                display: "flex",
                                alignItems: "center",
                                justifyContent: "center",
                                borderRadius: "4px",
                                padding: "0",
                                marginLeft: "auto"
                            }}
                            onMouseOver={(e) => e.target.style.background = "#F5F5F5"}
                            onMouseOut={(e) => e.target.style.background = "transparent"}
                        >
                            ×
                        </button>
                    }
                    popupStyles={{
                        width: "800px",
                        maxWidth: "90vw",
                        maxHeight: "90vh",
                        overflow: "auto",
                        borderRadius: "8px",
                        padding: "0",
                        position: "relative"
                    }}
                    popupModuleMianStyles={{
                        padding: "32px"
                    }}
                    actionCancelLabel={null}
                    actionSaveLabel="Proceed →"
                    actionSaveOnSubmit={handleProceedToCreate}
                    formId="create-project-modal"
                    actionBarStyle={{
                        padding: "24px 0 0 0",
                        borderTop: "1px solid #E0E0E0",
                        marginTop: "24px",
                        display: "flex",
                        justifyContent: "flex-end",
                        gap: "16px"
                    }}
                    style={{
                        backgroundColor: "#C84C0E",
                        minHeight: "45px",
                        height: "auto",
                        width: "120px",
                        border: "none",
                        borderRadius: "4px",
                        fontWeight: "700",
                        fontSize: "16px",
                        padding: "12px 24px",
                        color: "white"
                    }}
                    hideSubmit={false}
                    overlayStyle={{
                        backgroundColor: "rgba(255, 255, 255, 0.8)", // Lighter overlay
                        zIndex: 1000,
                        position: "fixed",
                        top: 0,
                        left: 0,
                        right: 0,
                        bottom: 0
                    }}
                >
                    <div style={{ marginBottom: "32px" }}>
                        <p style={{
                            margin: "0 0 16px 0",
                            fontSize: "16px",
                            color: "#0B0C0C",
                            fontWeight: "400",
                            lineHeight: "24px"
                        }}>
                            You will be able to create a project on <strong style={{fontWeight: "600"}}>'xxxxxx'</strong>
                        </p>
                        <CardText style={{
                            margin: "0",
                            fontSize: "16px",
                            color: "#0B0C0C",
                            lineHeight: "24px",
                            fontWeight: "400"
                        }}>
                            Before getting started, you will need the following details to successfully create a project:
                        </CardText>
                    </div>

                    {/* Three Requirements in Horizontal Layout */}
                    <div style={{
                        display: "flex",
                        justifyContent: "space-between",
                        alignItems: "flex-start",
                        gap: "24px",
                        marginBottom: "8px"
                    }}>
                        {/* Team Members */}
                        <div style={{
                            flex: 1,
                            textAlign: "center",
                            position: "relative",
                            padding: "0 16px"
                        }}>
                            <div style={{
                                width: "80px",
                                height: "80px",
                                borderRadius: "50%",
                                margin: "0 auto 16px auto",
                                display: "flex",
                                alignItems: "center",
                                justifyContent: "center",
                                background: "#F5F5F5",
                                border: "2px solid #E0E0E0"
                            }}>
                                <PersonIcon style={{ width: "40px", height: "40px", color: "#0B4B66" }} />
                            </div>
                            <h3 style={{
                                margin: "0 0 8px 0",
                                fontSize: "18px",
                                fontWeight: "600",
                                color: "#0B4B66",
                                lineHeight: "24px"
                            }}>
                                Details of Team Members
                            </h3>
                            <p style={{
                                margin: 0,
                                fontSize: "14px",
                                color: "#666",
                                lineHeight: "20px",
                                fontWeight: "400"
                            }}>
                                Name, mobile number, role
                            </p>

                            <div style={{
                                position: "absolute",
                                top: "20px",
                                right: "0",
                                bottom: "20px",
                                width: "1px",
                                background: "#E0E0E0",
                                display: "block"
                            }} />
                        </div>

                        {/* Health Facilities */}
                        <div style={{
                            flex: 1,
                            textAlign: "center",
                            position: "relative",
                            padding: "0 16px"
                        }}>
                            <div style={{
                                width: "80px",
                                height: "80px",
                                borderRadius: "50%",
                                margin: "0 auto 16px auto",
                                display: "flex",
                                alignItems: "center",
                                justifyContent: "center",
                                background: "#F5F5F5",
                                border: "2px solid #E0E0E0"
                            }}>
                                <PropertyHouse style={{ width: "40px", height: "40px", color: "#0B4B66" }} />
                            </div>
                            <h3 style={{
                                margin: "0 0 8px 0",
                                fontSize: "18px",
                                fontWeight: "600",
                                color: "#0B4B66",
                                lineHeight: "24px"
                            }}>
                                List of Health Facilities
                            </h3>
                            <p style={{
                                margin: 0,
                                fontSize: "14px",
                                color: "#666",
                                lineHeight: "20px",
                                fontWeight: "400"
                            }}>
                                Create installation reports for the health facilities assigned to you (online and offline)
                            </p>

                            <div style={{
                                position: "absolute",
                                top: "20px",
                                right: "0",
                                bottom: "20px",
                                width: "1px",
                                background: "#E0E0E0",
                                display: "block"
                            }} />
                        </div>

                        {/* Save Reports */}
                        <div style={{
                            flex: 1,
                            textAlign: "center",
                            position: "relative",
                            padding: "0 16px"
                        }}>
                            <div style={{
                                width: "80px",
                                height: "80px",
                                borderRadius: "50%",
                                margin: "0 auto 16px auto",
                                display: "flex",
                                alignItems: "center",
                                justifyContent: "center",
                                background: "#F5F5F5",
                                border: "2px solid #E0E0E0"
                            }}>
                                <DocumentIconSolid style={{ width: "40px", height: "40px", color: "#0B4B66" }} />
                            </div>
                            <h3 style={{
                                margin: "0 0 8px 0",
                                fontSize: "18px",
                                fontWeight: "600",
                                color: "#0B4B66",
                                lineHeight: "24px"
                            }}>
                                Save Reports
                            </h3>
                            <p style={{
                                margin: 0,
                                fontSize: "14px",
                                color: "#666",
                                lineHeight: "20px",
                                fontWeight: "400"
                            }}>
                                Save installation reports offline until ready for submission
                            </p>
                        </div>
                    </div>
                </Modal>
            )}
        </Fragment>
    );
};

export default PMCard;