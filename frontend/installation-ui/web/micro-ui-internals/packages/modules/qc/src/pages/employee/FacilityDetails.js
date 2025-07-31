import React, { useEffect, useState } from "react";
import Summary from "../../components/FacilityDetails/Summary";
import QCActions from "../../components/FacilityDetails/QCActions";
import AuditTrial from "../../components/FacilityDetails/AuditTrial";
import { useDispatch, useSelector } from "react-redux";
import { QCService } from "./Service/QCService";
import { clearRejectionReasons } from "../../redux/actions";

const FacilityDetails = ({t}) => {

  const selectedFacility = useSelector((state) => state.qc.common.selectedFacility);
  const [fetchedData, setData] = useState([]);
  const dispatch = useDispatch();

  const [pdfFile, setPdfFile] = useState({
    name: "Alkod.pdf",
    size: "3.5 MB"
  });

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
        await QCService.fetchImageFromFileStore(document?.fileStore)
          .then((response) => {
            fetchedDocuments.push(Digit.Utils.getFileUrl(response[document?.fileStore]))
          })
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

  useEffect(async () => {
    await QCService.fetchAssets(selectedFacility?.facilityId)
      .then(async (response) => {
        await formatData(response)
          .then((data) => {
            setData(data);
          })
      })
      .catch((error) => {
        console.error("Error fetching assets", error);
      })
  }, []);

  useEffect(() => {
    return () => {
      dispatch(clearRejectionReasons());
    }
  }, []);

  const hospitalDetails = {
    ...selectedFacility,
    healthFacilityType: "Loc 1"
  }

  const auditTrail = [
    {
      status: "Submitted",
      date: "25/05/25",
    },
    {
      status: "Rejected",
      date: "05/05/25",
      reasons: [
        {
          section: "Inverter",
          reasons: [
            { title: "Rejection Reason 1", details: "Additional Details" },
            { title: "Rejection Reason 2", details: "Additional Details" },
          ],
        },
        {
          section: "Panel",
          reasons: [
            { title: "Rejection Reason 1", details: "Additional Details" },
            { title: "Rejection Reason 2", details: "Additional Details" },
          ],
        },
      ],
    },
    {
      status: "Submitted",
      date: "25/04/25",
    },
  ];

  return (
    <div style={{marginTop: "20px"}}>
      <div style={{fontSize: "24px", fontWeight: "bold", marginBottom: "20px", color: "#004d66"}}>
          {hospitalDetails.facility}
      </div>
      <div style={{
        marginTop: "15px",
        width: "95%",
        padding: "20px",
        background: "white",
        borderRadius: "4px",
        boxShadow: "0px 0px 4px rgba(0, 0, 0, 0.25)",
        border: "1px solid #eee",
      }}>
        <div style={{display: "flex", alignItems: "center", marginTop: "15px"}}>
          <div style={{width: "30%"}}><strong>District</strong></div>
          {hospitalDetails.district}
        </div>
        <div style={{display: "flex", alignItems: "center", marginTop: "15px"}}>
          <div style={{width: "30%"}}><strong>Block</strong></div>
          {hospitalDetails.block}
        </div>
        <div style={{display: "flex", alignItems: "center", marginTop: "15px"}}>
          <div style={{width: "30%"}}><strong>Health Facility Type</strong></div>
          {hospitalDetails.healthFacilityType}
        </div>
        <div style={{display: "flex", alignItems: "center", marginTop: "15px"}}>
          <div style={{width: "30%"}}><strong>Status</strong></div>
          {hospitalDetails.status}
        </div>
      </div>

      {auditTrail && <AuditTrial t={t} auditTrial={auditTrail} />}

      {fetchedData && fetchedData.map((asset) => {
        return <Summary
          sectionName={asset?.assetName}
          count={asset?.count}
          specifications={asset?.specifications}
          details={asset?.details}
          items={asset?.items}
        />
      })}

      {pdfFile && <Summary sectionName="InstallationCompletionReport" pdf={pdfFile} isReport={true} />}

      {selectedFacility?.status && selectedFacility?.status.toUpperCase() === "SUBMITTED_BY_SUPERVISOR" && <QCActions />}

    </div>
  );
}

export default FacilityDetails;