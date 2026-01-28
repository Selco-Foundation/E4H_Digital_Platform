package org.egov.id.service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;
import org.egov.id.config.PropertiesManager;
import org.egov.id.model.IDSeqOverflowException;
import org.egov.id.model.IdGenerationRequest;
import org.egov.id.model.IdGenerationResponse;
import org.egov.id.model.IdRequest;
import org.egov.id.model.IdResponse;
import org.egov.id.model.InvalidIDFormatException;
import org.egov.id.model.RequestInfo;
import org.egov.id.model.ResponseInfoFactory;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;


/**
 * Description : IdGenerationService have methods related to the IdGeneration
 *
 * @author Pavan Kumar Kamma
 */
@Service
@Slf4j
public class IdGenerationService {

    @Autowired
    DataSource dataSource;

    @Autowired
    PropertiesManager propertiesManager;

    @Autowired
    private ResponseInfoFactory responseInfoFactory;

    @Autowired
    private MdmsService mdmsService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // by default 'idformat' will be taken from MDMS. Change value of 'ismdms.on' to 'false'
    // in application.properties to get data from DB instead.
    @Value("${idformat.from.mdms}")
    public boolean idFormatFromMDMS;

    //By default the auto create sequence is disabled
    @Value("${autocreate.new.seq}")
    public boolean autoCreateNewSeq;


    //default count value
    public Integer defaultCount = 1;


    /**
     * Description : This method to generate idGenerationResponse
     *
     * @param idGenerationRequest
     * @return idGenerationResponse
     * @throws Exception
     */

    public IdGenerationResponse generateIdResponse(IdGenerationRequest idGenerationRequest) throws Exception {
        log.trace("generateIdResponse method invoked");

        RequestInfo requestInfo = idGenerationRequest.getRequestInfo();
        List<IdRequest> idRequests = idGenerationRequest.getIdRequests();
        log.info("Processing ID generation request with {} id requests", idRequests != null ? idRequests.size() : 0);
        
        List<IdResponse> idResponses = new LinkedList<>();
        IdGenerationResponse idGenerationResponse = new IdGenerationResponse();

        for (IdRequest idRequest : idRequests) {
            log.debug("Processing ID request for idName: {}, tenantId: {}", 
                    idRequest != null ? idRequest.getIdName() : null, 
                    idRequest != null ? idRequest.getTenantId() : null);
            List<String> generatedId = generateIdFromIdRequest(idRequest, requestInfo);
            log.debug("Generated {} IDs for idName: {}", 
                    generatedId != null ? generatedId.size() : 0, 
                    idRequest != null ? idRequest.getIdName() : null);
            for (String ListOfIds : generatedId) {
                IdResponse idResponse = new IdResponse();
                idResponse.setId(ListOfIds);
                idResponses.add(idResponse);
            }
            idGenerationResponse.setIdResponses(idResponses);
        }
        idGenerationResponse.setResponseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(requestInfo, true));
        
