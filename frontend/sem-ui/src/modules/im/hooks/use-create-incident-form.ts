import {
  aggregateBoundaryCodes,
  translateOr,
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

function buildMediaErrorMessage(
  t: (key: string) => string,
  kind: MediaKind,
  error: MediaValidationError,
): string {
  const maxCount = kind === "image" ? MAX_IMAGE_COUNT : MAX_VIDEO_COUNT;
  const maxSizeMb = kind === "image" ? MAX_IMAGE_SIZE_MB : MAX_VIDEO_SIZE_MB;
  const formats = kind === "image" ? "JPG, JPEG, PNG" : "MP4, MOV, AVI, WMV";

  if (error.code === "COUNT") {
    return kind === "image"
      ? translateOr(
          t,
          "INCIDENT_IMAGE_COUNT_EXCEEDED",
          "You can upload up to {MAX_COUNT} images",
        ).replace("{MAX_COUNT}", String(maxCount))
      : translateOr(
          t,
          "INCIDENT_VIDEO_COUNT_EXCEEDED",
          "You can upload up to {MAX_COUNT} videos",
        ).replace("{MAX_COUNT}", String(maxCount));
  }

  if (error.code === "SIZE") {
    return kind === "image"
      ? translateOr(
          t,
          "INCIDENT_IMAGE_SIZE_EXCEEDED",
          `Each image must be ${maxSizeMb}MB or smaller`,
        )
      : translateOr(
          t,
          "INCIDENT_VIDEO_SIZE_EXCEEDED",
          `Each video must be ${maxSizeMb}MB or smaller`,
        );
  }

  return kind === "image"
    ? translateOr(
        t,
        "INCIDENT_IMAGE_FORMAT_INVALID",
        `Only ${formats} formats are supported`,
      )
    : translateOr(
        t,
        "INCIDENT_VIDEO_FORMAT_INVALID",
        `Only ${formats} formats are supported`,
      );
}

function toBoundaryOption(
  node: { code: string; parentCode: string },
  t: (key: string) => string,
): BoundaryOption {
  return {
    code: node.code,
    parentCode: node.parentCode,
    name: translateOr(t, `Boundary_${node.code}`, node.code),
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
  const [isImageUploading, setIsImageUploading] = useState(false);
  const [isVideoUploading, setIsVideoUploading] = useState(false);
  const [duplicateTickets, setDuplicateTickets] = useState<
    Array<{ ticketId: string; ticketTenantId: string }>
  >([]);
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
      if (kind !== "image" && kind !== "video") {
        return;
      }

      const fileArray = Array.from(files);
      const existingCount =
        kind === "image" ? imageUploads.length : videoUploads.length;
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
        kind === "image" ? setIsImageUploading : setIsVideoUploading;
      const setUploads = kind === "image" ? setImageUploads : setVideoUploads;

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
    [accessToken, employeeTenantId, imageUploads.length, t, videoUploads.length],
  );

  const removeUpload = useCallback(
    (kind: "image" | "video", fileStoreId: string) => {
      const setUploads = kind === "image" ? setImageUploads : setVideoUploads;
      setUploads((prev) => prev.filter((item) => item.fileStoreId !== fileStoreId));
    },
    [],
  );

  const validate = useCallback(() => {
    const errors: FieldErrors = {};
    if (!form.district) {
      errors.district = translateOr(t, "INCIDENT_DISTRICT_REQUIRED", "Please select a district");
    }
    if (!form.block) {
      errors.block = translateOr(t, "INCIDENT_BLOCK_REQUIRED", "Please select a block");
    }
    if (!form.facility) {
      errors.facility = translateOr(t, "INCIDENT_FACILITY_REQUIRED", "Please select a facility");
    }
    if (!form.ticketType) {
      errors.ticketType = translateOr(
        t,
        "INCIDENT_TICKET_TYPE_REQUIRED",
        "Please select a ticket type",
      );
    }
    if (!form.ticketSubType) {
      errors.ticketSubType = translateOr(
        t,
        "INCIDENT_TICKET_SUBTYPE_REQUIRED",
        "Please select a ticket subtype",
      );
    }
    if (!form.systemFunctional) {
      errors.systemFunctional = translateOr(
        t,
        "INCIDENT_SYSTEM_FUNCTIONAL_REQUIRED",
        "Please select whether the solar system is working",
      );
    }
    if (form.comments.length > MAX_COMMENT_LENGTH) {
      errors.comments = translateOr(
        t,
        "INCIDENT_COMMENTS_MAX_LENGTH",
        "Comments cannot exceed {MAX_COUNT} characters.",
      ).replace("{MAX_COUNT}", String(MAX_COMMENT_LENGTH));
    }
    setFieldErrors((prev) => ({ ...prev, ...errors }));
    return Object.keys(errors).length === 0;
  }, [form, t]);

  const canSubmit = useMemo(() => {
    return Boolean(
      form.district &&
        form.block &&
        form.facility &&
        form.ticketType &&
        form.ticketSubType &&
        form.systemFunctional &&
        !isImageUploading &&
        !isVideoUploading,
    );
  }, [form, isImageUploading, isVideoUploading]);

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
          translateOr(t, "CS_COMMON_SOMETHING_WENT_WRONG", "Something went wrong!");
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
      }),
    );
  }, [form, imageUploads, videoUploads]);

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
    translateOr,
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
    uploadFiles,
    removeUpload,
    isImageUploading,
    isVideoUploading,
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
    maxCommentLength: MAX_COMMENT_LENGTH,
  };
}
