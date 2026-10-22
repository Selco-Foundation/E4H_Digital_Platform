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

/** Codes of every node in `nodes` whose parent is `parentCode`. */
const childrenOf = (nodes, parentCode) =>
  (nodes || []).filter((node) => node.parentCode === parentCode).map((node) => node.code);

/**
 * The selection as the fewest boundary codes that cover the same blocks, for
 * `/template/facilityIngestion`'s `boundary_codes` field: a district whose every block is covered
 * collapses to the district code, and a state whose every district is covered to the state code.
 * Same meaning as `buildBoundaryDataTree` - a level picked on its own means all of it - but the
 * whole country is a few dozen codes instead of a tree of every block, which the gateway's
 * request-size limit rejects (HTTP 413).
 */
export const buildBoundaryCodeSelection = (geography, boundary) => {
  const tree = buildBoundaryDataTree(geography, boundary);
  const codes = [];
  (tree.children || []).forEach((state) => {
    const covered = [];
    const partial = [];
    (state.children || []).forEach((district) => {
      const blocks = (district.children || []).map((block) => block.boundaryCode);
      if (!blocks.length) return;
      if (blocks.length === childrenOf(boundary?.blocks, district.boundaryCode).length) covered.push(district.boundaryCode);
      else partial.push(...blocks);
    });
    // Districts with no blocks never reach the template, so they don't stop a state collapsing.
    const districtsWithBlocks = childrenOf(boundary?.districts, state.boundaryCode).filter(
      (district) => childrenOf(boundary?.blocks, district).length > 0
    );
    if (!partial.length && covered.length && covered.length === districtsWithBlocks.length) codes.push(state.boundaryCode);
    else codes.push(...covered, ...partial);
  });
  return codes;
};

/**
 * The selection as the `boundary_data` tree `/template/facilityIngestion` takes
 * (country -> state -> district -> block). The server reads each node's `boundaryCode`;
 * `name` carries the code too.
 *
 * Picking a level means "all of it": a state with no district picked under it expands to every
 * district in that state, and a district with no block picked expands to every block in it. So
 * selecting only a state yields that whole state, which is what makes the template downloadable
 * before any district or block is chosen. Narrowing by district or block is a refinement, not a
 * requirement.
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
  pruned.districts.forEach((district) => {
    const state = districtParent.get(district);
    districtsByState.set(state, [...(districtsByState.get(state) || []), district]);
  });

  return {
    boundaryCode: countryCode,
    type: "country",
    name: countryCode,
    children: pruned.states.map((state) => ({
      boundaryCode: state,
      type: "state",
      name: state,
      children: (districtsByState.get(state) || childrenOf(boundary?.districts, state)).map((district) => ({
        boundaryCode: district,
        type: "district",
        name: district,
        children: (blocksByDistrict.get(district) || childrenOf(boundary?.blocks, district)).map((block) => ({
          boundaryCode: block,
          type: "block",
          name: block,
        })),
      })),
    })),
  };
};
