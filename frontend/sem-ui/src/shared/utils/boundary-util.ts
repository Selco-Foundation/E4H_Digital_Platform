export type JurisdictionBoundaries = Record<string, string[]>;

export function aggregateBoundaryCodes(boundaries: JurisdictionBoundaries | null | undefined): string[] {
  if (!boundaries) {
    return [];
  }

  return Object.values(boundaries).flatMap((codes) => codes ?? []);
}

export function aggregateBoundaryTypes(boundaries: JurisdictionBoundaries | null | undefined): string[] {
  if (!boundaries) {
    return [];
  }

  return Object.keys(boundaries);
}

export function buildJurisdictionBoundaries(
  jurisdictions: Array<{ boundaryType?: string; boundary?: string }> | undefined,
): JurisdictionBoundaries {
  const jurisdictionBoundaries: JurisdictionBoundaries = {};

  for (const jurisdiction of jurisdictions ?? []) {
    if (!jurisdiction.boundaryType || !jurisdiction.boundary) {
      continue;
    }

    const key = jurisdiction.boundaryType.toLowerCase();
    jurisdictionBoundaries[key] = [...(jurisdictionBoundaries[key] ?? []), jurisdiction.boundary];
  }

  return jurisdictionBoundaries;
}
