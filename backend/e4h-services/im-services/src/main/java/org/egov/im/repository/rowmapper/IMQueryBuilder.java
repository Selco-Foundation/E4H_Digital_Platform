package org.egov.im.repository.rowmapper;

import org.egov.im.config.IMConfiguration;
import org.egov.im.web.models.RequestSearchCriteria;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
public class IMQueryBuilder {

	private IMConfiguration config;

	@Autowired
    public IMQueryBuilder(IMConfiguration config) {
        this.config = config;
	}


    private static final String QUERY_ALIAS =   "ser.id as ser_id,ser.accountId as ser_accountid,ser.reporterTenant as ser_reportertenant," +
                                                "ser.tenantId as ser_tenantId," +
                                                "ser.additionaldetails as ser_additionaldetails," +
                                                "ser.createdby as ser_createdby,ser.createdtime as ser_createdtime," +
                                                "ser.lastmodifiedby as ser_lastmodifiedby,ser.lastmodifiedtime as ser_lastmodifiedtime" ;


    private static final String QUERY = "select ser.*," + QUERY_ALIAS+
                                        " from {schema}.eg_incident_v2 ser";

    private static final String COUNT_WRAPPER = "select count(*) from ({INTERNAL_QUERY}) as count";

    private static final String RESOLVED_COMPLAINTS_QUERY = "select count(*) from {schema}.eg_incident_v2 where applicationstatus='CLOSEDAFTERRESOLUTION' and tenantid=? and lastmodifiedtime>? ";

    private static final String AVERAGE_RESOLUTION_TIME_QUERY = "select round(avg(lastmodifiedtime-createdtime)/86400000) from {schema}.eg_incident_v2 where applicationstatus='CLOSEDAFTERRESOLUTION' and tenantid=? ";



    public String getPGRSearchQuery(RequestSearchCriteria criteria, List<Object> preparedStmtList) {

        StringBuilder builder = new StringBuilder(QUERY);

        if(criteria.getIsPlainSearch() != null && criteria.getIsPlainSearch()){
            Set<String> tenantIds = criteria.getTenantIds();
            if(!CollectionUtils.isEmpty(tenantIds)){
                addClauseIfRequired(preparedStmtList, builder);
                builder.append(" ser.tenantId IN (").append(createQuery(tenantIds)).append(")");
                addToPreparedStatement(preparedStmtList, tenantIds);
            }
          
        }
        else if (criteria.getPhcType()==null && criteria.getTenantId()!=null && criteria.getTenantId().contains(","))
        {
            //String tenantId = criteria.getTenantId();
            String[] tenantIdChunks = criteria.getTenantId().split(",");
            Set<String> tenantIdList = new HashSet<>(Arrays.asList(tenantIdChunks));
            addClauseIfRequired(preparedStmtList, builder);
            builder.append(" ser.tenantid IN (").append(createQuery(tenantIdList)).append(")");
            addToPreparedStatement(preparedStmtList, tenantIdList);
        }
        else {
            if ( criteria.getPhcType()==null && criteria.getTenantId() != null) {
                String tenantId = criteria.getTenantId();
                String[] tenantIdChunks = tenantId.split("\\.");

                if (tenantIdChunks.length == config.getStateLevelTenantIdLength()) {
                    addClauseIfRequired(preparedStmtList, builder);
                    builder.append(" ser.tenantid LIKE ? ");
                    preparedStmtList.add(criteria.getTenantId() + '%');
                }
                else {
                    addClauseIfRequired(preparedStmtList, builder);
                    builder.append(" ser.tenantid=? ");
                    preparedStmtList.add(criteria.getTenantId());
                }
                
            }
        }

        Set<String> applicationStatus = criteria.getApplicationStatus();
        if (!CollectionUtils.isEmpty(applicationStatus)) {
            addClauseIfRequired(preparedStmtList, builder);
            builder.append(" ser.applicationstatus IN (").append(createQuery(applicationStatus)).append(")");
            addToPreparedStatement(preparedStmtList, applicationStatus);
        }

        Set<String> incidentType = criteria.getIncidentType();
        if (!CollectionUtils.isEmpty(incidentType)){
            addClauseIfRequired(preparedStmtList, builder);
            builder.append(" ser.incidenttype IN (").append(createQuery(incidentType)).append(")");
            addToPreparedStatement(preparedStmtList, incidentType);
        }
        Set<String> phcType = criteria.getPhcType();
         if (!CollectionUtils.isEmpty(phcType)){
            addClauseIfRequired(preparedStmtList, builder);
            builder.append(" ser.tenantid IN (").append(createQuery(phcType)).append(")");
            addToPreparedStatement(preparedStmtList, phcType);
        }

        if (criteria.getIncidentId() != null) {
            addClauseIfRequired(preparedStmtList, builder);
            builder.append(" ser.incidentid=? ");
            preparedStmtList.add(criteria.getIncidentId());
        }

        Set<String> ids = criteria.getIds();
        if (!CollectionUtils.isEmpty(ids)) {
            addClauseIfRequired(preparedStmtList, builder);
            builder.append(" ser.id IN (").append(createQuery(ids)).append(")");
            addToPreparedStatement(preparedStmtList, ids);
        }

        //When UI tries to fetch "escalated" complaints count.
        if(criteria.getSlaDeltaMaxLimit() != null && criteria.getSlaDeltaMinLimit() == null){
            addClauseIfRequired(preparedStmtList, builder);
            builder.append(" ((extract(epoch FROM NOW())*1000) - ser.createdtime) > ? ");
            preparedStmtList.add(criteria.getSlaDeltaMaxLimit());
        }
        //When UI tries to fetch "other" complaints count.
        if(criteria.getSlaDeltaMaxLimit() != null && criteria.getSlaDeltaMinLimit() != null){
            addClauseIfRequired(preparedStmtList, builder);
            builder.append(" ((extract(epoch FROM NOW())*1000) - ser.createdtime) > ? ");
            preparedStmtList.add(criteria.getSlaDeltaMinLimit());
            addClauseIfRequired(preparedStmtList, builder);
            builder.append(" ((extract(epoch FROM NOW())*1000) - ser.createdtime) < ? ");
            preparedStmtList.add(criteria.getSlaDeltaMaxLimit());
        }

        Set<String> userIds = criteria.getUserIds();
        if (!CollectionUtils.isEmpty(userIds)) {
            addClauseIfRequired(preparedStmtList, builder);
            builder.append(" ser.accountId IN (").append(createQuery(userIds)).append(")");
            addToPreparedStatement(preparedStmtList, userIds);
        }


        Set<String> localities = criteria.getLocality();
        if(!CollectionUtils.isEmpty(localities)){
            addClauseIfRequired(preparedStmtList, builder);
            builder.append(" ads.locality IN (").append(createQuery(localities)).append(")");
            addToPreparedStatement(preparedStmtList, localities);
        }

        if (criteria.getFromDate() != null) {
            addClauseIfRequired(preparedStmtList, builder);

            //If user does not specify toDate, take today's date as toDate by default.
            if (criteria.getToDate() == null) {
                criteria.setToDate(Instant.now().toEpochMilli());
            }

            builder.append(" ser.createdtime BETWEEN ? AND ?");
            preparedStmtList.add(criteria.getFromDate());
            preparedStmtList.add(criteria.getToDate());

        } else {
            //if only toDate is provided as parameter without fromDate parameter, throw an exception.
            if (criteria.getToDate() != null) {
                throw new CustomException("INVALID_SEARCH", "Cannot specify to-Date without a from-Date");
            }
        }


        addOrderByClause(builder, criteria);

        addLimitAndOffset(builder, criteria, preparedStmtList);

        return builder.toString();
    }


