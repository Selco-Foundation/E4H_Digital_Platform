import {
  aggregateBoundaryCodes,
  useAuthStore,
  useBoundary,
  useJurisdictionStore,
  useTranslate,
} from "@/shared";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useCallback, useEffect, useMemo, useState } from "react";
import { uploadIncidentFile, uploadIncidentVideo } from "../services/file-upload";
import { createIncident, searchPotentialDuplicates } from "../services/incident";
import {
  fetchServiceDefsForMenuPath,
  fetchSystemFunctionalityOptions,
  fetchTicketTypeMenu,
} from "../services/mdms";
import type {
  BoundaryOption,
  CreateIncidentFormValues,
  CreateIncidentResponse,
  SelectOption,
  UploadedMediaEntry,
} from "../types/create-incident";
import { buildUploadedDocuments } from "../utils/create-incident-documents";
import {
  MAX_COMMENT_LENGTH,
  MAX_FIR_COUNT,
  MAX_FIR_SIZE_MB,
  MAX_IMAGE_COUNT,
  MAX_IMAGE_SIZE_MB,
  MAX_VIDEO_COUNT,
  MAX_VIDEO_SIZE_MB,
  validateMediaFiles,
  type MediaKind,
  type MediaValidationError,
} from "../utils/media-validation";

const DRAFT_STORAGE_KEY = "sem-im-create-draft";

interface FieldErrors {
  district?: string;
  block?: string;
  facility?: string;
  ticketType?: string;
  ticketSubType?: string;
  systemFunctional?: string;
  comments?: string;
  image?: string;
  video?: string;
  fir?: string;
}

const EMPTY_FORM: CreateIncidentFormValues = {
  district: null,
  block: null,
  facility: null,
  ticketType: null,
  ticketSubType: null,
  systemFunctional: null,
  comments: "",
};

const MEDIA_ERROR_MESSAGE_CONFIG: Record<
  MediaKind,
  { maxCount: number; maxSizeMb: number; formats: string; countKey: string; sizeKey: string; formatKey: string }
> = {
  image: {
    maxCount: MAX_IMAGE_COUNT,
    maxSizeMb: MAX_IMAGE_SIZE_MB,
    formats: "JPG, JPEG, PNG",
    countKey: "INCIDENT_IMAGE_COUNT_EXCEEDED",
    sizeKey: "INCIDENT_IMAGE_SIZE_EXCEEDED",
    formatKey: "INCIDENT_IMAGE_FORMAT_INVALID",
  },
  video: {
    maxCount: MAX_VIDEO_COUNT,
    maxSizeMb: MAX_VIDEO_SIZE_MB,
    formats: "MP4, MOV, AVI, WMV",
    countKey: "INCIDENT_VIDEO_COUNT_EXCEEDED",
    sizeKey: "INCIDENT_VIDEO_SIZE_EXCEEDED",
    formatKey: "INCIDENT_VIDEO_FORMAT_INVALID",
  },
  fir: {
    maxCount: MAX_FIR_COUNT,
    maxSizeMb: MAX_FIR_SIZE_MB,
    formats: "PDF, JPG, JPEG, PNG",
    countKey: "INCIDENT_FIR_COUNT_EXCEEDED",
    sizeKey: "INCIDENT_FIR_SIZE_EXCEEDED",
    formatKey: "INCIDENT_FIR_FORMAT_INVALID",
  },
};

function buildMediaErrorMessage(
  t: (key: string) => string,
  kind: MediaKind,
  error: MediaValidationError,
): string {
  const { maxCount, maxSizeMb, formats, countKey, sizeKey, formatKey } =
    MEDIA_ERROR_MESSAGE_CONFIG[kind];

  if (error.code === "COUNT") {
    return t(countKey).replace(
      "{MAX_COUNT}",
      String(maxCount),
    );
  }

  if (error.code === "SIZE") {
    return t(sizeKey).replace("{MAX_SIZE}", String(maxSizeMb));
  }

  return t(formatKey).replace("{FORMATS}", formats);
}

function toBoundaryOption(
  node: { code: string; parentCode: string },
  t: (key: string) => string,
): BoundaryOption {
  return {
    code: node.code,
    parentCode: node.parentCode,
    name: t(`Boundary_${node.code}`),
  };
}

