import React, { useState } from "react";
import Summary from "./component/Summary";
import SystemParameterReport from "./component/SystemParameterReport";
import ApproveButton from "./component/ApproveButton";

const FacilityDetails = ({t, facility}) => {
  const [pdfFile, setPdfFile] = useState({
    name: "Alkod.pdf",
    size: "3.5 MB"
  });

  const handleRemovePdf = () => {
    setPdfFile(null);
  };
  const handleApprove = () => {
    alert("Approved!");
  };

  const facilityDetails = [
    {
      facilityName: "Panels",
      count: 2,
      specifications: {
        system: "AC",
        capacity: "1 KVA",
        voltage: "1 Volt"
      },
      details: {
        count: 2,
        warrantyStartDate: "21/03/25",
        warrantyDuration: "15 Years",
        brand: "Brand 1",
        modelNumber: "Model 1"
      },
      items: [
        {
          serialNumber: "SR5955340958",
          capacity: "1 KVA",
          image: "https://via.placeholder.com/100?text=Panel+1"
        },
        {
          serialNumber: "SR5955340958",
          capacity: "1 KVA",
          image: "https://via.placeholder.com/100?text=Panel+2"
        }
      ],
      images: [
        "https://via.placeholder.com/100?text=Img+1",
        "https://via.placeholder.com/100?text=Img+2",
        "https://via.placeholder.com/100?text=Img+3",
        "https://via.placeholder.com/100?text=Img+4"
      ],
      videos: [
        {
          name: "Video_1.mp4",
          size: "3.5 MB",
          url: "https://www.w3schools.com/html/mov_bbb.mp4"
        },
        {
          name: "Video_2.mp4",
          size: "3.5 MB",
          url: "https://www.w3schools.com/html/movie.mp4"
        }
      ]
    },
    {
      facilityName: "Battery",
      count: 3,
      specifications: {
        system: "DC",
        capacity: "2 KVA",
        voltage: "12 Volt"
      },
      details: {
        count: 3,
        warrantyStartDate: "15/07/24",
        warrantyDuration: "10 Years",
        brand: "PowerSafe",
        modelNumber: "Model B2"
      },
      items: [
        {
          serialNumber: "BBX93485732",
          capacity: "2 KVA",
          image: "https://via.placeholder.com/100?text=Battery+1"
        },
        {
          serialNumber: "BBX93485733",
          capacity: "2 KVA",
          image: "https://via.placeholder.com/100?text=Battery+2"
        },
        {
          serialNumber: "BBX93485734",
          capacity: "2 KVA",
          image: "https://via.placeholder.com/100?text=Battery+3"
        }
      ],
      images: [
        "https://via.placeholder.com/100?text=Batt+Img+1",
        "https://via.placeholder.com/100?text=Batt+Img+2"
      ],
      videos: [
        {
          name: "Battery_Intro.mp4",
          size: "2.3 MB",
          url: "https://www.w3schools.com/html/mov_bbb.mp4"
        }
      ]
    },
    {
      facilityName: "Inverter",
      count: 1,
      specifications: {
        system: "Hybrid",
        capacity: "3 KVA",
        voltage: "220 Volt"
      },
      details: {
        count: 1,
        warrantyStartDate: "01/01/23",
        warrantyDuration: "8 Years",
        brand: "InverTech",
        modelNumber: "Inv-H3000"
      },
      items: [
        {
          serialNumber: "INV8837461",
          capacity: "3 KVA",
          image: "https://via.placeholder.com/100?text=Inverter"
        }
      ],
      images: [
        "https://via.placeholder.com/100?text=Inv+Front",
        "https://via.placeholder.com/100?text=Inv+Back"
      ],
      videos: [
        {
          name: "Inverter_Setup.mp4",
          size: "4.0 MB",
          url: "https://www.w3schools.com/html/movie.mp4"
        }
      ]
    }
  ]
  const hospitalDetails = {
    name:"Alok Hospital",
    district: "District 1",
    taluk: "Taluk 1",
    healthFacilityType: "Loc 1",
    status: "Pending Approval",
  }

  return (
    <div style={{marginTop: "20px"}}>
      <div style={{fontSize: "24px", fontWeight: "bold", marginBottom: "20px", color: "#004d66"}}>
        {hospitalDetails.name}
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
          <div style={{width: "30%"}}><strong>Taluk</strong></div>
          {hospitalDetails.taluk}
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
      {facilityDetails && facilityDetails.map((detail) => {
        return <Summary
          sectionName={detail.facilityName}
          count={detail.count}
          specifications={detail.specifications}
          details={detail.details}
          items={detail.items}
          images={detail.images}
          videos={detail.videos}
        />
      })}
      {pdfFile && <Summary pdf={pdfFile} onPdfRemove={handleRemovePdf} isReport={true} />}
      <ApproveButton />
    </div>
  );
}

export default FacilityDetails;