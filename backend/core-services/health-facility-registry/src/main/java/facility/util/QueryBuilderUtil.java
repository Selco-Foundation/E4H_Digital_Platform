package facility.util;

import facility.web.models.FacilityBulkSearchCriteria;
import facility.web.models.FacilitySearchRequest;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

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
            whereClause.append(" AND facility_details ->> 'hfr_id' = ?");
            params.add(request.getHfrId());
        }

        if (request.getNinId() != null && !request.getNinId().isBlank()) {
            whereClause.append(" AND facility_details ->> 'nin_id' = ?");
            params.add(request.getNinId());
        }

        if (request.getBoundaryCode() != null && !request.getBoundaryCode().isBlank()) {
            whereClause.append(" AND boundary_code ILIKE ?");
            params.add(request.getBoundaryCode()+ "%");
        }

        if (request.getIsOnmReady() != null) {
            whereClause.append(" AND is_onm_ready = ?");
            params.add(request.getIsOnmReady());
        }

        return new QueryBuilderResult(whereClause.toString(), params);
    }

    public static QueryBuilderResult buildBulkWhereClause(FacilityBulkSearchCriteria criteria, RequestInfo requestInfo, List<String> onmNonReadyAllowedRoles) {
        StringBuilder whereClause = new StringBuilder(" WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (!CollectionUtils.isEmpty(criteria.getTenantIds())) {
            whereClause.append(" AND tenant_id in ( ").append(createQuery(criteria.getTenantIds().size())).append(" )");
            params.addAll(criteria.getTenantIds());
        }

        if (!CollectionUtils.isEmpty(criteria.getFacilityIds())) {
            whereClause.append(" AND id in ( ").append(createQuery(criteria.getFacilityIds().size())).append(" )");
            params.addAll(criteria.getFacilityIds());
        }

        if (!CollectionUtils.isEmpty(criteria.getFacilityNames())) {
            whereClause.append(" AND facility_name ILIKE ANY ( ARRAY [ ").append(createQuery(criteria.getFacilityNames().size())).append(" ] )");
            params.addAll(criteria.getFacilityNames().stream().map((facilityName) -> "%" + facilityName + "%").toList());
        }

        if (!CollectionUtils.isEmpty(criteria.getHfrIds())) {
            whereClause.append(" AND facility_details ->> 'hfr_id' in ( ").append(createQuery(criteria.getHfrIds().size())).append(" )");
            params.addAll(criteria.getHfrIds());
        }

        if (!CollectionUtils.isEmpty(criteria.getNinIds())) {
            whereClause.append(" AND facility_details ->> 'nin_id' in ( ").append(createQuery(criteria.getNinIds().size())).append(" )");
            params.addAll(criteria.getNinIds());
        }

        if (!CollectionUtils.isEmpty(criteria.getBoundaryCodes())) {
            whereClause.append(" AND boundary_code ILIKE ANY ( ARRAY [ ").append(createQuery(criteria.getBoundaryCodes().size())).append(" ] )");
            params.addAll(criteria.getBoundaryCodes().stream().map((boundaryCode) -> boundaryCode + "%").toList());
        }

        List<Role> currentUserRoles = Optional.ofNullable(requestInfo)
                .map(RequestInfo::getUserInfo)
                .map(User::getRoles)
                .orElse(Collections.emptyList());

        if (currentUserRoles.stream().noneMatch((role -> onmNonReadyAllowedRoles.contains(role.getCode())))) {
            whereClause.append(" AND is_onm_ready = ?");
            params.add(true);

        } else if (criteria.getIsOnmReady() != null) {
            whereClause.append(" AND is_onm_ready = ?");
            params.add(criteria.getIsOnmReady());
        }

        return new QueryBuilderResult(whereClause.toString(), params);
    }

    /**
     * This method returns a string with placeholders equal to the number of values that need to be put inside
     * "IN" clause
     *
     * @param size
     * @return
     */
    public static String createQuery(Integer size) {
        StringBuilder builder = new StringBuilder();

        IntStream.range(0, size).forEach(i -> {
            builder.append(" ?");
            if (i != size - 1)
                builder.append(",");
        });

        return builder.toString();
    }
}
