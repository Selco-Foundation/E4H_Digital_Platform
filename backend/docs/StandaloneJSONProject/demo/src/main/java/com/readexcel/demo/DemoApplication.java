package com.readexcel.demo;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.formula.eval.ConcatEval;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.ui.ModelMap;
//import org.json.JSONObject; 

import com.fasterxml.jackson.databind.ObjectMapper;
import com.readexcel.demo.entity.Assignments;
import com.readexcel.demo.entity.Education;
import com.readexcel.demo.entity.Employees;
import com.readexcel.demo.entity.JsonResponse;
import com.readexcel.demo.entity.RequestInfo;
import com.readexcel.demo.entity.Roles;
import com.readexcel.demo.entity.ServiceHistory;
import com.readexcel.demo.entity.Tests;
import com.readexcel.demo.entity.User;
import com.readexcel.demo.entity.jurisdiction.Jurisdictions;

public class DemoApplication{
	public static void main(String[] args) {
		try {
			FileInputStream fis=new FileInputStream("/home/beehyv/pasam/Devops/Selco/E4H_Digital_Platform/backend/docs/StandaloneJSONProject/demo/src/main/resources/config.properties");
			Properties prop=new Properties();
			prop.load(fis);
			String query_url=prop.getProperty("url");
			List<JsonResponse> user=testing();
			InputStream in=null;
			HttpURLConnection con=null;
			URL url = null;
			int i=1;
			for(JsonResponse json:user)
			{
				System.out.println(i++);
				System.out.println("Url ::::"+query_url+json.getTennantId());
				System.out.println("Request ::"+json.getJson());
				url=new URL (query_url+json.getTennantId());
				con = (HttpURLConnection)url.openConnection();
				con.setRequestMethod("POST");
				con.setRequestProperty("Content-Type", "application/json; utf-8");
				con.setRequestProperty("Accept", "application/json");
				con.setDoOutput(true);
				
				OutputStream os = con.getOutputStream();
				byte[] input = json.getJson().getBytes("utf-8"); 
				os.write(input, 0, input.length);
				
			    try {
			    	
					in=new BufferedInputStream(con.getInputStream());
				    String result=IOUtils.toString(in,"UTF-8");
				    System.out.println("Response ::"+result);
				}
			    catch(Exception e)
			    {
			    	e.printStackTrace();
			    	
			    }
			  //in.close(); con.disconnect();
				}
				
							
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("error- "+e.getMessage());
			//e.printStackTrace();
			
		}
		//writeExcel();
		
	}

private static void writeExcel() {
	System.out.println("inside writeExcel");
	try {
	FileInputStream fis=new FileInputStream("/home/beehyv/pasam/Devops/Selco/E4H_Digital_Platform/backend/docs/StandaloneJSONProject/demo/src/main/resources/config.properties");
	Properties prop=new Properties();
	prop.load(fis);
	String outputExcel=prop.getProperty("outexcel");
	FileOutputStream fileOut=new FileOutputStream(new File(outputExcel));
	XSSFWorkbook workbook=new XSSFWorkbook();
	Sheet sh=workbook.createSheet("result");
	String[] columnHeadings= {"UserName","Status"};
	Font headerFont=workbook.createFont();
	((XSSFFont) headerFont).setBold(true);
	headerFont.setFontHeightInPoints((short)12);
	headerFont.setColor(IndexedColors.BLACK.index);
	CellStyle headerStyle=workbook.createCellStyle();
	headerStyle.setFont(headerFont);
	headerStyle.setFillForegroundColor(IndexedColors.BLACK.index);
	Row headerRow=sh.createRow(0);
	for(int i=0;i<columnHeadings.length;i++) {
		Cell cell= headerRow.createCell(i);
		cell.setCellValue(columnHeadings[i]);
		cell.setCellStyle(headerStyle);
	}
	CreationHelper creationHelper=workbook.getCreationHelper();
	CellStyle dataStyle=workbook.createCellStyle();
	int rownum=1;
			/*
			 * for(int i=0;i<cr.size();i++) { Row row=sh.createRow(rownum++);
			 * row.createCell(0).setCellValue(cr.get(i).toString());
			 * row.createCell(1).setCellValue(status.get(i).toString()); }
			 */
	
		workbook.write(fileOut);
		fileOut.close();

		System.out.println("Completed");
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}

public static List<JsonResponse> testing() {
	List<RequestInfo> reqList = new ArrayList<RequestInfo>();
	List<Object> a=new ArrayList();
	ModelMap m = new ModelMap();
	//List<String> json=new ArrayList<String>();
	List<JsonResponse> jsonList=new ArrayList<JsonResponse>();
	JsonResponse json=null;
	try {
		FileInputStream fis=new FileInputStream("/home/beehyv/pasam/Devops/Selco/E4H_Digital_Platform/backend/docs/StandaloneJSONProject/demo/src/main/resources/config.properties");
		Properties prop=new Properties();
		prop.load(fis);
		String inputExcel=prop.getProperty("inexcel");
		String outputExcel=prop.getProperty("outexcel");
		FileInputStream file = new FileInputStream(new File(inputExcel));
	    XSSFWorkbook  wb = new XSSFWorkbook(file); 
	    for(int sn=0; sn<wb.getNumberOfSheets(); sn++) {
	    Sheet sheet = wb.getSheetAt(sn);
	    for (int rn=sheet.getFirstRowNum(); rn<=sheet.getLastRowNum(); rn++) {
	      Row row = sheet.getRow(rn);
	      
	      if(rn>0)
	      if (row != null) {
	    	  Employees employee = new Employees();
	    	  User user = new User();
	    	 List<Roles> urolelist= new ArrayList<Roles>();
	    	 List<Jurisdictions>jurisdictionsList= new ArrayList<Jurisdictions>();
	    	 List<com.readexcel.demo.entity.jurisdiction.Roles> jrolist =new ArrayList<com.readexcel.demo.entity.jurisdiction.Roles>();
	    	 List<Assignments>assignmentsList= new ArrayList<Assignments>();
	    	 List<ServiceHistory>serviceList= new ArrayList<ServiceHistory>();
	    	 List<Education>educationList= new ArrayList<Education>();
	    	 List<Tests>testList= new ArrayList<Tests>();
	    	 json=new JsonResponse();
	    	  int i=0;
	    	  Object cellvalue="";
		         for (int cn=0; cn<row.getLastCellNum(); cn++) {
		            Cell cell = row.getCell(cn);
		            if (cell != null && cn !=9) {
		            	switch (cell.getCellType()) 
	                    {
	                        case Cell.CELL_TYPE_NUMERIC:
	                            cellvalue=cell.getNumericCellValue();
	                            break;
	                        case Cell.CELL_TYPE_STRING:
	                            cellvalue=cell.getStringCellValue();
	                            break;   	
	                    }
		            }
		            else  if (cell != null && cn ==9) {
		            	cell.setCellType(Cell.CELL_TYPE_STRING);
	                     cellvalue=cell.getStringCellValue();
	                     
		            }
		            else {
		            	 cellvalue="";
		            }
		            switch (cn){
					case 0:{
                    	employee.setTenantId(getString(cellvalue));
                    	break;
                    }
                    case 1:{
                    	employee.setEmployeeStatus(getString(cellvalue));
                    	break;
                    }
                    case 2:{
                    	user.setName(getString(cellvalue));
                    	break;
                    }
                    case 3:{
                    	user.setMobileNumber(getBigDecimal(cellvalue).toString());
                    	break;
                    }
                    case 4:{
                    	user.setFatherOrHusbandName(getString(cellvalue));
                    	break;
                    }
                    case 5:{
                    	user.setRelationship(getString(cellvalue));
                    	break;
                    }
                    case 6:{
                    	user.setGender(getString(cellvalue));
                    	break;
                    }
                    case 7:{
                    	user.setDob(getBigDecimal(cellvalue));
                    	break;
                    }
                    case 8:{
                    	String[] rolearray;
                    	rolearray = splitDataByComma(cellvalue);
                    	if(null!=rolearray && rolearray.length>0) {
                    		for(int f=0;f<rolearray.length;f++) {
                    			Roles ur = new Roles();
                    			ur.setCode(rolearray[f]);
                    			ur.setName(prop.getProperty(ur.getCode()));
                    			//ur.setTenantId(employee.getTenantId());
                    			urolelist.add(ur);
                    		}
                    	}
                    	break;
                    }
                    case 9:{
                    	employee.setCode(getString(cellvalue).toString());
						outputExcel=employee.getCode();
                    	break;
                    }
                    case 10:{
                    	employee.setDateOfAppointment(getBigDecimal(prop.getProperty("appointDate").toString()));
                    	break;
                    }
                    case 11:{
                    	employee.setEmployeeType(getString(cellvalue).toString());
                    	break;
                    }
                    case 12:{
                    	String assignarray1,assignarray2,assignarray3,assignarray4,assignarray5;                    	
                    	if (cell != null) {
    		            	switch (cell.getCellType()) 
    	                    {
    	                        case Cell.CELL_TYPE_NUMERIC:
    	                            cellvalue=cell.getNumericCellValue();
    	                            break;
    	                        case Cell.CELL_TYPE_STRING:
    	                            cellvalue=cell.getStringCellValue();
    	                            break;   	
    	                    }
    		            } else {
    		            	 cellvalue="";
    		            }
    		            assignarray1 = getString(cellvalue).toString();
    		            cell = row.getCell(++cn);
    		            if (cell != null) {
    		            	switch (cell.getCellType()) 
    	                    {
    	                        case Cell.CELL_TYPE_NUMERIC:
    	                            cellvalue=cell.getNumericCellValue();
    	                            break;
    	                        case Cell.CELL_TYPE_STRING:
    	                            cellvalue=cell.getStringCellValue();
    	                            break;   	
    	                    }
    		            } else {
    		            	 cellvalue="";
    		            }
    		            assignarray2 = getString(cellvalue).toString();
    		            cell = row.getCell(++cn);
    		            if (cell != null) {
    		            	switch (cell.getCellType()) 
    	                    {
    	                        case Cell.CELL_TYPE_NUMERIC:
    	                            cellvalue=cell.getNumericCellValue();
    	                            break;
    	                        case Cell.CELL_TYPE_STRING:
    	                            cellvalue=cell.getStringCellValue();
    	                            break;   	
    	                    }
    		            } else {
    		            	 cellvalue="";
    		            }
    		            assignarray3 = getString(cellvalue).toString();
    		            cell = row.getCell(++cn);
    		            if (cell != null) {
    		            	switch (cell.getCellType()) 
    	                    {
    	                        case Cell.CELL_TYPE_NUMERIC:
    	                            cellvalue=cell.getNumericCellValue();
    	                            break;
    	                        case Cell.CELL_TYPE_STRING:
    	                            cellvalue=cell.getStringCellValue();
    	                            break;   	
    	                    }
    		            } else {
    		            	 cellvalue="";
    		            }
    		            assignarray4 = getString(cellvalue).toString();
    		            cell = row.getCell(++cn);
    		            if (cell != null) {
    		            	switch (cell.getCellType()) 
    	                    {
    	                        case Cell.CELL_TYPE_NUMERIC:
    	                            cellvalue=cell.getNumericCellValue();
    	                            break;
    	                        case Cell.CELL_TYPE_STRING:
    	                            cellvalue=cell.getStringCellValue();
    	                            break;
    	                    }
    		            } else {
    		            	 cellvalue="";
    		            }
    		            assignarray5 = getString(cellvalue).toString();
    		            Assignments j = new Assignments();
                    	j.setFromDate(getBigDecimal(prop.getProperty("fromdate").toString())); 
                    	if(assignarray1.toString().equalsIgnoreCase("null"))
                    		j.setToDate(null);
                    	else
                    	j.setToDate(getBigDecimal(assignarray1.toString())); 
                    	j.setIsCurrentAssignment(assignarray2);
                    	j.setDepartment(assignarray3); j.setDesignation(assignarray4); 
                    	assignmentsList.add(j);
                    	break;
                    }
                  }
		       }
		         user.setRoles(urolelist);
		         user.setTenantId(employee.getTenantId());
		         employee.setUser(user);
		         //jurisdictions
		         String furnishedRoles="";
         		 Jurisdictions j = new Jurisdictions();
     			 j.setHierarchy(prop.getProperty("hierarchy")); j.setBoundaryType(prop.getProperty("boundaryType")); j.setBoundary(employee.getTenantId());
     			 for(int f=0;f<urolelist.size();f++)
     			 {
     				com.readexcel.demo.entity.jurisdiction.Roles jr = new com.readexcel.demo.entity.jurisdiction.Roles();
     				jr.setValue(urolelist.get(f).getCode()); jr.setLabel(urolelist.get(f).getName());
         			jrolist.add(jr);
         			j.setRoles(jrolist);
         			furnishedRoles+=urolelist.get(f).getName();
         			if(f!=urolelist.size()-1)
         				furnishedRoles+=", ";
     			 }	
     			 j.setFurnishedRolesList(furnishedRoles);
     			 j.setTenantId(employee.getTenantId());
     			 jurisdictionsList.add(j);
     	         employee.setJurisdictions(jurisdictionsList);
		         
		         employee.setAssignments(assignmentsList);
		         employee.setServiceHistory(serviceList);
		         employee.setEducation(educationList);
		         employee.setTests(testList);
		      
		         RequestInfo r=new RequestInfo();
		         User userInfo=new User();
		         userInfo.setUuid(prop.getProperty("authToken"));
		         r.setApiId(prop.getProperty("apId"));
		         r.setVer(prop.getProperty("ver"));
		         r.setAction(prop.getProperty("action"));
		         r.setDid(prop.getProperty("did"));
		         r.setKey(prop.getProperty("key"));
		         r.setMsgId(prop.getProperty("msgId"));
		         r.setRequesterId(prop.getProperty("requestId"));
		         r.setAuthToken(prop.getProperty("authToken"));
		         r.setUserInfo(userInfo);
		         
		         m.put("RequestInfo", r);   
		      List<Employees>em =new ArrayList<Employees>();
		      em.add(employee);
		      m.put("Employees", em);
		      ObjectMapper objectMapper = new ObjectMapper();
		      	json.setTennantId(employee.getTenantId());
			    json.setJson(objectMapper.writeValueAsString(m));
			    jsonList.add(json);

			 }
	      
		   	}
	    
			  //ObjectMapper mapper=new ObjectMapper(); 
			 // String
			 // output=prop.getProperty("outexcel")+outputExcel+".json";
			//  mapper.writeValue(new File(output), m);
			 
			//System.out.println(m);
		}
	}catch (Exception e) {
		e.printStackTrace();
	}
	return jsonList;
}


public static String getString(Object o) {
	if(null!=o) {
		return o.toString();
	}
	
	else
		return null;
}

public static Integer getInteger(Object o) {
	if(null!=o) {
		System.out.println(o);
		return Integer.parseInt(o.toString());
	}
	
	else
	return null;
}

public static BigDecimal getBigDecimal(Object o) {
	if(null!=o) {
		
		BigDecimal bd=new BigDecimal(o.toString());
		return new BigDecimal(bd.longValue());
	}
	
	else
	return null;
}
public static BigInteger getBigInteger(Object o) {
	if(null!=o) {
		//System.out.println(o);
		return new BigInteger(o.toString());
	}
	
	else
	return null;
}

public static String[] splitDataByComma(Object o) {
	if(null!=o) {
		return o.toString().split(",");
	}else {
		return null;
	}
	
}
}
