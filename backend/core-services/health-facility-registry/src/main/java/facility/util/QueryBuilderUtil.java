package facility.util;

import facility.service.FacilityService;
import facility.web.models.FacilityBulkSearchCriteria;
import facility.web.models.FacilitySearchRequest;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
@Slf4j
@Component
public class QueryBuilderUtil {

    @Autowired
    public static FacilityService facilityService;

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
            whereClause.append(" AND hfr_id = ?");
            params.add(request.getHfrId());
        }

        if (request.getNinId() != null && !request.getNinId().isBlank()) {
            whereClause.append(" AND nin_id = ?");
            params.add(request.getNinId());
        }

        if (request.getFacilityPocName() != null && !request.getFacilityPocName().isBlank()) {
            whereClause.append(" AND facility_poc_name ILIKE ?");
            params.add("%" + request.getFacilityPocName() + "%");
        }

        if (request.getFacilityPocPhone() != null && !request.getFacilityPocPhone().isBlank()) {
            String encryptedMobileNumber = facilityService.encryptMobileNumber(request.getFacilityPocPhone());
            whereClause.append(" AND facility_poc_phone = ?");
            params.add(encryptedMobileNumber);
        }

        if (request.getFacilityPocEmail() != null && !request.getFacilityPocEmail().isBlank()) {
            whereClause.append(" AND facility_poc_email = ?");
            params.add(request.getFacilityPocEmail());
        }

        if (request.getFacilityStatus() != null && !request.getFacilityStatus().isBlank()) {
            whereClause.append(" AND facility_status = ?");
            params.add(request.getFacilityStatus());
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
            whereClause.append(" AND fac.tenant_id in ( ").append(createQuery(criteria.getTenantIds().size())).append(" )");
            params.addAll(criteria.getTenantIds());
        }

        if (!CollectionUtils.isEmpty(criteria.getFacilityIds())) {
            whereClause.append(" AND fac.id in ( ").append(createQuery(criteria.getFacilityIds().size())).append(" )");
            params.addAll(criteria.getFacilityIds());
        }

        if (!CollectionUtils.isEmpty(criteria.getFacilityNames())) {
            whereClause.append(" AND fac.facility_name ILIKE ANY ( ARRAY [ ").append(createQuery(criteria.getFacilityNames().size())).append(" ] )");
            params.addAll(criteria.getFacilityNames().stream().map((facilityName) -> "%" + facilityName + "%").toList());
        }

        if (!CollectionUtils.isEmpty(criteria.getHfrIds())) {
            whereClause.append(" AND hfr_id in ( ").append(createQuery(criteria.getHfrIds().size())).append(" )");
            params.addAll(criteria.getHfrIds());
        }

        if (!CollectionUtils.isEmpty(criteria.getNinIds())) {
            whereClause.append(" AND nin_id in ( ").append(createQuery(criteria.getNinIds().size())).append(" )");
            params.addAll(criteria.getNinIds());
        }

        if (!CollectionUtils.isEmpty(criteria.getFacilityPocNames())) {
            whereClause.append(" AND facility_poc_name ILIKE ANY ( ARRAY [ ").append(createQuery(criteria.getFacilityPocNames().size())).append(" ] )");
            params.addAll(criteria.getFacilityPocNames().stream().map((facilityPocName) -> "%" + facilityPocName + "%").toList());
        }

        if (!CollectionUtils.isEmpty(criteria.getFacilityPocPhones())) {
            whereClause.append(" AND facility_poc_phone in ( ").append(createQuery(criteria.getFacilityPocPhones().size())).append(" )");
            params.addAll(criteria.getFacilityPocPhones());
        }

        if (!CollectionUtils.isEmpty(criteria.getFacilityPocEmails())) {
            whereClause.append(" AND facility_poc_email in ( ").append(createQuery(criteria.getFacilityPocEmails().size())).append(" )");
            params.addAll(criteria.getFacilityPocEmails());
        }

        if (!CollectionUtils.isEmpty(criteria.getFacilityStatus())) {
            whereClause.append(" AND facility_status in ( ").append(createQuery(criteria.getFacilityStatus().size())).append(" )");
            params.addAll(criteria.getFacilityStatus());
        }

        if (!CollectionUtils.isEmpty(criteria.getBoundaryCodes())) {
            whereClause.append(" AND boundary_code in ( ").append(createQuery(criteria.getBoundaryCodes().size())).append(" )");
            params.addAll(criteria.getBoundaryCodes());
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
