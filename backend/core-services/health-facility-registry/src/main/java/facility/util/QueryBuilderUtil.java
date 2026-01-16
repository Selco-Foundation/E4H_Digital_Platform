package facility.util;

import facility.web.models.FacilityBulkSearchCriteria;
import facility.web.models.FacilitySearchRequest;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Slf4j
public class QueryBuilderUtil {

    public static QueryBuilderResult buildWhereClause(FacilitySearchRequest request) {
        log.trace("Entering buildWhereClause method");
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

        log.debug("Built WHERE clause with {} parameters", params.size());
        log.trace("Exiting buildWhereClause method");
        return new QueryBuilderResult(whereClause.toString(), params);
    }

    public static QueryBuilderResult buildBulkWhereClause(FacilityBulkSearchCriteria criteria, RequestInfo requestInfo, List<String> onmNonReadyAllowedRoles) {
        log.trace("Entering buildBulkWhereClause method");
        StringBuilder whereClause = new StringBuilder(" WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (!CollectionUtils.isEmpty(criteria.getTenantIds())) {
            whereClause.append(" AND tenant_id in ( ").append(createQuery(criteria.getTenantIds().size())).append(" )");
            params.addAll(criteria.getTenantIds());
            log.debug("Added {} tenant IDs to WHERE clause", criteria.getTenantIds().size());
        }

        if (!CollectionUtils.isEmpty(criteria.getFacilityIds())) {
            whereClause.append(" AND id in ( ").append(createQuery(criteria.getFacilityIds().size())).append(" )");
            params.addAll(criteria.getFacilityIds());
            log.debug("Added {} facility IDs to WHERE clause", criteria.getFacilityIds().size());
        }

        if (!CollectionUtils.isEmpty(criteria.getFacilityNames())) {
            whereClause.append(" AND facility_name ILIKE ANY ( ARRAY [ ").append(createQuery(criteria.getFacilityNames().size())).append(" ] )");
            params.addAll(criteria.getFacilityNames().stream().map((facilityName) -> "%" + facilityName + "%").toList());
            log.debug("Added {} facility names to WHERE clause", criteria.getFacilityNames().size());
        }

        if (!CollectionUtils.isEmpty(criteria.getHfrIds())) {
            whereClause.append(" AND facility_details ->> 'hfr_id' in ( ").append(createQuery(criteria.getHfrIds().size())).append(" )");
            params.addAll(criteria.getHfrIds());
            log.debug("Added {} HFR IDs to WHERE clause", criteria.getHfrIds().size());
        }

        if (!CollectionUtils.isEmpty(criteria.getNinIds())) {
            whereClause.append(" AND facility_details ->> 'nin_id' in ( ").append(createQuery(criteria.getNinIds().size())).append(" )");
            params.addAll(criteria.getNinIds());
            log.debug("Added {} NIN IDs to WHERE clause", criteria.getNinIds().size());
        }

        if (!CollectionUtils.isEmpty(criteria.getBoundaryCodes())) {
            whereClause.append(" AND boundary_code in ( ").append(createQuery(criteria.getBoundaryCodes().size())).append(" )");
            params.addAll(criteria.getBoundaryCodes());
            log.debug("Added {} boundary codes to WHERE clause", criteria.getBoundaryCodes().size());
        }

        List<Role> currentUserRoles = Optional.ofNullable(requestInfo)
                .map(RequestInfo::getUserInfo)
                .map(User::getRoles)
                .orElse(Collections.emptyList());

        if (currentUserRoles.stream().noneMatch((role -> onmNonReadyAllowedRoles.contains(role.getCode())))) {
            whereClause.append(" AND is_onm_ready = ?");
            params.add(true);
            log.debug("Added is_onm_ready filter (true) based on user roles");

        } else if (criteria.getIsOnmReady() != null) {
            whereClause.append(" AND is_onm_ready = ?");
            params.add(criteria.getIsOnmReady());
            log.debug("Added is_onm_ready filter: {}", criteria.getIsOnmReady());
        }

        log.debug("Built bulk WHERE clause with {} parameters", params.size());
        log.trace("Exiting buildBulkWhereClause method");
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
        log.trace("Entering createQuery method with size: {}", size);
        StringBuilder builder = new StringBuilder();

        IntStream.range(0, size).forEach(i -> {
            builder.append(" ?");
            if (i != size - 1)
                builder.append(",");
        });

        String result = builder.toString();
        log.trace("Exiting createQuery method");
        return result;
    }
}
