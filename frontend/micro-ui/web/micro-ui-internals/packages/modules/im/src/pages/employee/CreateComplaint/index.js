import React, { useState, useEffect, useRef } from "react";
import { useTranslation } from "react-i18next";
import { useDispatch } from "react-redux";
import { Button, Dropdown, Loader, MultiUploadWrapper, PopUp } from "@selco/digit-ui-react-components";
import { useRouteMatch, useHistory } from "react-router-dom";
import { useQueryClient } from "react-query";
import { FormComposer } from "../../../components/FormComposer";
import { createComplaint } from "../../../redux/actions/index";
import { Link } from "react-router-dom";

export const CreateComplaint = ({ parentUrl }) => {
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
  const [file, setFile] = useState(null);
  const [showToast, setShowToast] = useState(null);
  const [uploadedFile, setUploadedFile] = useState([]);
  const [uploadedImages, setUploadedImagesIds] = useState(null);
  const [isUploading, setIsUploading] = useState(false);
  const [isImageUploading, setIsImageUploading] = useState(false);
  const [isVideoUploading, setIsVideoUploading] = useState(false);
  const [imageState, setImageState] = useState({ newArr: [], mappedArray: [] });
  const [videoState, setVideoState] = useState({ newArr: [], mappedArray: [] });
  const specificFileConstraint = [
    { type: "video", maxSize: 50, maxFiles: 2 },
    { type: "image", maxSize: 10, maxFiles: 5 },
  ];
  const [district, setDistrict] = useState(null);
  const [block, setBlock] = useState(null);
  const [error, setError] = useState(null);
  const [canSubmit, setSubmitValve] = useState(false);
  const [submitted, setSubmitted] = useState(false);
  const tenantId = window.Digit.SessionStorage.get("Employee.tenantId");
  const [complaintType, setComplaintType] = useState({});
  const [subTypeMenu, setSubTypeMenu] = useState([]);
  const [disbaled, setDisable] = useState(true);
  const [disbaledUpload, setDisableUpload] = useState(true);
  const [phcMenuNew, setPhcMenu] = useState([]);
  const [subType, setSubType] = useState({});
  const [systemFunctionality, setSystemFunctionality] = useState();
  const [systemFunctionalityMenu, setSystemFunctionalityMenu] = useState([]);
  const [dataState, setDataState] = useState({ newArr: [], mappedArray: [] });
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
      setFacilityOptions(facilityData?.facilities?.map((facility) => ({
        code: facility.boundaryCode,
        id: facility.facilityId,
        parentCode: facilityBoundaryCodeToParentMap.get(facility.boundaryCode),
      })));
    }
  }, [facilityBoundaries, facilityData]);

  useEffect(() => {
    setSortedDistrictMenu(
      districtMenu
        .map((district) => ({
          ...district,
          name: t(`Boundary_${district.code}`),
        }))
        .sort((a, b) => a.name.localeCompare(b.name)),
    )
  }, [t, districtMenu]);

  useEffect(() => {
    setSortedBlockMenu(
      blockMenu
        .map((block) => ({
          ...block,
          name: t(`Boundary_${block.code}`),
        }))
        .sort((a, b) => a.name.localeCompare(b.name)),
    )
  }, [t, blockMenu]);

  useEffect(() => {
    setSortedFacilityMenu(
      facilityMenu
        .map((facility) => ({
          ...facility,
          name: t(`Boundary_${facility.code}`),
        }))
        .sort((a, b) => a.name.localeCompare(b.name)),
    )
  }, [t, facilityMenu]);

  let sortedSubMenu = [];
  if (subTypeMenu !== null) {
    sortedSubMenu = subTypeMenu.sort((a, b) => a.name.localeCompare(b.name));
  }

  const menu = Digit.Hooks.pgr.useComplaintTypes({ stateCode: tenantId });
  let sortedMenu = [];
  if (menu !== null) {
    let othersItem = menu.find((item) => item.key === "Others");
    let otherItems = menu.filter((item) => item.key !== "Others");
    otherItems.sort((a, b) => a.name.localeCompare(b.name));
    if (othersItem) {
      otherItems.push(othersItem);
    }
    sortedMenu = otherItems;
  }

  if (subTypeMenu !== null) {
    let othersItem = subTypeMenu.find((item) => item.key === "Other");
    let otherItems = subTypeMenu.filter((item) => item.key !== "Other");
    otherItems.sort((a, b) => a.name.localeCompare(b.name));
    if (othersItem) {
      otherItems.push(othersItem);
    }
    sortedSubMenu = otherItems;
  }
  const state = Digit.ULBService.getStateId();
  const { data: mdmsData } = Digit.Hooks.pgr.useMDMS(state, "Incident", ["SystemFunctionality"]);

  useEffect(() => {
    const fetchSystemFunctionalMenu = async () => {
      const response = mdmsData?.Incident?.SystemFunctionality;
      if (response) {
        setSystemFunctionalityMenu(
          response.filter(def => def.active)
            .sort((a, b) => a.name.localeCompare(b.name))
            .map((def) => ({
              key: def.code,
              name: t(def.name),
            }))
        );
      }
    }

    fetchSystemFunctionalMenu();
  }, [state, mdmsData, t]);

  useEffect(() => {
    Digit.Utils.analytics.trackPageView("new_ticket_page", {
      page_path: window.location?.pathname || "/new-ticket",
      page_title: "New Ticket",
    });
    if (selectBoundaryCode !== stateBoundaryCode) {
      ticketTypeRef?.current?.validate();
      ticketSubTypeRef?.current?.validate();
    } else {
      handleButtonClick();
    }
  }, []);

  useEffect(async () => {
    if (
      selectBoundaryCode && stateBoundaryCode && selectBoundaryCode !== stateBoundaryCode
      && districtMenu?.length && blockOptions?.length && facilityOptions?.length
    ) {
      const selectedHealthCentre = facilityOptions.find((facility) => facility?.code === selectBoundaryCode)
      if (selectedHealthCentre) {
        setHealthCentre({
          ...selectedHealthCentre,
          name: t(`Boundary_${selectedHealthCentre.code}`),
        })

        const selectedBlock = blockOptions.find((block) => block?.code === selectedHealthCentre.parentCode);
        if (selectedBlock) {
          setBlock({
            ...selectedBlock,
            name: t(`Boundary_${selectedBlock.code}`),
          })

          const selectedDistrict = districtMenu.find((district) => district?.code === selectedBlock.parentCode);
          if (selectedDistrict) {
            setDistrict({
              ...selectedDistrict,
              name: t(`Boundary_${selectedDistrict.code}`),
            })
          }
        }
      }
    }
  }, [t, districtMenu, blockOptions, facilityOptions, selectBoundaryCode, stateBoundaryCode]);

  useEffect(() => {
    (async () => {
      setError(null);
      if (file) {
        const allowedFileTypesRegex = /(.*?)(jpg|jpeg|png|image|pdf)$/i;
        if (file.size >= 5242880) {
          setError(t("CS_MAXIMUM_UPLOAD_SIZE_EXCEEDED"));
        } else if (file?.type && !allowedFileTypesRegex.test(file?.type)) {
          setError(t(`NOT_SUPPORTED_FILE_TYPE`));
        } else {
          try {
            const response = await Digit.UploadServices.Filestorage("Incident", file, tenantId);
            if (response?.data?.files?.length > 0) {
              //setUploadedFile(response?.data?.files[0]?.fileStoreId);
            } else {
              setError(t("CS_FILE_UPLOAD_ERROR"));
            }
          } catch (err) {
            setError(t("CS_FILE_UPLOAD_ERROR"));
          }
        }
      }
    })();
  }, [file]);
  const dispatch = useDispatch();
  const history = useHistory();
  const serviceDefinitions = Digit.GetServiceDefinitions;
  const client = useQueryClient();

  useEffect(() => {
    const isAnyUploading = isImageUploading || isVideoUploading;
    if (complaintType?.key && subType?.key && systemFunctionality?.key && healthcentre?.code && district?.code && block?.code && !isAnyUploading) {
      setSubmitValve(true);
    } else {
      setSubmitValve(false);
    }
  }, [complaintType, subType, systemFunctionality, healthcentre, district, block, isImageUploading, isVideoUploading]);

  useEffect(() => {
    const handleDuplicateCheck = async () => {
      if (healthcentre?.code && complaintType?.key && subType?.key) {
        setBlockUI(true);
        try {
          const data = await Digit.InboxGeneral.Search({
            inbox: {
              tenantId,
              processSearchCriteria: {
                businessService: ["Incident"],
                moduleName: "Incident",
                status: [
                  "PENDINGFORASSIGNMENT",
                  "PENDINGRESOLUTION",
                  "PENDING_ASSIGNMENT_SPARE_PART_NEEDED",
                  "PENDING_ASSIGNMENT_OUT_OF_WARRANTY",
                  "PENDING_RESOLUTION_SPARE_PART_NEEDED",
                  "PENDING_RESOLUTION_OUT_OF_WARRANTY"
                ],
                tenantId,
              },
              jurisdictionSearchCriteria: jurisdictionCurrentBoundary,
              moduleSearchCriteria: {
                facility: [healthcentre.code],
                incidentType: [complaintType.key],
                incidentSubType: [subType?.key],
                tenantId,
                sortOrder: "DESC"
              },
              limit: 100,
              offset: 0
            }
          });

          if (data?.items?.length) {
            setDuplicateTicketIds(data?.items?.map(item => ({
              ticketId: item?.businessObject?.incident?.incidentId,
              ticketTenantId: item?.businessObject?.incident?.tenantId,
            })));
          }
        } catch (error) {
          console.error("Error fetching duplicate tickets:", error);
        } finally {
          setBlockUI(false);
        }
      }
    }

    handleDuplicateCheck();
  }, [healthcentre, complaintType, subType]);

  async function selectedType(value) {
    setDisableUpload(false);
    if (value.key !== complaintType.key) {
      if (value.key === "Others") {
        setSubType({ name: "" });
        setComplaintType(value);
        setSubTypeMenu([{ key: "Others", name: t("SERVICEDEFS.OTHERS") }]);
        ticketSubTypeRef?.current?.validate();
      } else {
        setSubType({ name: "" });
        setComplaintType(value);
        setSubTypeMenu(await serviceDefinitions.getSubMenu(tenantId, value, t));
        ticketSubTypeRef?.current?.validate();
      }
    }
  }
  const handleDistrictChange = async (selectedDistrict) => {
    setDistrict(selectedDistrict);
    setBlock({});
    setHealthCentre({});

    const newBlocksMenu = blockOptions?.filter((blockOption) => blockOption?.parentCode === selectedDistrict?.code);
    setBlockMenu(newBlocksMenu);
  };

  function selectedSubType(value) {
    setSubType(value);
  }

  function selectedSystemFunctionality(value) {
    setSystemFunctionality(value);
  }
  async function selectedHealthCentre(value) {
    setHealthCentre(value);
    setDisableUpload(false);
    setDisable(false);
    setShowToast(null);
  }
  const handleBlockChange = (selectedBlock) => {
    setHealthCentre({});
    setBlock(selectedBlock);
    const newFacilityMenu = facilityOptions.filter((facility) => facility?.parentCode === selectedBlock?.code);
    setFacilityMenu(newFacilityMenu);
  };

  const wrapperSubmit = (data) => {
    const abc = handleButtonClick();
    if (!canSubmit) return;
    setSubmitted(true);
    !submitted && !abc && onSubmit(data);
  };
  const onSubmit = async (data) => {
    Digit.Utils.analytics.trackSubmitTicket({ page_name: "new_ticket_page" });
    if (!canSubmit) return;
    const formData = {
      ...data,
      complaintType,
      subType,
      systemFunctionality,
      district : {
        ...district,
        name: t(`Boundary_${district.code}`, { lng: "en_IN" })
      },
      block : {
        ...block,
        name: t(`Boundary_${block.code}`, { lng: "en_IN" })
      },
      healthcentre,
      uploadedFile,
      tenantId,
    };
    await dispatch(createComplaint(formData));
    await client.refetchQueries(["fetchInboxData"]);
    history.push(parentUrl + "/incident/response");
  };
  const districtRef = useRef(null);
  const blockRef = useRef(null);
  const healthCareRef = useRef(null);
  const ticketTypeRef = useRef(null);
  const ticketSubTypeRef = useRef(null);
  const systemFunctionalityRef = useRef(null);
  const fieldsToValidate = [
    { field: district, ref: districtRef },
    { field: block, ref: blockRef },
    { field: healthcentre, ref: healthCareRef },
    { field: complaintType, ref: ticketTypeRef },
    { field: subType, ref: ticketSubTypeRef },
    { field: systemFunctionality, ref: systemFunctionalityRef },
  ];
  const getData = (state) => {
    let data = Object.fromEntries(state);
    const mappedArray = state.map((item) => {
      return item[1];
    });
    let newArr = Object.values(data);

    setDataState({ newArr, mappedArray });
  };

  const getImageData = (state) => {
    let data = Object.fromEntries(state);
    const mappedArray = state.map((item) => {
      return item[1];
    });
    let newArr = Object.values(data);

    setImageState({ newArr, mappedArray });
  };

  const getVideoData = (state) => {
    let data = Object.fromEntries(state);
    const mappedArray = state.map((item) => {
      return item[1];
    });
    let newArr = Object.values(data);

    setVideoState({ newArr, mappedArray });
  };
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
  function selectfile(imageArr, imageMappedArr, videoArr, videoMappedArr) {
    let file = [];
    let videoCount = 0;

    console.log("Processing files - Images:", imageMappedArr.length, "Videos:", videoMappedArr.length);

    // Process image files
    if (imageArr && imageMappedArr.length > 0) {
      const imageFiles = imageMappedArr.flatMap((e) => {
        if (!e?.file || !e?.fileStoreId) return [];

        const { file, fileStoreId } = e;
        const { type } = file;

        const documentType = type.includes(".sheet") ? ".xlsx" : type.includes(".document") ? ".docs" : type;

        console.log("Processing image file:", file.name, "Type:", type, "FileStoreId:", fileStoreId.fileStoreId);
        return [{ fileStoreId: fileStoreId.fileStoreId, documentUid: "", documentType, additionalDetails: {} }];
      });
      file = [...file, ...imageFiles];
      console.log("Added", imageFiles.length, "image files to payload");
    }

    // Process video files
    if (videoArr && videoMappedArr.length > 0) {
      const videoFiles = videoMappedArr.flatMap((e) => {
        if (!e?.file || !e?.fileStoreId) return [];

        const { file, fileStoreId } = e;
        const { type } = file;

        const documentType = type.includes(".sheet") ? ".xlsx" : type.includes(".document") ? ".docs" : type;

        if (type.includes("video")) {
          videoCount++;
          const videoUid = `video${videoCount}`;
          console.log("Processing video file:", file.name, "Type:", type, "FileStoreId:", fileStoreId.fileStoreId, "MasterFileStoreId:", fileStoreId.masterFileStoreId);
          return [
            { fileStoreId: fileStoreId.masterFileStoreId, documentUid: videoUid, documentType: "HLS", additionalDetails: {} },
            { fileStoreId: fileStoreId.fileStoreId, documentUid: videoUid, documentType, additionalDetails: {} },
          ];
        }

        return [{ fileStoreId: fileStoreId.fileStoreId, documentUid: "", documentType, additionalDetails: {} }];
      });
      file = [...file, ...videoFiles];
      console.log("Added", videoFiles.length, "video file entries to payload");
    }

    // Remove Duplicates Efficiently Using Set()
    const seen = new Set();
    file = file.filter((doc) => {
      if (!doc.fileStoreId || seen.has(doc.fileStoreId)) return false;
      seen.add(doc.fileStoreId);
      return true;
    });

    console.log("Final uploaded files count:", file.length, "Files:", file);
    setUploadedFile(file);
  }

  useEffect(() => {
    selectfile(imageState.newArr, imageState.mappedArray, videoState.newArr, videoState.mappedArray);
  }, [imageState, videoState]);
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
      head: t("TICKET_DETAILS"),
      body: [
        {
          label: t("TICKET_TYPE"),
          type: "dropdown",
          isMandatory: true,
          populators: (
            <Dropdown
              ref={ticketTypeRef}
              option={sortedMenu}
              optionKey="name"
              id="complaintType"
              selected={complaintType}
              select={selectedType}
              required={true}
            />
          ),
        },
        {
          label: t("TICKET_SUBTYPE"),
          type: "dropdown",
          isMandatory: true,
          menu: { ...subTypeMenu },
          populators: (
            <Dropdown
              ref={ticketSubTypeRef}
              option={sortedSubMenu}
              optionKey="name"
              id="complaintSubType"
              selected={subType}
              select={selectedSubType}
              required={true}
            />
          ),
        },
        {
          label: t("SYSTEM_FUNCTIONAL"),
          type: "dropdown",
          isMandatory: true,
          populators: (
            <div>
              <Dropdown
                ref={systemFunctionalityRef}
                option={systemFunctionalityMenu}
                optionKey="name"
                id="systemFunctionality"
                selected={systemFunctionality}
                select={selectedSystemFunctionality}
                required={true}
              />
            </div>
          ),
        }
      ],
    },
    {
      head: t("ADDITIONAL_DETAILS"),
      body: [
        {
          label: t("INCIDENT_COMMENTS"),
          type: "text",
          isMandatory: false,
          populators: {
            name: "comments",
            // maxLength: 256,
            validation: {
              minLength: 0,
              maxLength: 256,
            },
            error: t("CS_LENGTH_EXCEED"),
          },
        },
        {
          label: t("INCIDENT_UPLOAD_IMAGE"),
          populators: (
            <div>
              <MultiUploadWrapper
                t={t}
                module="Incident"
                tenantId={tenantId}
                getFormState={(state, loading) => getImageData(state, loading)}
                onUploadStatusChange={setIsImageUploading}
                allowedFileTypesRegex={/(jpg|jpeg|png|image)$/i}
                allowedMaxSizeInMB={50}
                maxFilesAllowed={5}
                disabled={disbaledUpload}
                ulb={Digit.SessionStorage.get("Employee.tenantId")}
                acceptFiles={".png, .jpg, .jpeg, image/*"}
                multiple={true}
                specificFileConstraint={specificFileConstraint[1]}
                analyticsPage="new_ticket_page"
                mediaIntent="image"
              />
              {/* <ImageUploadHandler tenantId={tenant} uploadedImages={uploadedImages} onPhotoChange={handleUpload} disabled={disbaled}/> */}
              <div style={{ marginTop: "10px", marginBottom: "20px", fontSize: "12px", color: "#b5b4b4" }}>{t("CS_MAXIMUM_IMAGES")}</div>
            </div>
          ),
        },
        {
          label: t("INCIDENT_UPLOAD_VIDEO"),
          populators: (
            <div>
              <MultiUploadWrapper
                t={t}
                module="Incident"
                tenantId={tenantId}
                getFormState={(state, loading) => getVideoData(state, loading)}
                onUploadStatusChange={setIsVideoUploading}
                allowedFileTypesRegex={/(mp4|mov|avi|wmv|video)$/i}
                allowedMaxSizeInMB={50}
                maxFilesAllowed={2}
                disabled={disbaledUpload}
                ulb={Digit.SessionStorage.get("Employee.tenantId")}
                acceptFiles={".mp4, .avi, .mov, .wmv, video/*"}
                multiple={false}
                specificFileConstraint={specificFileConstraint[0]}
                analyticsPage="new_ticket_page"
                mediaIntent="video"
              />
              {/* <ImageUploadHandler tenantId={tenant} uploadedImages={uploadedImages} onPhotoChange={handleUpload} disabled={disbaled}/> */}
              <div style={{ marginTop: "10px", fontSize: "12px", color: "#b5b4b4" }}>{t("CS_MAXIMUM_VIDEOS")}</div>
            </div>
          ),
        },
      ],
    },
  ];
  return (
    <div>
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
      <FormComposer heading={t("")} config={config} onSubmit={wrapperSubmit} isDisabled={!canSubmit && !submitted} label={t("FILE_INCIDENT")} />

      {/* <button onClick={(!selectedOption || Object.keys(selectedOption).length == 0)}>Check Errors</button>  
      {errors.map((error, index) => (
        <div key={index}>{error}</div>
      ))} */}
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
              <Button
                variation={"secondary"}
                style={{ width: "150px" }}
                label={t("TL_COMMON_YES")}
                onButtonClick={() => setDuplicateTicketIds([])}
              />
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
