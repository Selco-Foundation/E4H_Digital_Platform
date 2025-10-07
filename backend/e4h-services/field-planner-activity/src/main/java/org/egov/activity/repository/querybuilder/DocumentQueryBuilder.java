package org.egov.activity.repository.querybuilder;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Component
public class DocumentQueryBuilder {

    private static final String FETCH_DOCUMENT_QUERY = "select d.id as documentId, d.bomid as document_bomId, d.documentType as document_documentType, " +
            " d.fileStoreId as document_fileStoreId, d.documentUid as document_documentUid, d.additionalDetails as document_additionalDetails, d.status as document_status, " +
            "d.createdBy as document_createdBy, d.createdTime as document_createdTime, d.lastModifiedBy as document_lastModifiedBy, d.lastModifiedTime as document_lastModifiedTime " +
            " from bom_document d ";

    private static void addClauseIfRequired(List<Object> values, StringBuilder queryString) {
        if (values.isEmpty())
            queryString.append(" WHERE ");
        else {
            queryString.append(" AND");
        }
    }

    /* Constructs document search query based on BOM Ids */
    public String getDocumentSearchQuery(Set<String> bomIdIds, List<Object> preparedStmtList) {
        StringBuilder queryBuilder = null;
        queryBuilder = new StringBuilder(FETCH_DOCUMENT_QUERY);

        if (bomIdIds != null && !bomIdIds.isEmpty()) {
            addClauseIfRequired(preparedStmtList, queryBuilder);
            queryBuilder.append(" d.bomid IN (").append(createQuery(bomIdIds)).append(")");
            addToPreparedStatement(preparedStmtList, bomIdIds);
        }

        return queryBuilder.toString();
    }

    private String createQuery(Collection<String> ids) {
        StringBuilder builder = new StringBuilder();
        int length = ids.size();
        for (int i = 0; i < length; i++) {
            builder.append(" ? ");
            if (i != length - 1) builder.append(",");
        }
        return builder.toString();
    }

    private void addToPreparedStatement(List<Object> preparedStmtList, Collection<String> ids) {
        ids.forEach(id -> {
            preparedStmtList.add(id);
        });
    }
}
