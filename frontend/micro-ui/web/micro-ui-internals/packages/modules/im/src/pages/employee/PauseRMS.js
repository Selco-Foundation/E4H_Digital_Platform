import React, { useState, useEffect, useRef } from "react";
import { useTranslation } from "react-i18next";
import { Dropdown, Loader, TextInput, Toast } from "@selco/digit-ui-react-components";
import { useHistory } from "react-router-dom";
import { FormComposer } from "../../components/FormComposer";
import {useQueryClient} from "react-query";
import {useDispatch} from "react-redux";
import {populatePauseRMSResponse} from "../../redux/actions/complaint";
import CommonUtils from "../../utilities/CommonUtils";
import ConfirmationAlert from "../../components/ConfirmationAlert";
import FormattedDateInput from "../../components/custom/FormattedDateInput";

export const PauseRMS = ({ parentUrl }) => {
  const { t } = useTranslation();
  const tenantId = Digit.ULBService.getCurrentTenantId();
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
  const [blockUI, setBlockUI] = useState(false);
  const [selectBoundaryCode, setSelectBoundaryCode] = useState("");
  const jurisdictionCurrentBoundary = Digit.PersistantStorage.get("Jurisdiction.CurrentBoundary") || {
    country: ["-"],
  };
  const jurisdictionCurrentBoundaryCodes = Digit.Utils.BoundaryUtil.aggregateBoundaryCodes(jurisdictionCurrentBoundary);
  const [stateBoundaryCode, setStateBoundaryCode] = useState("");
  const [facilityBoundaries, setFacilityBoundaries] = useState([]);
  const [facilityBoundaryCodes, setFacilityBoundaryCodes] = useState(["-"]);
  const [disableGeographySelection, setDisableGeographySelection] = useState(false);
  const [isPausedFacility, setIsPausedFacility] = useState(false);
  const { facilityId } = Digit.Hooks.useQueryParams();
  const client = useQueryClient();
  const dispatch = useDispatch();
  const [reason, setReason] = useState("");
  const [duration, setDuration] = useState("");
  const [savedDuration, setSavedDuration] = useState("");
  const [canModify, setCanModify] = useState(false);
  const [formData, setFormData] = useState({});
  const [alert, setAlert] = useState(null);

  const { data: boundaryData, isLoading: boundaryDataLoading } = Digit.Hooks.im.useBoundary(jurisdictionCurrentBoundaryCodes);
  const { data: facilityData, isLoading: facilityDataLoading } = Digit.Hooks.im.useFacility(facilityBoundaryCodes);

  useEffect(() => {
    setSelectBoundaryCode(jurisdictionCurrentBoundaryCodes?.join(","));
    if (boundaryData) {
      setStateBoundaryCode(boundaryData.states?.map((state) => state?.code)?.join(","));
      setDistrictMenu(boundaryData.districts || []);
      setBlockOptions(boundaryData.blocks);
      setFacilityBoundaries(boundaryData.facilities);
      setFacilityBoundaryCodes(boundaryData.facilities?.map((facility) => facility?.code) || ["-"]);
    }
  }, [boundaryData, t]);

  useEffect(() => {
    if (facilityId) {
      const selectedHealthCentre = facilityOptions.find((facility) => facility?.id === facilityId);
      const selectedBlock = blockOptions.find((block) => block?.code === selectedHealthCentre?.parentCode);
      const selectedDistrict = districtMenu.find((district) => district?.code === selectedBlock?.parentCode);
      if (selectedHealthCentre && selectedBlock && selectedDistrict) {
        setHealthCentre({
          ...selectedHealthCentre,
          name: t(`Boundary_${selectedHealthCentre.code}`),
        });
        setBlock({
          ...selectedBlock,
          name: t(`Boundary_${selectedBlock.code}`),
        });
        setDistrict({
          ...selectedDistrict,
          name: t(`Boundary_${selectedDistrict.code}`),
        });
        setDisableGeographySelection(true);
      }
    }
  }, [districtMenu, blockOptions, facilityOptions, t]);

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
          facilityName: facility.facilityName,
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
    if (healthcentre?.code && district?.code && block?.code && duration && reason) {
      setSubmitValve(true);
    } else {
      setSubmitValve(false);
    }
  }, [healthcentre, district, block, duration, reason]);

  useEffect(() => {
    const checkFacilityStatus = async () => {
      if (healthcentre?.code) {
        setBlockUI(true);
        try {
          const data = await Digit.RMSService.fetchFacilityStatus({
            FacilitySearch: {
              tenantId,
              facilityId: healthcentre?.id
            }
          });

          if (data?.isPaused) {
            setIsPausedFacility(true);
            setReason(data?.reason || "");
            setDuration(data?.pausedUntil ? CommonUtils.formatUTCDate(data.pausedUntil * 1000) : "");
            setSavedDuration(data?.pausedUntil ? CommonUtils.formatUTCDate(data.pausedUntil * 1000) : "");
          } else {
            setIsPausedFacility(false);
          }
        } catch (error) {
          setIsPausedFacility(false);
          console.error("Error fetching facility status:", error);
        } finally {
          setBlockUI(false);
        }
      }
    };

    checkFacilityStatus();
  }, [healthcentre]);

  useEffect(() => {
    setCanModify(savedDuration !== duration);
  }, [savedDuration, duration]);

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

  const wrapperSubmit = (data, action = "PAUSE") => {
    const abc = handleButtonClick();
    if (!canSubmit) return;
    !abc && onSubmit(data, action);
  };

  const onSubmit = async (data, action) => {
    Digit.Utils.analytics.trackSubmitTicket({ page_name: "pause_rms_page" });
    if (!canSubmit) return;
    const formData = {
      ...data,
      tenantId,
      reason: reason,
      pausedUntil: `${duration}T23:59:59Z`,
      action,
      facilityId: healthcentre?.id,
      facilityName: healthcentre?.facilityName,
      boundaryCode: healthcentre?.code,
    };
    setBlockUI(true);

    try {
      await Digit.RMSService.updateRMSTicketPause({ PauseFacility: formData });

      setBlockUI(false);
      dispatch(
        populatePauseRMSResponse({
          success: true,
          message: action === "PAUSE" ? (isPausedFacility ? t("MODIFY_PAUSE_RMS_SUCCESS_MESSAGE") : t("PAUSE_RMS_SUCCESS_MESSAGE")) : t("RESUME_RMS_SUCCESS_MESSAGE"),
          cardText: action === "PAUSE" ? (isPausedFacility ? t("MODIFY_PAUSE_RMS_CARD_TEXT") : t("PAUSE_RMS_CARD_TEXT")) : t("RESUME_RMS_CARD_TEXT"),
          facilityId: healthcentre?.id,
          info: t("RMS_FACILITY_ID")
        })
      );

      await client.refetchQueries(["RMS_PAUSED_FACILITY"]);
      history.push(parentUrl + "/incident/rms-response");
    } catch (error) {
      setBlockUI(false);
      setCreationError(CommonUtils.getApiErrorMessage(error) || t("CS_COMMON_SOMETHING_WENT_WRONG"));
    }
  };

  const resumeFacility = async () => {
    await onSubmit({}, "RESUME");
  };

  const handleFacilityActivation = () => {
    setAlert({
      message: t("RMS_FACILITY_ACTIVATION_ALERT_DESC"),
      continueAction: async () => await resumeFacility(),
    });
  }

  const handleFacilityModification = () => {
    setAlert({
      message: `${t("RMS_FACILITY_MODIFICATION_ALERT_DESC")} ${CommonUtils.formatUTCDate((new Date(duration)).getTime(), "DD/MM/YYYY")}`,
      continueAction: async () => wrapperSubmit(formData, "PAUSE"),
    });
  }

  const handleFormValueChange = (data) => {
    if (CommonUtils.isNotEqual(data, formData)) {
      setFormData(data);
    }
  }

  const districtRef = useRef(null);
  const blockRef = useRef(null);
  const healthCareRef = useRef(null);
  const durationRef = useRef(null);
  const reasonRef = useRef(null);
  const fieldsToValidate = [
    { field: district, ref: districtRef },
    { field: block, ref: blockRef },
    { field: healthcentre, ref: healthCareRef },
    { field: duration, ref: durationRef },
    { field: reason, ref: reasonRef },
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
      head: t("FACILITY_LOCATION"),
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
              disable={!!(selectBoundaryCode && selectBoundaryCode !== stateBoundaryCode) || disableGeographySelection}
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
              disable={!!(selectBoundaryCode && selectBoundaryCode !== stateBoundaryCode) || disableGeographySelection}
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
              disable={!!(selectBoundaryCode && selectBoundaryCode !== stateBoundaryCode) || disableGeographySelection}
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
          type: "custom",
          isMandatory: true,
          populators: (
            <FormattedDateInput
              type={"date"}
              className="field desktop-w-full employee-card-input"
              value={duration}
              inputRef={durationRef}
              onChange={(e) => setDuration(e.target.value)}
              min={new Date().toISOString().split("T")[0]}
              dateFormat={"dd/MM/yyyy"}
            />
          ),
        },
        {
          label: t("RMS_PAUSE_REASON"),
          type: "custom",
          isMandatory: true,
          populators: (
            <TextInput
              type={"text"}
              className="field desktop-w-full"
              value={reason}
              inputRef={reasonRef}
              onChange={(e) => setReason(e.target.value)}
              validation={{
                minLength: 0,
                maxLength: 256,
              }}
              error={t("CS_LENGTH_EXCEED")}
              required={true}
            />
          ),
        },
      ],
    },
  ];

  const handleBack = () => {
    if (window.history.length > 1) {
      history.goBack();
    } else {
      history.push(`/${window.contextPath}/employee/im/paused-rms-facilities`);
    }
  };

  const showLoader = blockUI || (facilityId && (boundaryDataLoading || facilityDataLoading));

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
      {showLoader && (
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
        <div style={{ paddingRight: "15px", display: "flex", justifyContent: "flex-end" }}>
          <div onClick={handleBack} style={{ width: "fit-content", cursor: "pointer" }}>
            {t("CS_COMMON_BACK")}
          </div>
        </div>
      </div>

      <FormComposer
        heading={t("PAUSE_RMS")}
        config={config}
        onSubmit={(data) => (isPausedFacility ? handleFacilityActivation() : wrapperSubmit(data, "PAUSE"))}
        isDisabled={isPausedFacility ? false : !canSubmit}
        label={isPausedFacility ? t("CS_ACTION_ACTIVATE") : t("CS_ACTION_DISABLE")}
        secondaryActionLabel={isPausedFacility ? t("CS_ACTION_MODIFY") : ""}
        isSecondaryActionDisabled={!(canSubmit && canModify)}
        onSecondaryActionClick={isPausedFacility ? handleFacilityModification : null}
        actionBarClassName={"reverse-actionbar"}
        onFormValueChange={handleFormValueChange}
      />

      {alert && <ConfirmationAlert t={t} alert={alert} setAlert={setAlert} />}

      {creationError && <Toast error={creationError} isDleteBtn={true} label={creationError} onClose={() => setCreationError(null)} />}
    </div>
  );
};