export function useCreateIncidentForm(inboxPath: string) {
  const { t } = useTranslate();
  const queryClient = useQueryClient();
  const user = useAuthStore((state) => state.user);
  const accessToken = useAuthStore((state) => state.accessToken);
  const employeeTenantId = useAuthStore((state) => state.employeeTenantId);
  const boundaries = useJurisdictionStore((state) => state.boundaries);
  const jurisdictionCodes = useMemo(() => aggregateBoundaryCodes(boundaries), [boundaries]);
  const { data: boundaryData, isLoading: isBoundaryLoading } = useBoundary(jurisdictionCodes);

  const [form, setForm] = useState<CreateIncidentFormValues>(EMPTY_FORM);
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [ticketTypeMenu, setTicketTypeMenu] = useState<SelectOption[]>([]);
  const [ticketSubTypeMenu, setTicketSubTypeMenu] = useState<SelectOption[]>([]);
  const [systemFunctionalMenu, setSystemFunctionalMenu] = useState<SelectOption[]>([]);
  const [imageUploads, setImageUploads] = useState<UploadedMediaEntry[]>([]);
  const [videoUploads, setVideoUploads] = useState<UploadedMediaEntry[]>([]);
  const [firUploads, setFirUploads] = useState<UploadedMediaEntry[]>([]);
  const [isImageUploading, setIsImageUploading] = useState(false);
  const [isVideoUploading, setIsVideoUploading] = useState(false);
  const [isFirUploading, setIsFirUploading] = useState(false);
  const [duplicateTickets, setDuplicateTickets] = useState<
    Array<{ ticketId: string; ticketTenantId: string }>
  >([]);
  // Matches DIGIT-UI's CreateComplaint/index.js: `isTheftIssue = complaintType?.key?.toUpperCase() === "THEFT"`
  // gates the mandatory "Upload FIR or Police Complaint Letter" field.
  const isTheftIssue = form.ticketType?.key?.toUpperCase() === "THEFT";
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [submittedResponse, setSubmittedResponse] =
    useState<CreateIncidentResponse | null>(null);

  const districtOptions = useMemo(
    () =>
      (boundaryData?.districts ?? [])
        .map((node) => toBoundaryOption(node, t))
        .sort((a, b) => a.name.localeCompare(b.name)),
    [boundaryData, t],
  );

  const blockOptions = useMemo(() => {
    if (!form.district) {
      return [];
    }
    return (boundaryData?.blocks ?? [])
      .filter((node) => node.parentCode === form.district!.code)
      .map((node) => toBoundaryOption(node, t))
      .sort((a, b) => a.name.localeCompare(b.name));
  }, [boundaryData, form.district, t]);

  const facilityOptions = useMemo(() => {
    if (!form.block) {
      return [];
    }
    return (boundaryData?.facilities ?? [])
      .filter((node) => node.parentCode === form.block!.code)
      .map((node) => toBoundaryOption(node, t))
      .sort((a, b) => a.name.localeCompare(b.name));
  }, [boundaryData, form.block, t]);

  useEffect(() => {
    if (!accessToken) {
      setTicketTypeMenu([]);
      return;
    }
    void fetchTicketTypeMenu(accessToken, user, t).then(setTicketTypeMenu);
  }, [accessToken, t, user]);

  useEffect(() => {
    if (!accessToken) {
      setSystemFunctionalMenu([]);
      return;
    }
    void fetchSystemFunctionalityOptions(accessToken, user, t).then(setSystemFunctionalMenu);
  }, [accessToken, t, user]);

  useEffect(() => {
    if (!form.ticketType?.key || !accessToken) {
      setTicketSubTypeMenu([]);
      return;
    }

    void fetchServiceDefsForMenuPath(accessToken, user, form.ticketType.key, t).then(
      (types) => {
        setTicketSubTypeMenu(
          types.map((item) => ({
            code: item.key,
            key: item.key,
            serviceCode: item.serviceCode,
            menuPath: item.menuPath,
            name: item.name,
          })),
        );
      },
    );
  }, [accessToken, form.ticketType?.key, t, user]);

  useEffect(() => {
    if (!form.facility?.code || !form.ticketType?.key) {
      setDuplicateTickets([]);
      return;
    }

    const jurisdiction = boundaries ?? { country: ["-"] };
    void searchPotentialDuplicates(
      employeeTenantId!,
      jurisdiction,
      form.facility.code,
      form.ticketType.key,
      accessToken!,
      user,
    ).then(setDuplicateTickets);
  }, [
    accessToken,
    boundaries,
    employeeTenantId,
    form.facility,
    form.ticketType,
    user,
  ]);

  const uploadFiles = useCallback(
    async (files: FileList, kind: UploadedMediaEntry["kind"]) => {
      if (!accessToken || !employeeTenantId) {
        return;
      }

      const fileArray = Array.from(files);
      const existingCount =
        kind === "image" ? imageUploads.length : kind === "video" ? videoUploads.length : firUploads.length;
      const validationError = validateMediaFiles(fileArray, existingCount, kind);

      if (validationError) {
        setFieldErrors((prev) => ({
          ...prev,
          [kind]: buildMediaErrorMessage(t, kind, validationError),
        }));
        return;
      }
      setFieldErrors((prev) => ({ ...prev, [kind]: undefined }));

      const setUploading =
        kind === "image" ? setIsImageUploading : kind === "video" ? setIsVideoUploading : setIsFirUploading;
      const setUploads = kind === "image" ? setImageUploads : kind === "video" ? setVideoUploads : setFirUploads;

      setUploading(true);
      try {
        const uploaded: UploadedMediaEntry[] = [];
        for (const file of fileArray) {
          const result =
            kind === "video"
              ? await uploadIncidentVideo(file, employeeTenantId, accessToken)
              : await uploadIncidentFile(file, employeeTenantId, accessToken);
          uploaded.push({
            file,
            fileStoreId: result.fileStoreId,
            masterFileStoreId: result.masterFileStoreId,
            kind,
          });
        }
        setUploads((prev) => [...prev, ...uploaded]);
      } finally {
        setUploading(false);
      }
    },
    [accessToken, employeeTenantId, firUploads.length, imageUploads.length, t, videoUploads.length],
  );

  const removeUpload = useCallback(
    (kind: UploadedMediaEntry["kind"], fileStoreId: string) => {
      const setUploads = kind === "image" ? setImageUploads : kind === "video" ? setVideoUploads : setFirUploads;
      setUploads((prev) => prev.filter((item) => item.fileStoreId !== fileStoreId));
    },
    [],
  );

  const validate = useCallback(() => {
    const errors: FieldErrors = {};
    if (!form.district) {
      errors.district = t("INCIDENT_DISTRICT_REQUIRED");
    }
    if (!form.block) {
      errors.block = t("INCIDENT_BLOCK_REQUIRED");
    }
    if (!form.facility) {
      errors.facility = t("INCIDENT_FACILITY_REQUIRED");
    }
    if (!form.ticketType) {
      errors.ticketType = t("INCIDENT_TICKET_TYPE_REQUIRED");
    }
    if (!form.ticketSubType) {
      errors.ticketSubType = t("INCIDENT_TICKET_SUBTYPE_REQUIRED");
    }
    if (!form.systemFunctional) {
      errors.systemFunctional = t("INCIDENT_SYSTEM_FUNCTIONAL_REQUIRED");
    }
    if (form.comments.length > MAX_COMMENT_LENGTH) {
      errors.comments = t("CS_LENGTH_EXCEED").replace("{MAX_COUNT}", String(MAX_COMMENT_LENGTH));
    }
    // Matches DIGIT-UI's `hasMandatoryTheftUpload` check on submit.
    if (isTheftIssue && firUploads.length === 0) {
      errors.fir = t("INCIDENT_PLEASE_UPLOAD_FIR_POLICE_LETTER");
    }
    setFieldErrors((prev) => ({ ...prev, ...errors }));
    return Object.keys(errors).length === 0;
  }, [firUploads.length, form, isTheftIssue, t]);

  const canSubmit = useMemo(() => {
    return Boolean(
      form.district &&
        form.block &&
        form.facility &&
        form.ticketType &&
        form.ticketSubType &&
        form.systemFunctional &&
        (!isTheftIssue || firUploads.length > 0) &&
        !isImageUploading &&
        !isVideoUploading &&
        !isFirUploading,
    );
  }, [firUploads.length, form, isFirUploading, isImageUploading, isTheftIssue, isVideoUploading]);

  const createMutation = useMutation({
    mutationFn: async () => {
      if (!user?.uuid || !accessToken || !employeeTenantId) {
        throw new Error("AUTH_REQUIRED");
      }
      if (!validate()) {
        throw new Error("VALIDATION_FAILED");
      }

      const uploadedDocuments = buildUploadedDocuments([
        ...imageUploads,
        ...videoUploads,
        ...firUploads,
      ]);

      return createIncident({
        tenantId: employeeTenantId,
        district: form.district!,
        block: form.block!,
        facility: form.facility!,
        ticketType: form.ticketType!,
        ticketSubType: form.ticketSubType!,
        systemFunctional: form.systemFunctional!,
        comments: form.comments,
        uploadedDocuments,
        user,
        accessToken,
      });
    },
    onSuccess: async (response) => {
      if (!response?.IncidentWrappers) {
        const message =
          response?.Errors?.[0]?.message ??
          response?.message ??
          t("CS_COMMON_SOMETHING_WENT_WRONG");
        setSubmitError(message);
        return;
      }
      sessionStorage.removeItem(DRAFT_STORAGE_KEY);
      await queryClient.invalidateQueries({ queryKey: ["im-inbox"] });
      await queryClient.invalidateQueries({ queryKey: ["im-inbox-summary"] });
      setSubmittedResponse(response);
    },
  });

  const clearForm = useCallback(() => {
    setForm(EMPTY_FORM);
    setFieldErrors({});
    setImageUploads([]);
    setVideoUploads([]);
    setFirUploads([]);
    setTicketSubTypeMenu([]);
    sessionStorage.removeItem(DRAFT_STORAGE_KEY);
  }, []);

  const saveDraft = useCallback(() => {
    sessionStorage.setItem(
      DRAFT_STORAGE_KEY,
      JSON.stringify({
        form,
        imageUploads: imageUploads.map((item) => item.fileStoreId),
        videoUploads: videoUploads.map((item) => item.fileStoreId),
        firUploads: firUploads.map((item) => item.fileStoreId),
      }),
    );
  }, [firUploads, form, imageUploads, videoUploads]);

  useEffect(() => {
    const raw = sessionStorage.getItem(DRAFT_STORAGE_KEY);
    if (!raw) {
      return;
    }
    try {
      const draft = JSON.parse(raw) as { form?: CreateIncidentFormValues };
      if (draft.form) {
        setForm({ ...EMPTY_FORM, ...draft.form });
      }
    } catch {
      sessionStorage.removeItem(DRAFT_STORAGE_KEY);
    }
  }, []);

  const updateField = <K extends keyof CreateIncidentFormValues>(
    key: K,
    value: CreateIncidentFormValues[K],
  ) => {
    setForm((prev) => ({ ...prev, [key]: value }));
    setFieldErrors((prev) => ({ ...prev, [key]: undefined }));
  };

  const handleDistrictChange = (district: BoundaryOption | null) => {
    setForm((prev) => ({ ...prev, district, block: null, facility: null }));
    setFieldErrors((prev) => ({ ...prev, district: undefined, block: undefined, facility: undefined }));
  };

  const handleBlockChange = (block: BoundaryOption | null) => {
    setForm((prev) => ({ ...prev, block, facility: null }));
    setFieldErrors((prev) => ({ ...prev, block: undefined, facility: undefined }));
  };

  const handleFacilityChange = (facility: BoundaryOption | null) => {
    updateField("facility", facility);
  };

  const handleTicketTypeChange = (ticketType: SelectOption | null) => {
    setForm((prev) => ({ ...prev, ticketType, ticketSubType: null }));
    setFieldErrors((prev) => ({ ...prev, ticketType: undefined, ticketSubType: undefined }));
  };

  const handleTicketSubTypeChange = (ticketSubType: SelectOption | null) => {
    updateField("ticketSubType", ticketSubType);
  };

  const handleSystemFunctionalChange = (systemFunctional: SelectOption | null) => {
    updateField("systemFunctional", systemFunctional);
  };

  return {
    t,
    form,
    updateField,
    fieldErrors,
    districtOptions,
    blockOptions,
    facilityOptions,
    ticketTypeMenu,
    ticketSubTypeMenu,
    systemFunctionalMenu,
    isBoundaryLoading,
    imageUploads,
    videoUploads,
    firUploads,
    uploadFiles,
    removeUpload,
    isImageUploading,
    isVideoUploading,
    isFirUploading,
    isTheftIssue,
    duplicateTickets,
    setDuplicateTickets,
    canSubmit,
    submitError,
    setSubmitError,
    createMutation,
    clearForm,
    saveDraft,
    validate,
    inboxPath,
    submittedResponse,
    handleDistrictChange,
    handleBlockChange,
    handleFacilityChange,
    handleTicketTypeChange,
    handleTicketSubTypeChange,
    handleSystemFunctionalChange,
    maxImageCount: MAX_IMAGE_COUNT,
    maxImageSizeMb: MAX_IMAGE_SIZE_MB,
    maxVideoCount: MAX_VIDEO_COUNT,
    maxVideoSizeMb: MAX_VIDEO_SIZE_MB,
    maxFirCount: MAX_FIR_COUNT,
    maxFirSizeMb: MAX_FIR_SIZE_MB,
    maxCommentLength: MAX_COMMENT_LENGTH,
  };
}
