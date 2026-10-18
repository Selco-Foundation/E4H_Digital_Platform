/**
 * A State -> District -> Block selection held as code arrays, plus the helpers the bulk-add
 * geography step needs. `boundary` is `useBoundary()`'s data: `{states, districts, blocks}`,
 * each a list of `{code, parentCode}`.
 */
export const EMPTY_GEOGRAPHY = { states: [], districts: [], blocks: [] };

const parentMap = (nodes) => new Map((nodes || []).map((node) => [node.code, node.parentCode]));

/** Drops districts whose state and blocks whose district are no longer selected. */
export const pruneGeography = (geography, boundary) => {
  const districtParent = parentMap(boundary?.districts);
  const blockParent = parentMap(boundary?.blocks);
  const states = new Set(geography.states);
  const districts = geography.districts.filter((code) => states.has(districtParent.get(code)));
  const keptDistricts = new Set(districts);
  const blocks = geography.blocks.filter((code) => keptDistricts.has(blockParent.get(code)));
  return { states: geography.states, districts, blocks };
};

/**
 * The selection as the `boundary_data` tree `/template/facilityIngestion` takes
 * (country -> state -> district -> block). Only branches ending in a selected block are kept.
 * The server reads each node's `boundaryCode`; `name` carries the code too.
 */
export const buildBoundaryDataTree = (geography, boundary, countryCode = "India") => {
  const pruned = pruneGeography(geography, boundary);
  const districtParent = parentMap(boundary?.districts);
  const blockParent = parentMap(boundary?.blocks);

  const blocksByDistrict = new Map();
  pruned.blocks.forEach((block) => {
    const district = blockParent.get(block);
    blocksByDistrict.set(district, [...(blocksByDistrict.get(district) || []), block]);
  });
  const districtsByState = new Map();
  pruned.districts
    .filter((district) => blocksByDistrict.has(district))
    .forEach((district) => {
      const state = districtParent.get(district);
      districtsByState.set(state, [...(districtsByState.get(state) || []), district]);
    });

  return {
    boundaryCode: countryCode,
    type: "country",
    name: countryCode,
    children: pruned.states
      .filter((state) => districtsByState.has(state))
      .map((state) => ({
        boundaryCode: state,
        type: "state",
        name: state,
        children: districtsByState.get(state).map((district) => ({
          boundaryCode: district,
          type: "district",
          name: district,
          children: blocksByDistrict.get(district).map((block) => ({
            boundaryCode: block,
            type: "block",
            name: block,
          })),
        })),
      })),
  };
};
