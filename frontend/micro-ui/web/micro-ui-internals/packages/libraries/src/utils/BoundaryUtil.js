
export const BoundaryUtil = {

  aggregateBoundaryCodes: (boundaries) => {
    const boundaryCodes = [];
    if (boundaries) {
      Object.values(boundaries).forEach((value) => {
        boundaryCodes.push(...(value || []));
      })
    }

    return [...new Set(boundaryCodes)];
  },

  aggregateBoundaryTypes: (boundaries) => {
    const boundaryTypes = [];
    if (boundaries) {
      Object.keys(boundaries).forEach((key) => {
        boundaryTypes.push(key);
      })
    }

    return boundaryTypes;
  }

}