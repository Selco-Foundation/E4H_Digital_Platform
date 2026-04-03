package org.egov.asset.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.asset.web.models.Document;
import org.egov.asset.web.models.GeoLocation;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@Component
@Slf4j
public class DocumentRowMapper {
    private final ObjectMapper mapper = new ObjectMapper();

    public final RowMapper<Document> rowMapper = (rs, rowNum) -> {
        log.trace("DocumentRowMapper::rowMapper called | rowNum={}", rowNum);
        return mapDocument(rs);
    };

    public Document mapDocument(ResultSet rs) throws SQLException {
        log.trace("DocumentRowMapper::mapDocument called");
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
            log.error("Error parsing JSONB fields for document | documentId={} error={}", 
                    document.getId(), e.getMessage(), e);
            throw new RuntimeException("Error parsing JSONB fields", e);
        }
        double latitude = rs.getDouble("latitude");
        double longitude = rs.getDouble("longitude");
        if (!rs.wasNull()) {
            GeoLocation geoLocation = GeoLocation.builder()
                    .latitude(latitude)
                    .longitude(longitude)
                    .build();
            document.setGeoLocation(geoLocation);
            log.debug("GeoLocation set for document | documentId={} latitude={} longitude={}", 
                    document.getId(), latitude, longitude);
        }
        log.debug("Document mapped successfully | documentId={}", document.getId());
        return document;
    }
}
