import React, { useState, Fragment } from "react";
import { useTranslation } from "react-i18next";
import { AdminPanelSettings, AddExpense, ListAlt } from "@egovernments/digit-ui-svg-components";
import { PopUp, CardText, Button } from "@egovernments/digit-ui-components";

const PMCard = () => {
    const { t } = useTranslation();
    const [open, setOpen] = useState(false);

    const RequirementItem = ({ icon, title, desc, showDivider }) => (
        <div style={{
            flex: "1 1 30%",
            textAlign: "center",
            position: "relative",
            padding: "16px 12px",
            minWidth: "0",
            display: "flex",
            flexDirection: "column",
            alignItems: "center"
        }}>
            <div style={{
                width: "50px",
                height: "50px",
                borderRadius: "50%",
                margin: "0 auto 12px auto",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                background: "#F3F6F9",
                fontSize: "20px",
                flexShrink: 0
            }}>
                {icon}
            </div>
            <h3 style={{
                margin: "0 0 8px 0",
                fontSize: "16px",
                fontWeight: "600",
                color: "#0B4B66",
                lineHeight: "20px"
            }}>
                {title}
            </h3>
            <p style={{
                margin: 0,
                fontSize: "14px",
                color: "#666",
                lineHeight: "18px"
            }}>
                {desc}
            </p>

            {showDivider && (
                <div style={{
                    position: "absolute",
                    top: "25%",
                    right: "0",
                    height: "50%",
                    width: "1px",
                    background: "#E0E0E0"
                }} />
            )}
        </div>
    );

    return (
        <Fragment>
            {/* Card that launches popup */}
            <div style={{ width: 350, background: "#fff", borderRadius: 4, boxShadow: "1px 1px 4px rgba(0,0,0,.2)" }}>
                <section style={{ padding: 16 }}>
                    <div style={{
                        display: "flex", alignItems: "center", gap: 10, padding: 8, paddingLeft: 0,
                        borderBottom: "1px solid #D6D5D4", marginBottom: 10
                    }}>
                        <AdminPanelSettings width="28" height="28" />
                        <div style={{ font: "700 24px/1 Roboto", color: "#0B4B66" }}>{t("CS_COMMON_PROJECTS")}</div>
                    </div>

                    <div style={{ paddingLeft: 0 }}>
                        <div
                            onClick={() => setOpen(true)}
                            style={{ display: "flex", gap: 8, alignItems: "center", color: "#C84C0E", cursor: "pointer",
                                marginBottom: 8, font: "bold 16px Roboto" }}
                        >
                            <AddExpense /><span>{t("QC_ACTION_CREATE_PROJECT")}</span>
                        </div>
                        <div
                            onClick={() => console.debug("clicked 2")}
                            style={{ display: "flex", gap: 8, alignItems: "center", color: "#C84C0E", cursor: "pointer",
                                font: "bold 16px Roboto" }}
                        >
                            <ListAlt /><span>{t("QC_ACTION_VIEW_PROJECTS")}</span>
                        </div>
                    </div>
                </section>
            </div>

            {open && (
                <PopUp
                    className="digit-create-project-popup"
                    onClose={() => setOpen(false)}
                    style={{
                        width: "min(800px, 90vw)",
                        height: "auto",
                        minHeight: "520px",
                        maxHeight: "85vh",
                        background: "#FFFFFF",
                        borderRadius: 8,
                        padding: "28px",
                        display: "flex",
                        flexDirection: "column"
                    }}
                >
                    <h2
                        id="create-project-title"
                        style={{
                            margin: "0 0 20px 0",
                            fontSize: "22px",
                            fontWeight: "700",
                            color: "#0B4B66"
                        }}
                    >
                        Before Creating a Project
                    </h2>
                    <p style={{ margin: "0 0 10px 0", fontSize: "16px", color: "#0B0C0C" }}>
                        You will be able to create a project on <strong>'xxxxxx'</strong>
                    </p>
                    <p style={{ margin: "0 0 30px 0", fontSize: "16px", color: "#0B0C0C" }}>
                        Before getting started, you will need the following details to successfully create a project:
                    </p>

                    <div style={{
                        display: "flex",
                        flex: "1",
                        flexWrap: "wrap",
                        justifyContent: "space-between",
                        alignItems: "stretch",
                        marginBottom: "24px",
                        gap: "12px",
                        minHeight: "0",
                    }}>
                        <RequirementItem
                            icon="👥"
                            title="Details of Team Members"
                            desc="Name, mobile number, role"
                            showDivider={true}
                        />
                        <RequirementItem
                            icon="🏥"
                            title="List of Health Facilities"
                            desc="Create installation reports for the health facilities assigned to you (online and offline)"
                            showDivider={true}
                        />
                        <RequirementItem
                            icon="📄"
                            title="Save Reports"
                            desc="Save installation reports offline until ready for submission"
                            showDivider={false}
                        />
                    </div>

                    <div style={{
                        display: "flex",
                        justifyContent: "flex-end",
                        marginTop: "auto",
                        paddingTop: "20px"
                    }}>
                        <Button type="button" label="Proceed →" onClick={() => setOpen(false)} />
                    </div>
                </PopUp>
            )}

            <style>
                {`
                    /* Ensure the PopUp component uses our styles */
                    .digit-create-project-popup .PopUp-module_content__2x1kL {
                        width: min(800px, 90vw) !important;
                        height: auto !important;
                        min-height: 520px !important;
                        max-height: 85vh !important;
                        background: #FFFFFF !important;
                        border-radius: 8px !important;
                        padding: 28px !important;
                        display: flex !important;
                        flex-direction: column !important;
                    }
                    
                    /* Hide any duplicate close buttons */
                    .digit-create-project-popup button[aria-label="Close"] {
                        display: none !important;
                    }
                    
                    /* Responsive adjustments */
                    @media (max-width: 768px) {
                        .digit-create-project-popup .PopUp-module_content__2x1kL {
                            width: 95vw !important;
                            padding: 20px !important;
                            min-height: 480px !important;
                        }
                        
                        .digit-create-project-popup h2 {
                            font-size: 20px !important;
                        }
                        
                        .digit-create-project-popup p {
                            font-size: 15px !important;
                        }
                    }
                    
                    @media (max-width: 600px) {
                        .digit-create-project-popup .PopUp-module_content__2x1kL {
                            min-height: 560px !important;
                        }
                        
                        /* Stack items vertically on small screens */
                        .digit-create-project-popup .requirement-item {
                            flex: 1 1 100% !important;
                            margin-bottom: 20px;
                            padding: 12px 8px !important;
                            min-width: 0 !important;
                        }
                        
                        .digit-create-project-popup .requirement-item:not(:last-child) {
                            border-bottom: 1px solid #E0E0E0;
                            padding-bottom: 20px;
                        }
                        
                        .digit-create-project-popup .divider {
                            display: none !important;
                        }
                    }
                `}
            </style>
        </Fragment>
    );
};

export default PMCard;
