import JSZip from "jszip";

export const getInstallationReportFilename = (facilityName, projectId) => {
  const sanitize = (value) => String(value).replace(/[<>:"/\\|?*\x00-\x1f]/g, "_").trim();
  return `${sanitize(facilityName)}_${sanitize(projectId)}.pdf`;
};

export const createInstallationReportZip = (documents) => {
  const zip = new JSZip();
  const names = new Set();
  for (const { report, data } of documents) {
    const filename = getInstallationReportFilename(report.facilityName, report.projectId);
    // Preserve the required PDF name without overwriting reports sharing that name.
    let path = filename;
    let duplicate = 2;
    while (names.has(path)) path = `${duplicate++}/${filename}`;
    names.add(path);
    zip.file(path, data);
  }
  return zip.generateAsync({ type: "blob" });
};
