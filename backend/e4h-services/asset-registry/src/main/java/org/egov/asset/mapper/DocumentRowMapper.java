package org.egov.asset.mapper;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import digit.models.coremodels.Document;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@Component
public class DocumentRowMapper {
    private final ObjectMapper mapper = new ObjectMapper();

    public final RowMapper<Document> rowMapper = (rs, rowNum) -> {
        return mapDocument(rs);
    };

    public Document mapDocument(ResultSet rs) throws SQLException {
        Document document = new Document();
        document.setId(rs.getString("id"));
        document.setDocumentType(rs.getString("document_type"));
        document.setFileStore(rs.getString("filestore_id"));
        document.setDocumentUid(rs.getString("id"));
        String additionalJson = rs.getString("additional_details");
        try {
            if (additionalJson != null && !additionalJson.isBlank()) {
                document.setAdditionalDetails(
                        mapper.readValue(additionalJson, new TypeReference<Map<String, Object>>() {})
                );
            }
        } catch (IOException e) {
            throw new RuntimeException("Error parsing JSONB fields", e);
        }
        return document;
    }
}
