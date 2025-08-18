import { useQuery, useQueryClient } from "react-query";

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
      return assetDetails?.inverterCapacity + " " + assetDetails?.capacityUnit;
  }
}

const fetchFileStoreDocuments = async (documents) => {
  const fetchedDocuments = [];
  for (const document of documents) {
    if (document?.documentType?.toUpperCase() === "ASSET") {
      const fileStoreResponse = await Digit.QCService.fetchImageFromFileStore(document?.fileStore);
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
        assetName: getAssetName(assetType),
        count: 1,
        specifications: {
          system: row?.system,
          capacity: getAssetCapacity(assetType, row?.assetDetails)
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

const fetchFacilityDetails = async (facilityId) => {
  const facilityDetailsResponse = await Digit.QCService.fetchAssets(facilityId);
  return await formatData(facilityDetailsResponse);
}

const useAsset = (facilityId) => {

  const facility = facilityId;
  const queryClient = useQueryClient();
  const { isLoading, isError, error, data } = useQuery(
    ["facilityDetails", facility],
    () => fetchFacilityDetails(facility)
  );

  return {
    isLoading, isError, error, data,
    revalidate: () => queryClient.invalidateQueries(["facilityDetails", facility])
  }
}

export default useAsset;