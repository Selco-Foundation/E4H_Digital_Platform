import React, { useState, useEffect, useRef } from "react";
import { useTranslation } from "react-i18next";
import { Button, Dropdown, Loader, PopUp, Toast } from "@selco/digit-ui-react-components";
import { useHistory } from "react-router-dom";
import { FormComposer } from "../../components/FormComposer";
import { Link } from "react-router-dom";

export const PauseRMS = () => {
  const { t } = useTranslation();
  const [healthcentre, setHealthCentre] = useState();
  const [districtMenu, setDistrictMenu] = useState([]);
  const [sortedDistrictMenu, setSortedDistrictMenu] = useState([]);
  const [blockOptions, setBlockOptions] = useState([]);
  const [blockMenu, setBlockMenu] = useState([]);
  const [sortedBlockMenu, setSortedBlockMenu] = useState([]);
  const [facilityOptions, setFacilityOptions] = useState([]);
  const [facilityMenu, setFacilityMenu] = useState([]);
  const [sortedFacilityMenu, setSortedFacilityMenu] = useState([]);
  const [district, setDistrict] = useState(null);
  const [block, setBlock] = useState(null);
  const [creationError, setCreationError] = useState(null);
  const [canSubmit, setSubmitValve] = useState(false);
  const tenantId = window.Digit.SessionStorage.get("Employee.tenantId");
  const [duplicateTicketIds, setDuplicateTicketIds] = useState([]);
  const [blockUI, setBlockUI] = useState(false);
  const [selectBoundaryCode, setSelectBoundaryCode] = useState("");
  const jurisdictionCurrentBoundary = Digit.SessionStorage.get("Jurisdiction.CurrentBoundary") || {
    country: ["-"],
  };
  const jurisdictionCurrentBoundaryCodes = Digit.Utils.BoundaryUtil.aggregateBoundaryCodes(jurisdictionCurrentBoundary);
  const [stateBoundaryCode, setStateBoundaryCode] = useState("");
  const [facilityBoundaries, setFacilityBoundaries] = useState([]);
  const [facilityBoundaryCodes, setFacilityBoundaryCodes] = useState(["-"]);

  const { data: boundaryData } = Digit.Hooks.im.useBoundary(jurisdictionCurrentBoundaryCodes);
  const { data: facilityData } = Digit.Hooks.im.useFacility(facilityBoundaryCodes);

  useEffect(() => {
    setSelectBoundaryCode(jurisdictionCurrentBoundaryCodes?.join(","));
    if (boundaryData) {
      setStateBoundaryCode(boundaryData.states?.map((state) => state?.code)?.join(","));
      setDistrictMenu(boundaryData.districts);
      setBlockOptions(boundaryData.blocks);
      setFacilityBoundaries(boundaryData.facilities);
      setFacilityBoundaryCodes(boundaryData.facilities?.map((facility) => facility?.code) || ["-"]);
    }
  }, [boundaryData, t]);

  useEffect(() => {
    if (facilityBoundaries?.length && facilityData?.facilities?.length) {
      const facilityBoundaryCodeToParentMap = new Map();
      for (let facilityBoundary of facilityBoundaries) {
        facilityBoundaryCodeToParentMap.set(facilityBoundary.code, facilityBoundary.parentCode);
      }
      setFacilityOptions(
        facilityData?.facilities?.map((facility) => ({
          code: facility.boundaryCode,
          id: facility.facilityId,
          status: facility.facilityStatus,
          parentCode: facilityBoundaryCodeToParentMap.get(facility.boundaryCode),
        }))
      );
    }
  }, [facilityBoundaries, facilityData]);

  useEffect(() => {
    if (district?.code && blockOptions?.length) {
      const newBlocksMenu = blockOptions?.filter((blockOption) => blockOption?.parentCode === district.code);
      setBlockMenu(newBlocksMenu);
    } else {
      setBlockMenu([]);
    }
  }, [district, blockOptions]);

  useEffect(() => {
    if (block?.code && facilityOptions?.length) {
      const newFacilityMenu = facilityOptions.filter((facility) => facility?.parentCode === block.code);
      setFacilityMenu(newFacilityMenu);
    } else {
      setFacilityMenu([]);
    }
  }, [block, facilityOptions]);

  useEffect(() => {
    setSortedDistrictMenu(
      districtMenu
        .map((district) => ({
          ...district,
          name: t(`Boundary_${district.code}`),
        }))
        .sort((a, b) => a.name.localeCompare(b.name))
    );
  }, [t, districtMenu]);

  useEffect(() => {
    setSortedBlockMenu(
      blockMenu
        .map((block) => ({
          ...block,
          name: t(`Boundary_${block.code}`),
        }))
        .sort((a, b) => a.name.localeCompare(b.name))
    );
  }, [t, blockMenu]);

  useEffect(() => {
    setSortedFacilityMenu(
      facilityMenu
        .map((facility) => ({
          ...facility,
          name: t(`Boundary_${facility.code}`),
        }))
        .sort((a, b) => a.name.localeCompare(b.name))
    );
  }, [t, facilityMenu]);

  useEffect(() => {
    if (creationError) {
      const timeOut = setTimeout(() => {
        setCreationError("");
      }, 2500);
      return () => clearTimeout(timeOut);
    }
  }, [creationError]);

  useEffect(async () => {
    if (
      selectBoundaryCode &&
      stateBoundaryCode &&
      selectBoundaryCode !== stateBoundaryCode &&
      districtMenu?.length &&
      blockOptions?.length &&
      facilityOptions?.length
    ) {
      const selectedHealthCentre = facilityOptions.find((facility) => facility?.code === selectBoundaryCode);
      if (selectedHealthCentre) {
        setHealthCentre({
          ...selectedHealthCentre,
          name: t(`Boundary_${selectedHealthCentre.code}`),
        });

        const selectedBlock = blockOptions.find((block) => block?.code === selectedHealthCentre.parentCode);
        if (selectedBlock) {
          setBlock({
            ...selectedBlock,
            name: t(`Boundary_${selectedBlock.code}`),
          });

          const selectedDistrict = districtMenu.find((district) => district?.code === selectedBlock.parentCode);
          if (selectedDistrict) {
            setDistrict({
              ...selectedDistrict,
              name: t(`Boundary_${selectedDistrict.code}`),
            });
          }
        }
      }
    }
  }, [t, districtMenu, blockOptions, facilityOptions, selectBoundaryCode, stateBoundaryCode]);

  const history = useHistory();

  useEffect(() => {
    if (
      healthcentre?.code &&
      district?.code &&
      block?.code
    ) {
      setSubmitValve(true);
    } else {
      setSubmitValve(false);
    }
  }, [
    healthcentre,
    district,
    block,
  ]);

  useEffect(() => {
    const checkFacilityStatus = async () => {
      if (healthcentre?.code) {
        setBlockUI(true);
        try {
          const data = await Digit.RMSService.fetchFacilityStatus({facilityId: healthcentre?.id});

          if (data?.items?.length) {
            setDuplicateTicketIds(
              data?.items?.map((item) => ({
                ticketId: item?.businessObject?.incident?.incidentId,
                ticketTenantId: item?.businessObject?.incident?.tenantId,
              }))
            );
          }
        } catch (error) {
          console.error("Error fetching facility status:", error);
        } finally {
          setBlockUI(false);
        }
      }
    };

    // checkFacilityStatus();
  }, [healthcentre]);

  const handleDistrictChange = async (selectedDistrict) => {
    setDistrict(selectedDistrict);
    setBlock({});
    setHealthCentre({});
  };

  async function selectedHealthCentre(value) {
    setHealthCentre(value);
  }
  const handleBlockChange = (selectedBlock) => {
    setHealthCentre({});
    setBlock(selectedBlock);
  };

  const wrapperSubmit = (data) => {
    const abc = handleButtonClick();
    if (!canSubmit) return;
    !abc && onSubmit(data);
  };

  const onSubmit = async (data) => {
    Digit.Utils.analytics.trackSubmitTicket({ page_name: "new_ticket_page" });
    if (!canSubmit) return;
    const formData = {
      ...data,
      district: {
        ...district,
        name: t(`Boundary_${district.code}`, { lng: "en_IN" }),
      },
      block: {
        ...block,
        name: t(`Boundary_${block.code}`, { lng: "en_IN" }),
      },
      healthcentre,
      tenantId,
    };

    // setBlockUI(true);
    console.debug("formData", formData);
    // const response = await Digit.Complaint.create(formData);
    //
    // if (!response?.IncidentWrappers) {
    //   setBlockUI(false);
    //   const assignErrorMessage = Array.isArray(response) ? response?.[0]?.message : response?.message || response;
    //   setCreationError(assignErrorMessage || t("CS_COMMON_SOMETHING_WENT_WRONG"));
    //   return;
    // }
    //
    // setBlockUI(false);
    // dispatch(populateCreateResponse(response));
    // await client.refetchQueries(["fetchInboxData"]);
    // history.push(parentUrl + "/incident/response");
  };

  const districtRef = useRef(null);
  const blockRef = useRef(null);
  const healthCareRef = useRef(null);
  const fieldsToValidate = [
    { field: district, ref: districtRef },
    { field: block, ref: blockRef },
    { field: healthcentre, ref: healthCareRef },
  ];

  const handleButtonClick = () => {
    const hasEmptyFields = fieldsToValidate.some(({ field }) => field === null || Object.keys(field).length === 0);

    if (hasEmptyFields) {
      fieldsToValidate.forEach(({ field, ref }) => {
        if (field === null || field === undefined || Object.keys(field).length === 0) {
          ref?.current?.validate();
        }
      });

      return true; // At least one field is empty
    } else {
      return false; // None of the fields are empty
    }
  };

  const config = [
    {
      head: t("TICKET_LOCATION"),
      body: [
        {
          label: t("INCIDENT_DISTRICT"),
          type: "dropdown",
          isMandatory: true,
          populators: (
            <Dropdown
              ref={districtRef}
              option={sortedDistrictMenu}
              optionKey="name"
              id="name"
              selected={district}
              select={handleDistrictChange}
              disable={!!(selectBoundaryCode && selectBoundaryCode !== stateBoundaryCode)}
              required={true}
            />
          ),
        },
        {
          label: t("INCIDENT_BLOCK"),
          isMandatory: true,
          type: "dropdown",
          menu: { ...blockMenu },
          populators: (
            <Dropdown
              ref={blockRef}
              option={sortedBlockMenu}
              optionKey="name"
              id="name"
              selected={block}
              select={handleBlockChange}
              disable={!!(selectBoundaryCode && selectBoundaryCode !== stateBoundaryCode)}
              required={true}
            />
          ),
        },
        {
          label: t("HEALTH_CARE_CENTRE"),
          isMandatory: true,
          type: "dropdown",
          populators: (
            <Dropdown
              ref={healthCareRef}
              t={t}
              option={sortedFacilityMenu}
              optionKey="name"
              id="healthCentre"
              selected={healthcentre}
              select={selectedHealthCentre}
              disable={!!(selectBoundaryCode && selectBoundaryCode !== stateBoundaryCode)}
              required={true}
            />
          ),
        },
      ],
    },
    {
      head: t("ADDITIONAL_DETAILS"),
      body: [
        {
          label: t("RMS_PAUSE_DURATION"),
          type: "date",
          isMandatory: false,
          populators: {
            name: "duration",
            validation: {
              minLength: 0,
              maxLength: 256,
            },
            error: t("CS_LENGTH_EXCEED"),
          },
        },
        {
          label: t("RMS_PAUSE_REASON"),
          type: "text",
          isMandatory: false,
          populators: {
            name: "reason",
            validation: {
              minLength: 0,
              maxLength: 256,
            },
            error: t("CS_LENGTH_EXCEED"),
          },
        },
      ],
    },
  ];

  return (
    <div className={"pause-rms-form-wrapper"}>
      <style>
        {`
          .employee-select-wrap .select:hover {
            --border-opacity: 1;
            border: 1px solid #7a2829;
            border-color: #7a2829;
          }
        `}
      </style>
      {blockUI && (
        <div
          style={{
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            height: "100%",
            width: "100%",
            zIndex: 10000005,
            backgroundColor: "rgba(128, 128, 128, 0.5)",
            position: "fixed",
            top: 0,
            left: 0,
          }}
        >
          <Loader />
        </div>
      )}

      <div style={{ color: "#9e1b32", marginBottom: "10px", textAlign: "right", marginRight: "0px" }}>
        <div style={{ marginRight: "15px" }}>
          <Link to={`/${window.contextPath}/employee`}>{t("CS_COMMON_BACK")}</Link>
        </div>
      </div>

      <FormComposer heading={t("PAUSE_RMS")} config={config} onSubmit={wrapperSubmit} isDisabled={!canSubmit} label={t("FILE_INCIDENT")} />

      {creationError && <Toast error={creationError} isDleteBtn={true} label={creationError} onClose={() => setCreationError(null)} />}

      {duplicateTicketIds?.length > 0 && (
        <PopUp>
          <div
            style={{
              backgroundColor: "white",
              position: "fixed",
              top: "50%",
              left: "50%",
              transform: "translate(-50%, -50%)",
              width: "400px",
              maxWidth: "95%",
              padding: "24px",
              borderRadius: "5px",
            }}
          >
            <h2
              style={{
                margin: "0 0 16px 0",
                fontSize: "20px",
                fontWeight: "600",
                color: "#333",
                textAlign: "center",
              }}
            >
              {t("IM_ALERT_POTENTIAL_DUPLICATES")}
            </h2>

            <div style={{ marginBottom: "24px" }}>
              <p
                style={{
                  fontSize: "16px",
                  textAlign: "center",
                  marginBottom: "5px",
                }}
              >
                {t("IM_ALERT_POTENTIAL_DUPLICATES_DESC")}
              </p>
              <p
                style={{
                  fontSize: "16px",
                  textAlign: "center",
                  marginBottom: "5px",
                  maxHeight: "250px",
                  overflow: "auto",
                }}
              >
                <span>
                  {t("IM_ALERT_POTENTIAL_DUPLICATES_EXISTING")}
                  {": "}
                </span>
                {duplicateTicketIds.map(({ ticketId, ticketTenantId }, index, array) => (
                  <span key={index}>
                    <Link
                      to={`/${window.contextPath}/employee/im/complaint/details/${ticketId}/${ticketTenantId}`}
                      target={"_blank"}
                      style={{ color: "#7a2829", textDecoration: "underline" }}
                    >
                      {ticketId}
                    </Link>
                    {index < array.length - 1 ? ", " : ""}
                  </span>
                ))}
              </p>
              <p
                style={{
                  fontSize: "16px",
                  textAlign: "center",
                }}
              >
                {t("IM_ALERT_POTENTIAL_DUPLICATES_ACTION_DESC")}
              </p>
            </div>

            <div style={{ display: "flex", justifyContent: "space-around" }}>
              <Button variation={"secondary"} style={{ width: "150px" }} label={t("TL_COMMON_YES")} onButtonClick={() => setDuplicateTicketIds([])} />
              <Button
                variation={"primary"}
                style={{ width: "150px" }}
                label={t("TL_COMMON_NO")}
                onButtonClick={() => history.push(`/${window.contextPath}/employee/im/inbox`)}
              />
            </div>
          </div>
        </PopUp>
      )}
    </div>
  );
};
