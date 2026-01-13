import { useQuery, useQueryClient } from "react-query";
import { AssetService } from "../services/Asset";
import { FilestoreService } from "../services/Filestore";

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

const fetchFileStoreDocuments = async (documents) => {
  const fetchedDocuments = [];
  for (const document of documents) {
    if (document?.documentType?.toUpperCase() === "ASSET") {
      const fileStoreResponse = await FilestoreService.fetchDocumentFromFilestore(document?.fileStore);
      fetchedDocuments.push(Digit.Utils.getFileUrl(fileStoreResponse[document?.fileStore]))
    }
  }

  return fetchedDocuments;
}

const formatData = async (data) => {
  const dataMap = new Map();

  for (const row of data) {
    const assetType = row?.assetTypeID;

    if (dataMap.has(assetType)) {
      dataMap.set(assetType, {
        ...dataMap.get(assetType),
        count: dataMap.get(assetType).count + 1,
        details: {
          ...dataMap.get(assetType).details,
          count: dataMap.get(assetType).details.count + 1
        },
        items: [
          ...dataMap.get(assetType).items,
          {
            assetId: row?.assetId,
            serialNumber: row?.serialNumber,
            capacity: getAssetCapacity(assetType, row?.assetDetails),
            documents: await fetchFileStoreDocuments(row?.documents)
          }
        ]
      })
    } else {
      dataMap.set(assetType, {
        assetType,
        assetName: getAssetName(assetType),
        count: 1,
        specifications: {
          system: row?.system,
          capacity: getAssetCapacity(assetType, row?.assetDetails),
          voltage: getAssetVoltage(assetType, row?.assetDetails)
        },
        details: {
          count: 1,
          warrantyStartDate: new Date(row?.warrantyStartDate).toLocaleDateString("en-IN", {
            day: "2-digit",
            month: "2-digit",
            year: "numeric",
          }),
          warrantyDuration: row?.warrantyDuration + " Years",
          brand: row?.brandID,
          modelNumber: row?.modelNumber
        },
        items: [
          {
            assetId: row?.assetId,
            serialNumber: row?.serialNumber,
            capacity: getAssetCapacity(assetType, row?.assetDetails),
            documents: await fetchFileStoreDocuments(row?.documents)
          }
        ]
      })
    }
  }

  return dataMap.values().toArray();
}

const fetchFacilityDetails = async (filter) => {
  const facilityDetailsResponse = await AssetService.fetchAssets(filter);
  return await formatData(facilityDetailsResponse);
}

const useActivityAsset = (activityFacilityId) => {

  const filter = {
    criteria: {
      tenantId: Digit.ULBService.getCurrentTenantId(),
      activityFacilityID: activityFacilityId,
    }
  }

  const queryClient = useQueryClient();
  const { isLoading, isError, error, data } = useQuery(
    ["ASSET", filter],
    () => fetchFacilityDetails(filter)
  );

  return {
    isLoading, isError, error, data,
    revalidate: () => queryClient.invalidateQueries(["ASSET"])
  }
}

export default useActivityAsset;