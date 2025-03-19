import { Card } from "@selco/digit-ui-react-components";
import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";

const ComplaintsLink = ({ isMobile, data }) => {
  const { t } = useTranslation();

  const allLinks = [
    { text: "ES_IM_NEW_INCIDENT", link: `/${window.contextPath}/employee/im/incident/create`, accessTo: ["COMPLAINANT"] },
    // { text: "Reports", link: "/employee" },
    // { text: "Dashboard", link: "/employee" },
  ];

  const [links, setLinks] = useState([]);

  useEffect(() => {
    let linksToShow = [];
    allLinks.forEach((link) => {
      if (link.accessTo) {
        if (Digit.UserService.hasAccess(link.accessTo)) {
          linksToShow.push(link);
        }
      } else {
        linksToShow.push(link);
      }
    });
    setLinks(linksToShow);
  }, []);

  // useEffect(() => {
  //   if (isMobile) {
  //     const mobileLinks = links.filter((link) => {
  //       return link.text !== "Dashboard";
  //     });
  //     setLinks(mobileLinks);
  //   }
  // }, []);

  const GetLogo = () => (
    <div className="header">
      <span className="logo">
        <svg xmlns="http://www.w3.org/2000/svg" height="24" viewBox="0 0 24 24" width="24">
          <path d="M0 0h24v24H0z"  style={{fill:"#7a2829"}}/>
          <path d="M20 2H4c-1.1 0-1.99.9-1.99 2L2 22l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-7 9h-2V5h2v6zm0 4h-2v-2h2v2z" style={{fill:"white"}} />
        </svg>
      </span>{" "}
      <span className="text">{t("ES_IM_HEADER_INCIDENTS")}</span>
    </div>
  );

  return (
    <Card className="employeeCard filter inboxLinks" style={{width:"270px !important"}}>
      <div className="complaint-links-container" style={{padding:"16px",height:"115px",margin:"auto",width:!isMobile?"250px":""}}>
      <style>
        {`
        .complaint-links-container .header .logo {
            width: 56px;
            height: 56px;
            --bg-opacity: 1;
            background-color: #7a2829;
            
            padding: 12px;
            border-radius: 4px;
        }
        .complaint-links-container .body .link {
         padding-left: 0px;
         
      }
      .complaint-links-container .body {
        padding-left: 0px;
        margin-left: 0px;
        
        `
      }
    </style>
        {GetLogo()}
        <div className="body">
          {links.map(({ link, text }, index) => (
            <span className="link" key={index}>
              <Link to={link} style={{color:"#7a2829"}}>{t(text)}</Link>
            </span>
          ))}
        </div>
      </div>
    </Card>
  );
};

export default ComplaintsLink;