    public String getCountQuery(RequestSearchCriteria criteria, List<Object> preparedStmtList){
        String query = getPGRSearchQuery(criteria, preparedStmtList);
        String countQuery = COUNT_WRAPPER.replace("{INTERNAL_QUERY}", query);
        return countQuery;
    }

    private void addOrderByClause(StringBuilder builder, RequestSearchCriteria criteria){

        if(StringUtils.isEmpty(criteria.getSortBy()))
            builder.append( " ORDER BY ser_createdtime ");

        else if(criteria.getSortBy()== RequestSearchCriteria.SortBy.locality)
            builder.append(" ORDER BY ads.locality ");

        else if(criteria.getSortBy()== RequestSearchCriteria.SortBy.applicationStatus)
            builder.append(" ORDER BY ser.applicationStatus ");

        else if(criteria.getSortBy()== RequestSearchCriteria.SortBy.incidentId)
            builder.append(" ORDER BY ser.incidentid ");

        if(criteria.getSortOrder()== RequestSearchCriteria.SortOrder.ASC)
            builder.append(" ASC ");
        else builder.append(" DESC ");

    }

    private void addLimitAndOffset(StringBuilder builder, RequestSearchCriteria criteria, List<Object> preparedStmtList){

        builder.append(" OFFSET ? ");
        preparedStmtList.add(criteria.getOffset());

        builder.append(" LIMIT ? ");
        preparedStmtList.add(criteria.getLimit());

    }

    private static void addClauseIfRequired(List<Object> values, StringBuilder queryString) {
        if (values.isEmpty())
            queryString.append(" WHERE ");
        else {
            queryString.append(" AND");
        }
    }

    private String createQuery(Collection<String> ids) {
        StringBuilder builder = new StringBuilder();
        int length = ids.size();
        for( int i = 0; i< length; i++){
            builder.append(" ? ");
            if(i != length -1) builder.append(",");
        }
        return builder.toString();
    }

    private void addToPreparedStatement(List<Object> preparedStmtList, Collection<String> ids)
    {
        ids.forEach(id ->{ preparedStmtList.add(id);});
    }


	public String getResolvedComplaints(String tenantId, List<Object> preparedStmtListComplaintsResolved) {

		StringBuilder query = new StringBuilder("");
		query.append(RESOLVED_COMPLAINTS_QUERY);

		preparedStmtListComplaintsResolved.add(tenantId);

		// In order to get data of last 12 months, the months variables is pre-configured in application properties
    	int days = Integer.valueOf(config.getNumberOfDays()) ;

    	Calendar calendar = Calendar.getInstance();

    	// To subtract 12 months from current time, we are adding -12 to the calendar instance, as subtract function is not in-built
    	calendar.add(Calendar.DATE, -1*days);

    	// Converting the timestamp to milliseconds and adding it to prepared statement list
    	preparedStmtListComplaintsResolved.add(calendar.getTimeInMillis());

		return query.toString();
	}


	public String getAverageResolutionTime(String tenantId, List<Object> preparedStmtListAverageResolutionTime) {
		StringBuilder query = new StringBuilder("");
		query.append(AVERAGE_RESOLUTION_TIME_QUERY);

		preparedStmtListAverageResolutionTime.add(tenantId);

		return query.toString();
	}

}
