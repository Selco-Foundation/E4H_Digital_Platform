import { contextPath, translateOr, useTranslate } from "@/shared";
import { Link } from "@tanstack/react-router";
import type { PausedFacility } from "../../types/rms";

function formatDate(isoInstant?: string): string {
  if (!isoInstant) {
    return "-";
  }
  const date = new Date(isoInstant);
  return Number.isNaN(date.getTime())
    ? "-"
    : date.toLocaleDateString("en-GB", { day: "2-digit", month: "2-digit", year: "numeric" });
}

interface RmsPausedTableProps {
  facilities: PausedFacility[];
  isLoading: boolean;
}

/** Matches DIGIT-UI's RMSPausedDesktop.js columns exactly. */
export function RmsPausedTable({ facilities, isLoading }: RmsPausedTableProps) {
  const { t } = useTranslate();
  const basePath = `/${contextPath()}`;

  if (isLoading) {
    return (
      <div className="livelihood-card p-6 text-sm text-muted-foreground">
        {translateOr(t, "CORE_COMMON_LOADING", "Loading...")}
      </div>
    );
  }

  if (!facilities.length) {
    return (
      <div className="livelihood-card p-6 text-center text-sm text-muted-foreground">
        {translateOr(t, "RMS_NO_PAUSED_FACILITIES_FOUND", "No RMS Paused Facilities found")}
      </div>
    );
  }

  return (
    <div className="livelihood-card overflow-x-auto">
      <table className="w-full text-left text-sm">
        <thead className="border-b border-border text-xs font-semibold text-muted-foreground uppercase">
          <tr>
            <th className="px-4 py-3">{translateOr(t, "RMS_FACILITY_NAME", "Facility Name")}</th>
            <th className="px-4 py-3">{translateOr(t, "RMS_FACILITY_ID", "Facility ID")}</th>
            <th className="px-4 py-3">{translateOr(t, "RMS_PAUSED_UNTIL", "Paused Until")}</th>
            <th className="px-4 py-3">{translateOr(t, "RMS_PAUSED_BY", "Paused By")}</th>
          </tr>
        </thead>
        <tbody>
          {facilities.map((facility, index) => (
            <tr key={`${facility.facilityId}-${index}`} className="border-b border-border last:border-0">
              <td className="px-4 py-3">
                <Link
                  to={`${basePath}/employee/im/pause-rms`}
                  search={{ facilityId: facility.facilityId }}
                  className="font-medium text-destructive hover:underline"
                >
                  {translateOr(t, `Boundary_${facility.boundaryCode}`, facility.boundaryCode ?? "-")}
                </Link>
              </td>
              <td className="px-4 py-3">{facility.facilityId ?? "-"}</td>
              <td className="px-4 py-3">{formatDate(facility.pausedUntil)}</td>
              <td className="px-4 py-3">{facility.pausedBy ?? "-"}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
