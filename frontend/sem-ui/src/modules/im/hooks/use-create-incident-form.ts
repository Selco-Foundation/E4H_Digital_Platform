import {
  translateOr,
  useAuthStore,
  useJurisdictionStore,
  useTranslate,
} from "@/shared";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useCallback, useEffect, useMemo, useState } from "react";
import { searchAssetsForFacility } from "../services/asset-search";
import { searchFacilitiesByJurisdiction } from "../services/facility-search";
import { uploadIncidentFile, uploadIncidentVideo } from "../services/file-upload";
import { createIncident, searchPotentialDuplicates } from "../services/incident";
import { fetchServiceDefsForMenuPath } from "../services/mdms";
import type {
  CreateIncidentFormValues,
  CreateIncidentResponse,
  SelectOption,
  UploadedMediaEntry,
} from "../types/create-incident";
import type { LivelihoodAsset, LivelihoodFacility } from "../types/facility-asset";
import { buildUploadedDocuments } from "../utils/create-incident-documents";
import { buildFacilitySearchCriteria } from "../utils/jurisdiction-facility-criteria";
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

const DRAFT_STORAGE_KEY = "livelihood-im-create-draft";

interface FieldErrors {
  endUser?: string;
  asset?: string;
  complaintType?: string;
  comments?: string;
  image?: string;
  video?: string;
}

