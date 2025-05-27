package org.egov.asset.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import digit.models.coremodels.Document;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class DocumentRowMapper {
    private final ObjectMapper mapper = new ObjectMapper();

    public final RowMapper<Document> rowMapper = (rs, rowNum) -> {
        Document document = new Document();
        document.setId(rs.getString("id"));
        document.setDocumentType(rs.getString("document_type"));
        document.setFileStore(rs.getString("filestore_id"));
        document.setDocumentUid(rs.getString("id"));
        document.setAdditionalDetails(rs.getObject("additional_details"));
        return document;
    };
}
