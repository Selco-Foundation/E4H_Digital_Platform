// Dummy data source for the Assessment Plan feature until the backend entity exists.

const DEFAULT_SUMMARY = {
  totalFacilities: 10,
  remoteAssessmentsCompleted: 7,
  onSiteAssessmentsCompleted: 0,
  eligibleCount: 2,
  notEligibleCount: 2,
};

export const DUMMY_ASSESSMENT_PLANS = [
  { id: "1", name: "GU-INS-2026-1", startDate: "2026-06-18", endDate: "2026-07-01", numberOfFacilities: 57, status: "DRAFT", summary: DEFAULT_SUMMARY },
  { id: "2", name: "GU-INS-2026-2", startDate: "2026-07-05", endDate: "2026-07-20", numberOfFacilities: 45, status: "SCHEDULED", summary: DEFAULT_SUMMARY },
  { id: "3", name: "GU-INS-2026-3", startDate: "2026-07-25", endDate: "2026-08-10", numberOfFacilities: 30, status: "COMPLETED", summary: DEFAULT_SUMMARY },
  { id: "4", name: "GU-INS-2026-4", startDate: "2026-08-15", endDate: "2026-08-30", numberOfFacilities: 60, status: "DRAFT", summary: DEFAULT_SUMMARY },
];

export const DUMMY_ASSESSMENT_FACILITIES = [
  { id: "1", name: "PHC Bundu", facilityType: "Primary Health Center", district: "Ranchi", block: "Bundu", remoteStatus: "QUALIFIED", onSiteStatus: "NOT_INITIATED", result: "PENDING" },
  { id: "2", name: "SC Tamar", facilityType: "Sub Center", district: "Ranchi", block: "Tamar", remoteStatus: "QUALIFIED", onSiteStatus: "PENDING", result: "PENDING" },
  { id: "3", name: "PHC Khunti", facilityType: "Block Primary Health Center", district: "Khunti", block: "Khunti", remoteStatus: "QUALIFIED", onSiteStatus: "QUALIFIED", result: "ELIGIBLE" },
  { id: "4", name: "Anganwadi-12", facilityType: "Health and Wellness Center", district: "Ranchi", block: "Bundu", remoteStatus: "PENDING", onSiteStatus: "NOT_INITIATED", result: "PENDING" },
  { id: "5", name: "SC Murhu", facilityType: "Community Health Center", district: "Khunti", block: "Murhu", remoteStatus: "QUALIFIED", onSiteStatus: "QUALIFIED", result: "NOT_ELIGIBLE" },
  { id: "6", name: "SC Silli", facilityType: "Dispensary", district: "Ranchi", block: "Silli", remoteStatus: "PENDING", onSiteStatus: "NOT_INITIATED", result: "PENDING" },
  { id: "7", name: "District Hospital Dhanbad", facilityType: "District Hospital", district: "Dhanbad", block: "Dhanbad", remoteStatus: "QUALIFIED", onSiteStatus: "QUALIFIED", result: "ELIGIBLE" },
  { id: "8", name: "Anganwadi-Tathibandh", facilityType: "Health and Wellness Center", district: "Dhanbad", block: "Tathibandh", remoteStatus: "PENDING", onSiteStatus: "NOT_INITIATED", result: "PENDING" },
  { id: "9", name: "PHC Ormanjhi", facilityType: "Primary Health Center", district: "Ranchi", block: "Ormanjhi", remoteStatus: "QUALIFIED", onSiteStatus: "PENDING", result: "PENDING" },
  { id: "10", name: "SC Jharia", facilityType: "Sub Center", district: "Dhanbad", block: "Jharia", remoteStatus: "QUALIFIED", onSiteStatus: "QUALIFIED", result: "NOT_ELIGIBLE" },
];

export const ASSESSMENT_FACILITY_STATUS_CODES = ["QUALIFIED", "PENDING", "NOT_INITIATED"];

export const ASSESSMENT_RESULT_CODES = ["ELIGIBLE", "NOT_ELIGIBLE", "PENDING"];