        log.info("Successfully generated {} total IDs", idResponses.size());
        return idGenerationResponse;
    }

    ;

    /**
     * Description : This method to generate id
     *
     * @param idRequest
     * @param requestInfo
     * @return generatedId
     * @throws Exception
     */
    private List generateIdFromIdRequest(IdRequest idRequest, RequestInfo requestInfo) throws Exception {
        log.trace("generateIdFromIdRequest method invoked");

        List<String> generatedId = new LinkedList<>();
        boolean autoCreateNewSeqFlag = true;
        if (!StringUtils.isEmpty(idRequest.getIdName())) {
            log.debug("ID name provided: {}, fetching format", idRequest.getIdName());
            // If IDName is specified then check if it is defined in MDMS
            String idFormat = getIdFormatFinal(idRequest, requestInfo);

            // If the idname is defined then the format should be used
            // else fallback to the format in the request itself
            if (!StringUtils.isEmpty(idFormat)){
                idRequest.setFormat(idFormat);
                autoCreateNewSeqFlag = true;
                log.debug("ID format found from MDMS/DB, autoCreateNewSeqFlag set to true");
            } else if(StringUtils.isEmpty(idFormat)){
                autoCreateNewSeqFlag = false;
                log.debug("ID format not found in MDMS/DB, autoCreateNewSeqFlag set to false");
            }
        } else {
            log.debug("No ID name provided, using format from request");
        }

        if (StringUtils.isEmpty(idRequest.getFormat())) {
            log.error("No format available in MDMS or request for idName: {}, tenantId: {}", 
                    idRequest.getIdName(), idRequest.getTenantId());
            throw new CustomException("ID_NOT_FOUND",
                    "No Format is available in the MDMS for the given name and tenant");
        }

        log.info("Generating formatted IDs with format: {}", idRequest.getFormat().substring(0, Math.min(50, idRequest.getFormat().length())));
        return getFormattedId(idRequest, requestInfo, autoCreateNewSeqFlag);
    }


    /**
     * Description : This method to generate Id when format is unknown and select MDMS or DB.
     *
     * @param idRequest
     * @param requestInfo
     * @return generatedId
     * @throws Exception
     */
    private String getIdFormatFinal(IdRequest idRequest, RequestInfo requestInfo) throws Exception {
        log.trace("getIdFormatFinal method invoked");

        String idFormat = null;
        try{
            if (idFormatFromMDMS) {
                log.debug("Fetching ID format from MDMS for idName: {}", idRequest.getIdName());
                idFormat = mdmsService.getIdFormat(requestInfo, idRequest); //from MDMS
            } else {
                log.debug("Fetching ID format from DB for idName: {}", idRequest.getIdName());
                idFormat = getIdFormatfromDB(idRequest, requestInfo); //from DB
            }
        } catch(CustomException e) {
            throw e;
        } catch(Exception ex){
            if(StringUtils.isEmpty(idFormat)){
                log.error("Format returned NULL from both MDMS and DB for idName: {}, tenantId: {}", 
                        idRequest.getIdName(), idRequest.getTenantId(), ex);
                throw new CustomException("ID_NOT_FOUND",
                        "No Format is available in the MDMS for the given name and tenant");
            }
            log.error("Error while fetching format for idName: {}, tenantId: {}", 
                    idRequest.getIdName(), idRequest.getTenantId(), ex);
        }
        log.debug("ID format retrieval completed, format found: {}", idFormat != null);
        return idFormat;
    }

    /**
     * Description : This method to retrieve Id format from DB
     *
     * @param idRequest
     * @param requestInfo
     * @return idFormat
     * @throws Exception
     */
    private String getIdFormatfromDB(IdRequest idRequest, RequestInfo requestInfo) throws Exception {
        log.trace("getIdFormatfromDB method invoked");

        String idFormat = null;
        try {
            String idName = idRequest.getIdName();
            String tenantId = idRequest.getTenantId();
            log.debug("Querying DB for ID format with idName: {}, tenantId: {}", idName, tenantId);
            // select the id format from the id generation table
            StringBuffer idSelectQuery = new StringBuffer();
            idSelectQuery.append("SELECT format FROM id_generator ").append(" WHERE idname=? and tenantid=?");

           String rs = jdbcTemplate.queryForObject(idSelectQuery.toString(),new Object[]{idName,tenantId}, String.class);
            if (!StringUtils.isEmpty(rs)) {
                idFormat = rs;
                log.debug("ID format found in DB for idName: {}, tenantId: {}", idName, tenantId);
            } else {
                log.debug("ID format not found with tenantId, querying with idName only: {}", idName);
                // querying for the id format with idname
                StringBuffer idNameQuery = new StringBuffer();
                idNameQuery.append("SELECT format FROM id_generator ").append(" WHERE idname=?");
                 rs = jdbcTemplate.queryForObject(idNameQuery.toString(),new Object[]{idName}, String.class);
                if (!StringUtils.isEmpty(rs)) {
                    idFormat = rs;
                    log.debug("ID format found in DB for idName: {}", idName);
                } else {
                    log.warn("ID format not found in DB for idName: {}, tenantId: {}", idName, tenantId);
                }
            }
        } catch (Exception ex){
            log.error("SQL error while trying to retrieve format from DB for idName: {}, tenantId: {}", 
                    idRequest.getIdName(), idRequest.getTenantId(), ex);
        }
        return idFormat;
    }

    /**
     * Description : This method to generate Id when format is known
     *
     * @param idRequest
     * @param requestInfo
     * @return formattedId
     * @throws Exception
     */

    private List getFormattedId(IdRequest idRequest, RequestInfo requestInfo, boolean autoCreateNewSeqFlag) throws Exception {
        log.trace("getFormattedId method invoked");

        String idFormat = idRequest.getFormat();
        idFormat = applyTenantPlaceholders(idRequest, idFormat);

        List<String> attributes = extractAttributesFromFormat(idFormat);
        Integer count = getCount(idRequest);

        log.info("Generating {} formatted IDs", count);
        List<String> idFormatList = buildFormattedIdList(idRequest, requestInfo, autoCreateNewSeqFlag, idFormat, attributes, count);

        log.debug("Successfully generated {} formatted IDs", idFormatList.size());
        return idFormatList;
    }

    /**
     * Replaces tenant-based placeholders in the ID format.
     */
    private String applyTenantPlaceholders(IdRequest idRequest, String idFormat) {
        try {
            if (!StringUtils.isEmpty(idFormat.trim()) && !StringUtils.isEmpty(idRequest.getTenantId())) {
                log.debug("Replacing tenant placeholders in format with tenantId: {}", idRequest.getTenantId());
                idFormat = idFormat.replace("[tenantid]", idRequest.getTenantId());
                idFormat = idFormat.replace("[tenant_id]", idRequest.getTenantId().replace(".", "_"));
                idFormat = idFormat.replace("[TENANT_ID]", idRequest.getTenantId().replace(".", "_").toUpperCase());
            }
        } catch (Exception ex) {
            if (StringUtils.isEmpty(idFormat)) {
                log.error("Blank format encountered for idName: {}", idRequest.getIdName());
                throw new CustomException("IDGEN_FORMAT_ERROR", "Blank format is not allowed");
            }
        }
        return idFormat;
    }

    /**
     * Extracts attribute placeholders from the ID format.
     */
    private List<String> extractAttributesFromFormat(String idFormat) {
        List<String> attributes = new ArrayList<>();

        Pattern regExpPattern = Pattern.compile("\\[(.*?)\\]");
        Matcher regExpMatcher = regExpPattern.matcher(idFormat);

        while (regExpMatcher.find()) {
            attributes.add(regExpMatcher.group(1));
        }

        log.debug("Extracted {} placeholders from format", attributes.size());
        return attributes;
    }

    /**
     * Builds the final list of formatted IDs.
     */
    private List<String> buildFormattedIdList(IdRequest idRequest, RequestInfo requestInfo,
                                              boolean autoCreateNewSeqFlag, String idFormat,
                                              List<String> attributes, Integer count) throws Exception {

        List<String> idFormatList = new LinkedList<>();
        HashMap<String, List<String>> sequences = new HashMap<>();
        String idFormatTemplate = idFormat;
        String cityName = null;

        for (int i = 0; i < count; i++) {
            idFormat = idFormatTemplate;

            for (String attributeName : attributes) {
                log.trace("Processing attribute: {}", attributeName);
                idFormat = applyAttributeValue(idRequest, requestInfo, autoCreateNewSeqFlag, sequences, i, idFormat, attributeName, cityName);
                if (attributeName.substring(0, 4).equalsIgnoreCase("city") && cityName == null) {
                    cityName = mdmsService.getCity(requestInfo, idRequest);
                }
            }

            idFormatList.add(idFormat);
            log.trace("Generated formatted ID at index {}: {}", i, idFormat);
        }

        return idFormatList;
    }

    /**
     * Applies a single attribute value to the ID format string.
     */
    private String applyAttributeValue(IdRequest idRequest, RequestInfo requestInfo,
                                       boolean autoCreateNewSeqFlag, HashMap<String, List<String>> sequences,
                                       int index, String idFormat, String attributeName, String cityName) throws Exception {

        if (attributeName.substring(0, 3).equalsIgnoreCase("seq")) {
            if (!sequences.containsKey(attributeName)) {
                log.debug("Generating sequence numbers for: {}", attributeName);
                sequences.put(attributeName, generateSequenceNumber(attributeName, requestInfo, idRequest, autoCreateNewSeqFlag));
            }
            idFormat = idFormat.replace("[" + attributeName + "]", sequences.get(attributeName).get(index));
        } else if (attributeName.substring(0, 2).equalsIgnoreCase("fy")) {
            idFormat = idFormat.replace("[" + attributeName + "]",
                    generateFinancialYearDateFormat(attributeName, requestInfo));
        } else if (attributeName.substring(0, 2).equalsIgnoreCase("cy")) {
            idFormat = idFormat.replace("[" + attributeName + "]",
                    generateCurrentYearDateFormat(attributeName, requestInfo));
        } else if (attributeName.substring(0, 4).equalsIgnoreCase("city")) {
            if (cityName == null) {
                log.debug("Fetching city name from MDMS");
                cityName = mdmsService.getCity(requestInfo, idRequest);
            }
            idFormat = idFormat.replace("[" + attributeName + "]", cityName);
        } else {
            idFormat = idFormat.replace("[" + attributeName + "]", generateRandomText(attributeName, requestInfo));
        }

        return idFormat;
    }

    /**
     * Description : This method to generate current financial year in given
     * format
     *
     * @param requestInfo
     * @return formattedDate
     */
    private String generateFinancialYearDateFormat(String financialYearFormat, RequestInfo requestInfo) {
        log.trace("generateFinancialYearDateFormat method invoked");
        
        try {
            Date date = new Date();
            financialYearFormat = financialYearFormat.substring(financialYearFormat.indexOf(":") + 1);
            financialYearFormat = financialYearFormat.trim();
            log.debug("Generating financial year with format: {}", financialYearFormat);
            
            String currentFinancialYear = null;
            String[] financialYearPatternArray;
            financialYearPatternArray = financialYearFormat.split("-");
            int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
            int preYear = 0;
            int postYear = 0;

            for (String yearPattern : financialYearPatternArray) {
                String formattedYear = null;
                SimpleDateFormat formatter = new SimpleDateFormat(yearPattern.trim());
                formattedYear = formatter.format(date);

                if (financialYearPatternArray[0] == yearPattern) {
                    if (month > 3) {
                        preYear = Integer.valueOf(formattedYear);
                    } else {
                        preYear = Integer.valueOf(formattedYear) - 1;
                    }
                } else {
                    if (month > 3) {
                        postYear = Integer.valueOf(formattedYear) + 1;
                    } else {
                        postYear = Integer.valueOf(formattedYear);
                    }
                }
            }
            currentFinancialYear = preYear + "-" + postYear;
            log.debug("Generated financial year: {}", currentFinancialYear);
            return currentFinancialYear;

        } catch (Exception e) {
            log.error("Error while generating financial year with format: {}", financialYearFormat, e);
            throw new CustomException("INVALID_FORMAT", "Error while generating financial year in provided format. Given format invalid.");
        }
    }

    /**
     * Description : This method to generate current year date in given format
     *
     * @param dateFormat
     * @param requestInfo
     * @return formattedDate
     */
    private String generateCurrentYearDateFormat(String dateFormat, RequestInfo requestInfo) {
        log.trace("generateCurrentYearDateFormat method invoked");
        
        try {
            Date date = new Date();
            dateFormat = dateFormat.trim();
            dateFormat = dateFormat.substring(dateFormat.indexOf(":") + 1);
            dateFormat = dateFormat.trim();
            log.debug("Generating current year date with format: {}", dateFormat);
            
            SimpleDateFormat formatter = new SimpleDateFormat(dateFormat.trim());
            formatter.setTimeZone(TimeZone.getTimeZone(propertiesManager.getTimeZone()));
            String formattedDate = formatter.format(date);
            log.debug("Generated formatted date: {}", formattedDate);
            return formattedDate;

        } catch (Exception e) {
            log.error("Error while generating current year date with format: {}", dateFormat, e);
            throw new CustomException("INVALID_FORMAT", "Error while generating current year in provided format. Given format invalid.");
        }
    }

    /**
     * Description : This method to generate random text
     *
     * @param regex
     * @param requestInfo
     * @return randomTxt
     */
    private String generateRandomText(String regex, RequestInfo requestInfo) {
        log.trace("generateRandomText method invoked");
        
        Random random = new Random();
        List<String> matchList = new ArrayList<String>();
        int length = 2;// default digits length
        try {
            Pattern.compile(regex);
            log.debug("Validating regex pattern: {}", regex);
        } catch (Exception e) {
            log.error("Invalid regex pattern provided: {}", regex, e);
            throw new CustomException("INVALID_REGEX", "Random text could not be generated. Invalid regex provided.");
        }
        Matcher matcher = Pattern.compile("\\{(.*?)\\}").matcher(regex);
        while (matcher.find()) {
            matchList.add(matcher.group(1));
        }
        if (matchList.size() > 0) {
            length = Integer.parseInt(matchList.get(0));
            log.debug("Extracted length from regex: {}", length);
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            stringBuilder.append(random.nextInt(25));
        }
        String randomTxt = stringBuilder.toString();
        randomTxt = randomTxt.substring(0, length);
        log.trace("Generated random text of length {}", length);
        return randomTxt;
    }

    /**
     * Description : This method to set default count value
     *
     * @param idRequest
     * @return count
     */
    private Integer getCount(IdRequest idRequest) {
        log.trace("getCount method invoked");
        
        Integer count;
        if (idRequest.getCount() == null) {
            count = defaultCount;
            log.debug("Using default count: {}", defaultCount);
        } else {
            count = idRequest.getCount();
            log.debug("Using requested count: {}", count);
        }
        return count;
    }

    /**
     * Description : This method to generate sequence in DB
     *
     * @param sequenceName
     */

    private void createSequenceInDb(String sequenceName) throws Exception {
        log.trace("createSequenceInDb method invoked");

        StringBuilder query = new StringBuilder("CREATE SEQUENCE ");
        try {
            log.info("Creating new sequence in database: {}", sequenceName);
            query = query.append(sequenceName);
            jdbcTemplate.execute(query.toString());
            log.info("Successfully created sequence: {}", sequenceName);
        } catch (Exception ex){
            log.error("Error creating new sequence: {}", sequenceName, ex);
            throw ex;
        }
    }

    /**
     * Description : This method to generate sequence number
     *
     * @param sequenceName
     * @param requestInfo
     * @return seqNumber
     */
    private List<String> generateSequenceNumber(String sequenceName, RequestInfo requestInfo, IdRequest idRequest,boolean autoCreateNewSeqFlag) throws Exception {
        log.trace("generateSequenceNumber method invoked");

        Integer count = getCount(idRequest);
        log.info("Generating {} sequence numbers from sequence: {}", count, sequenceName);
        
        List<String> sequenceList = new LinkedList<>();
        List<String> sequenceLists = new LinkedList<>();
        // To generate a block of seq numbers

        String sequenceSql = "SELECT NEXTVAL ('" + sequenceName + "') FROM GENERATE_SERIES(1,?)";
        try {
            log.debug("Executing sequence query for: {} with count: {}", sequenceName, count);
            sequenceList = jdbcTemplate.queryForList(sequenceSql, new Object[]{count}, String.class);
            log.debug("Successfully retrieved {} sequence numbers", sequenceList.size());
        } catch (BadSqlGrammarException ex) {
            if (ex.getSQLException().getSQLState().equals("42P01")){
                log.warn("Sequence not found: {}, autoCreateNewSeqFlag: {}, autoCreateNewSeq: {}", 
                        sequenceName, autoCreateNewSeqFlag, autoCreateNewSeq);
                try{
                    if (sequenceList.isEmpty() && autoCreateNewSeqFlag && autoCreateNewSeq){
                        createSequenceInDb(sequenceName);
                        log.info("Retrying sequence query after creating sequence: {}", sequenceName);
                        sequenceList = jdbcTemplate.queryForList(sequenceSql, new Object[]{count}, String.class);
                    }
                    else if(sequenceList.isEmpty() && !autoCreateNewSeqFlag) {
                        log.error("Sequence does not exist and auto-creation is not allowed: {}", sequenceName);
                        throw new CustomException("SEQ_DOES_NOT_EXIST","auto creation of seq is not allowed in DB");
                    }
                }catch(CustomException e) {
                    throw e;
                }catch(Exception e) {
                    log.error("Error occurred while auto creating sequence: {}", sequenceName, e);
                    throw new CustomException("ERROR_CREATING_SEQ","Error occurred while auto creating seq in DB");
                }
            }else{
                log.error("SQL error while retrieving sequence number from DB for sequence: {}", sequenceName, ex);
                throw new CustomException("SEQ_NUMBER_ERROR","Error in retrieving seq number from DB");
            }
        } catch (CustomException e) {
            throw e;
        } catch (Exception ex) {
            log.error("Error retrieving seq number from DB for sequence: {}", sequenceName, ex);
            throw new CustomException("SEQ_NUMBER_ERROR","Error retrieving seq number from existing seq in DB");
        }
        for (String seqId : sequenceList) {
            String seqNumber = String.format("%04d", Integer.parseInt(seqId)).toString();
            sequenceLists.add(seqNumber.toString());
        }
        log.debug("Formatted {} sequence numbers to 4-digit format", sequenceLists.size());
        return sequenceLists;
    }

}
