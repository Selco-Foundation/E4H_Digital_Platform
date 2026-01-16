import { CustomRequest } from "../components/Custom/CustomRequest";

const getHeader = (headers, key) => {
  if (!headers) return "";
  return headers[key] || headers[key?.toLowerCase?.()] || headers[key?.toUpperCase?.()] || "";
};

const parseFilenameFromDisposition = (disposition = "") => {
  let filename = "";
  const utf8Match = /filename\*\s*=\s*UTF-8''([^;]+)/i.exec(disposition);
  if (utf8Match && utf8Match[1]) filename = decodeURIComponent(utf8Match[1].replace(/"/g, ""));

  if (!filename) {
    const asciiMatch = /filename\s*=\s*"?([^"]+)"?/i.exec(disposition);
    if (asciiMatch && asciiMatch[1]) filename = asciiMatch[1];
  }
  return filename;
};

const ensureXlsxExt = (name) => {
  if (!name) return "boundary-upload-result.xlsx";
  const n = String(name);
  return n.toLowerCase().endsWith(".xlsx") || n.toLowerCase().endsWith(".xls") || n.toLowerCase().endsWith(".csv") ? n : `${n}.xlsx`;
};

const isLikelyExcel = (contentType = "") => {
  const ct = String(contentType).toLowerCase();
  return ct.includes("spreadsheetml") || ct.includes("ms-excel") || ct.includes("excel");
};

const isJsonString = (s) => {
  if (typeof s !== "string") return false;
  const trimmed = s.trim();
  if (!trimmed) return false;
  if (!(trimmed.startsWith("{") || trimmed.startsWith("["))) return false;
  try {
    JSON.parse(trimmed);
    return true;
  } catch {
    return false;
  }
};

const looksLikeJsonBlob = async (blob) => {
  try {
    if (typeof Blob === "undefined" || !(blob instanceof Blob)) return false;
    const slice = blob.slice(0, 2048);
    const txt = (await slice.text())?.trim?.() || "";
    return txt.startsWith("{") || txt.startsWith("[");
  } catch {
    return false;
  }
};

const tryParseJsonFromAny = async (payload) => {
  try {
    if (!payload) return null;

    if (typeof Blob !== "undefined" && payload instanceof Blob) {
      const ok = await looksLikeJsonBlob(payload);
      if (!ok) return null;
      const text = await payload.text();
      return JSON.parse(text);
    }

    if (payload instanceof ArrayBuffer) {
      const preview = new TextDecoder("utf-8").decode(new Uint8Array(payload.slice(0, 2048))).trim();
      if (!(preview.startsWith("{") || preview.startsWith("["))) return null;

      const text = new TextDecoder("utf-8").decode(new Uint8Array(payload));
      return JSON.parse(text);
    }

    if (typeof payload === "string") {
      if (!isJsonString(payload)) return null;
      return JSON.parse(payload);
    }

    if (typeof payload === "object") return payload;

    return null;
  } catch {
    return null;
  }
};

const buildRequestInfo = () => {
  const user = Digit && Digit.UserService && Digit.UserService.getUser ? Digit.UserService.getUser() : null;
  const locale =
    Digit && Digit.Utils && Digit.Utils.locale && Digit.Utils.locale.getDefaultLocale ? Digit.Utils.locale.getDefaultLocale() : "en_IN";

  return {
    apiId: "Rainmaker",
    authToken: user && user.access_token,
    userInfo: user && user.info,
    msgId: `${Date.now()}|${locale}`,
    plainAccessRequest: {},
  };
};

const ensureRequestInfoInFormData = (fd) => {
  if (!fd || typeof fd.append !== "function") return fd;

  const getVal = (k) => (typeof fd.get === "function" ? fd.get(k) : null);

  const existingReqInfo = getVal("request_info");
  if (existingReqInfo) return fd;

  const existingBody = getVal("request_body");

  if (existingBody && isJsonString(existingBody)) {
    fd.append("request_info", existingBody);
    if (typeof fd.delete === "function") fd.delete("request_body");
    return fd;
  }

  fd.append("request_info", JSON.stringify(buildRequestInfo()));
  if (typeof fd.delete === "function") fd.delete("request_body");
  return fd;
};

