import { useQuery, useQueryClient } from "react-query";
import { AssetService } from "../services/Asset";

const getAssetName = (assetTypeID) => {
  switch(assetTypeID) {
    case "PANEL":
      return "Panel";
    case "BATTERY":
      return "Battery";
    case "INVERTER":
      return "Inverter";
  }
}

const getAssetCapacity = (assetTypeID, assetDetails) => {
  switch(assetTypeID) {
    case "PANEL":
      return assetDetails?.panelCapacity + " " + assetDetails?.capacityUnit;
    case "BATTERY":
      return assetDetails?.batteryCapacity + " " + assetDetails?.capacityUnit;
    case "INVERTER":
      return assetDetails?.inverterCapacity + " " + assetDetails?.invertorCapacityUnit;
  }
}

const getAssetVoltage = (assetTypeID, assetDetails) => {
  switch (assetTypeID) {
    case "PANEL":
      return "";
    case "BATTERY":
      return assetDetails?.batteryVoltage + " " + assetDetails?.voltageUnit;
    case "INVERTER":
      return "";
  }
}

const formatData = async (data) => {
  const assets = [];

  for (const row of data) {
    const assetType = row?.assetTypeID;

    assets.push({
      assetType,
      assetName: getAssetName(assetType),
      count: 1,
      system: row?.system,
      capacity: getAssetCapacity(assetType, row?.assetDetails),
      voltage: getAssetVoltage(assetType, row?.assetDetails),
      warrantyStartDate: new Date(row?.warrantyStartDate).toLocaleDateString("en-IN", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
      }),
      warrantyDuration: row?.warrantyDuration + " Years",
      brand: row?.brandID,
      modelNumber: row?.modelNumber,
      assetId: row?.assetId,
      serialNumber: row?.serialNumber,
      isOperational: row?.isOperational,
    });
  }

  return assets;
}

const fetchAssets = async (filter, limit, offset) => {
  const facilityDetailsResponse = await AssetService.fetchAssets(filter, limit, offset);
  return await formatData(facilityDetailsResponse);
};

const useAsset = (assetQueryFilter, limit = 1000, offset = 0) => {

  const { facility, assetFilterQuery } = assetQueryFilter;

  const filter = {
    criteria: {
      tenantId: Digit.ULBService.getCurrentTenantId(),
    }
  }

  if (facility?.facilityId) {
    filter.criteria.facilityID = facility.facilityId;
  }

  if (assetFilterQuery?.assetType?.length) {
    filter.criteria.assetType = assetFilterQuery.assetType;
  }

  if (assetFilterQuery?.serialNumber?.length) {
    filter.criteria.serialNumber = assetFilterQuery.serialNumber;
  }

  if (assetFilterQuery?.isOperational?.length) {
    filter.criteria.isOperational = assetFilterQuery.isOperational[0];
  }

  const queryClient = useQueryClient();
  const { isLoading, isError, error, data } = useQuery(
    ["ASSET", filter, limit, offset],
    () => fetchAssets(filter, limit, offset)
  );

  return {
    isLoading, isError, error, data,
    revalidate: () => queryClient.invalidateQueries(["ASSET"])
  }
}

export default useAsset;