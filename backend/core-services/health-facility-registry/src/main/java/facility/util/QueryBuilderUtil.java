package facility.util;

import facility.web.models.FacilitySearchRequest;

import java.util.ArrayList;
import java.util.List;

public class QueryBuilderUtil {

    public static QueryBuilderResult buildWhereClause(FacilitySearchRequest request) {
        StringBuilder whereClause = new StringBuilder(" WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (request.getTenantId() != null && !request.getTenantId().isBlank()) {
            whereClause.append(" AND tenant_id = ?");
            params.add(request.getTenantId());
        }

        if (request.getFacilityId() != null && !request.getFacilityId().isBlank()) {
            whereClause.append(" AND id = ?");
            params.add(request.getFacilityId());
        }

        if (request.getFacilityName() != null && !request.getFacilityName().isBlank()) {
            whereClause.append(" AND facility_name ILIKE ?");
            params.add("%" + request.getFacilityName() + "%");
        }

        if (request.getHfrId() != null && !request.getHfrId().isBlank()) {
            whereClause.append(" AND facility_details ->> 'hfrId' = ?");
            params.add(request.getHfrId());
        }

        if (request.getNinId() != null && !request.getNinId().isBlank()) {
            whereClause.append(" AND facility_details ->> 'ninId' = ?");
            params.add(request.getNinId());
        }

        if (request.getBoundaryCode() != null && !request.getBoundaryCode().isBlank()) {
            whereClause.append(" AND boundary_code = ?");
            params.add(request.getBoundaryCode());
        }

        if (request.getIsOnmReady() != null) {
            whereClause.append(" AND is_onm_ready = ?");
            params.add(request.getIsOnmReady());
        }

        return new QueryBuilderResult(whereClause.toString(), params);
    }
}