export const IngestionService = {
  downloadBoundaryIngestionTemplate: async (requestData) => {
    const endpoint = "/ingestion-service/template/boundaryIngestionTemplate";
    const headers = {
      "Content-Type": "application/json",
      Accept: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet, application/octet-stream",
    };

    const res = await CustomRequest({
      url: endpoint,
      data: requestData || {},
      userService: true,
      method: "POST",
      auth: true,
      attachAuthHeaders: true,
      headers: headers,
      responseType: "arraybuffer",
    });

    const buffer = res && res.data ? res.data : res;
    const respHeaders = res && res.headers ? res.headers : {};

    const contentType =
      (respHeaders["content-type"] || respHeaders["Content-Type"]) ||
      "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    const disposition = respHeaders["content-disposition"] || respHeaders["Content-Disposition"] || "";
    const filename = parseFilenameFromDisposition(disposition) || "boundary-ingestion-template.xlsx";

    const blob = new Blob([buffer], { type: contentType });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    a.remove();

    setTimeout(() => window.URL.revokeObjectURL(url), 1000);
    return true;
  },

  uploadBoundaryDataAndGetDisplayFile: async ({ boundaryFile, boundarySheetName, fallbackFileName }) => {
    const endpoint = "/ingestion-service/ingest/boundaries";

    const formData = new FormData();
    formData.append("boundary_file", boundaryFile);

    formData.append("boundary_sheet_name", boundarySheetName || "Boundary Data");

    const fd = ensureRequestInfoInFormData(formData);

    const res = await CustomRequest({
      url: endpoint,
      data: fd,
      userService: true,
      method: "POST",
      auth: true,
      attachAuthHeaders: true,
      responseType: "blob",
    });

    const payload = res && res.data ? res.data : res;
    const headers = res && res.headers ? res.headers : {};

    const contentType =
      getHeader(headers, "content-type") ||
      (typeof Blob !== "undefined" && payload instanceof Blob ? payload.type : "") ||
      "application/octet-stream";

    // Parse JSON only if it really looks like JSON (prevents reading excel blobs as text)
    const parsed = await tryParseJsonFromAny(payload);
    if (parsed && typeof parsed === "object" && !isLikelyExcel(contentType)) return parsed;

    const disposition = getHeader(headers, "content-disposition");
    const headerName = parseFilenameFromDisposition(disposition);
    const finalName = ensureXlsxExt(headerName || fallbackFileName || "boundary-upload-result.xlsx");

    const blob =
      typeof Blob !== "undefined" && payload instanceof Blob ? payload : new Blob([payload], { type: contentType });

    const fileObj = new File([blob], finalName, { type: contentType });

    return {
      file: { name: finalName, data: fileObj },
    };
  },

  downloadFacilityDataTemplate: async () => {
    const endpoint = "/ingestion-service/template/facilityIngestion";
    const headers = {
      "Content-Type": "application/json"
    }

    await CustomRequest({
      url: endpoint,
      userService: true,
      data: new FormData(),
      method: "POST",
      auth: true,
      headers: headers,
      attachAuthHeaders: true,
      fileDownload: true,
      responseType: "blob",
      attachRequestInfo: (data, RequestInfo) => {
        data.append("request_info", JSON.stringify(RequestInfo));
      },
      defaultFilename: "download.xlsx",
    });
  },

  uploadFacilityData: async (filledFacilityData) => {
    const endpoint = "/ingestion-service/ingest/facilities";

    return await CustomRequest({
      url: endpoint,
      data: filledFacilityData,
      userService: true,
      method: "POST",
      attachAuthHeaders: true,
      auth: true,
      attachRequestInfo: (data, RequestInfo) => {
        data.append("request_info", JSON.stringify(RequestInfo));
      },
      responseType: "blob",
    });
  },
};