const EMPTY_FORM: CreateIncidentFormValues = {
  endUser: null,
  asset: null,
  complaintType: null,
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

export function useCreateIncidentForm(inboxPath: string) {
  const { t } = useTranslate();
  const queryClient = useQueryClient();
  const user = useAuthStore((state) => state.user);
  const accessToken = useAuthStore((state) => state.accessToken);
  const employeeTenantId = useAuthStore((state) => state.employeeTenantId);
  const boundaries = useJurisdictionStore((state) => state.boundaries);

  const [form, setForm] = useState<CreateIncidentFormValues>(EMPTY_FORM);
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [complaintTypes, setComplaintTypes] = useState<SelectOption[]>([]);
  const [imageUploads, setImageUploads] = useState<UploadedMediaEntry[]>([]);
  const [videoUploads, setVideoUploads] = useState<UploadedMediaEntry[]>([]);
  const [isImageUploading, setIsImageUploading] = useState(false);
  const [isVideoUploading, setIsVideoUploading] = useState(false);
  const [duplicateTickets, setDuplicateTickets] = useState<
    Array<{ ticketId: string; ticketTenantId: string }>
  >([]);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [disableUpload, setDisableUpload] = useState(true);
  const [submittedResponse, setSubmittedResponse] =
    useState<CreateIncidentResponse | null>(null);

  const facilityCriteria = useMemo(
    () =>
      employeeTenantId
        ? buildFacilitySearchCriteria(boundaries, employeeTenantId)
        : null,
    [boundaries, employeeTenantId],
  );

  const facilitiesQuery = useQuery({
    queryKey: ["create-incident-facilities", facilityCriteria, accessToken],
    enabled: Boolean(accessToken && facilityCriteria),
    queryFn: () =>
      searchFacilitiesByJurisdiction(facilityCriteria!, accessToken!, user),
  });

  const facilities = facilitiesQuery.data?.facilities ?? [];
  const showEndUserDropdown = facilities.length !== 1;

  const assetsQuery = useQuery({
    queryKey: [
      "create-incident-assets",
      form.endUser?.facilityId,
      employeeTenantId,
      accessToken,
    ],
    enabled: Boolean(accessToken && employeeTenantId && form.endUser?.facilityId),
    queryFn: () =>
      searchAssetsForFacility(
        form.endUser!.facilityId,
        employeeTenantId!,
        accessToken!,
        user,
      ),
  });

  const assets = assetsQuery.data ?? [];

  const facilityById = useMemo(
    () => new Map(facilities.map((facility) => [facility.facilityId, facility])),
    [facilities],
  );

  const assetById = useMemo(
    () => new Map(assets.map((asset) => [asset.assetId, asset])),
    [assets],
  );

  const endUserOptions = useMemo(
    () =>
      facilities.map((facility) => ({
        code: facility.facilityId,
        name: facility.facilityPocName,
      })),
    [facilities],
  );

  const assetOptions = useMemo(
    () =>
      assets.map((asset) => {
        const assetName = translateOr(
          t,
          `ASSETTYPE_${asset.assetTypeId}`,
          asset.name,
        );
        return {
          code: asset.assetId,
          name: asset.serialNumber
            ? `${assetName} (${asset.serialNumber})`
            : assetName,
        };
      }),
    [assets, t],
  );

  useEffect(() => {
    if (facilities.length !== 1 || form.endUser) {
      return;
    }
    const facility = facilities[0];
    setForm((prev) => ({
      ...prev,
      endUser: facility,
      asset: null,
      complaintType: null,
    }));
    setDisableUpload(false);
  }, [facilities, form.endUser]);

  useEffect(() => {
    if (!form.asset?.assetTypeId || !accessToken) {
      setComplaintTypes([]);
      return;
    }

    void fetchServiceDefsForMenuPath(
      accessToken,
      user,
      form.asset.assetTypeId,
      t,
    ).then((types) => {
      setComplaintTypes(
        types.map((item) => ({
          code: item.key,
          key: item.key,
          serviceCode: item.serviceCode,
          menuPath: item.menuPath,
          name: item.name,
        })),
      );
    });
  }, [accessToken, form.asset?.assetTypeId, t, user]);

  useEffect(() => {
    if (!form.endUser?.facilityId || !form.complaintType?.key) {
      setDuplicateTickets([]);
      return;
    }

    const jurisdiction = boundaries ?? { country: ["-"] };
    void searchPotentialDuplicates(
      employeeTenantId!,
      jurisdiction,
      form.endUser.facilityId,
      form.complaintType.key,
      accessToken!,
      user,
    ).then(setDuplicateTickets);
  }, [
    accessToken,
    boundaries,
    employeeTenantId,
    form.complaintType,
    form.endUser,
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
    if (!form.endUser) {
      errors.endUser = translateOr(
        t,
        "INCIDENT_END_USER_REQUIRED",
        "Please select an end user to continue",
      );
    }
    if (!form.asset) {
      errors.asset = translateOr(
        t,
        "INCIDENT_ASSET_REQUIRED",
        "Please select an asset to continue",
      );
    }
    if (!form.complaintType) {
      errors.complaintType = translateOr(
        t,
        "INCIDENT_TYPE_REQUIRED",
        "Please select an issue type to continue",
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
      form.endUser &&
        form.asset &&
        form.complaintType &&
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
        endUser: form.endUser!,
        asset: form.asset!,
        complaintType: form.complaintType!,
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
    setComplaintTypes([]);
    setDisableUpload(facilities.length !== 1);
    sessionStorage.removeItem(DRAFT_STORAGE_KEY);
    if (facilities.length === 1) {
      setForm({
        ...EMPTY_FORM,
        endUser: facilities[0],
      });
      setDisableUpload(false);
    }
  }, [facilities]);

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

  const handleEndUserChange = (facility: LivelihoodFacility | null) => {
    setForm((prev) => ({
      ...prev,
      endUser: facility,
      asset: null,
      complaintType: null,
    }));
    setFieldErrors((prev) => ({
      ...prev,
      endUser: undefined,
      asset: undefined,
      complaintType: undefined,
    }));
    if (facility) {
      setDisableUpload(false);
    }
  };

  const handleAssetChange = (asset: LivelihoodAsset | null) => {
    setForm((prev) => ({
      ...prev,
      asset,
      complaintType: null,
    }));
    setFieldErrors((prev) => ({
      ...prev,
      asset: undefined,
      complaintType: undefined,
    }));
  };

  const handleComplaintTypeChange = (complaintType: SelectOption | null) => {
    updateField("complaintType", complaintType);
    if (complaintType) {
      setDisableUpload(false);
    }
  };

  return {
    t,
    translateOr,
    form,
    updateField,
    fieldErrors,
    endUserOptions,
    assetOptions,
    facilityById,
    assetById,
    complaintTypes,
    showEndUserDropdown,
    isFacilitiesLoading: facilitiesQuery.isLoading,
    isAssetsLoading: assetsQuery.isLoading,
    imageUploads,
    videoUploads,
    uploadFiles,
    removeUpload,
    isImageUploading,
    isVideoUploading,
    disableUpload,
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
    handleEndUserChange,
    handleAssetChange,
    handleComplaintTypeChange,
    maxImageCount: MAX_IMAGE_COUNT,
    maxImageSizeMb: MAX_IMAGE_SIZE_MB,
    maxVideoCount: MAX_VIDEO_COUNT,
    maxVideoSizeMb: MAX_VIDEO_SIZE_MB,
    maxCommentLength: MAX_COMMENT_LENGTH,
  };
}
