package digit.util;

import digit.repository.BoundaryHierarchyRepository;
import digit.repository.querybuilder.BoundaryHierarchyTypeQueryBuilder;
import digit.web.models.BoundaryTypeHierarchy;
import digit.web.models.BoundaryTypeHierarchyDefinition;
import digit.web.models.BoundaryTypeHierarchySearchCriteria;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Component
@Slf4j
public class HierarchyUtil {

    private BoundaryHierarchyRepository boundaryHierarchyRepository;

    private BoundaryHierarchyTypeQueryBuilder boundaryHierarchyTypeQueryBuilder;

    private JdbcTemplate jdbcTemplate;

    public HierarchyUtil(BoundaryHierarchyRepository boundaryHierarchyRepository, BoundaryHierarchyTypeQueryBuilder boundaryHierarchyTypeQueryBuilder, JdbcTemplate jdbcTemplate) {
        this.boundaryHierarchyRepository = boundaryHierarchyRepository;
        this.boundaryHierarchyTypeQueryBuilder = boundaryHierarchyTypeQueryBuilder;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * This method gives the hierarchy order from hierarchy definition.
     * @param tenantId
     * @param hierarchyType
     * @return
     */
    public List<String> getHierarchyOrder(String tenantId, String hierarchyType) {
        log.trace("getHierarchyOrder method invoked, tenantId={}, hierarchyType={}", tenantId, hierarchyType);
        log.debug("Searching for hierarchy definition");
        
        List<BoundaryTypeHierarchyDefinition> boundaryTypeHierarchyDefinitionList = boundaryHierarchyRepository.search(BoundaryTypeHierarchySearchCriteria.builder()
                .tenantId(tenantId)
                .hierarchyType(hierarchyType)
                .build());

        if(CollectionUtils.isEmpty(boundaryTypeHierarchyDefinitionList)) {
            log.warn("Hierarchy definition not found, tenantId={}, hierarchyType={}", tenantId, hierarchyType);
            throw new CustomException("HIERARCHY_DEFINITION_DOES_NOT_EXIST_ERR", "Hierarchy definition does not exist");
        }

        log.debug("Found hierarchy definition, building hierarchy order");
        List<BoundaryTypeHierarchy> boundaryTypeHierarchyList = boundaryTypeHierarchyDefinitionList.get(0).getBoundaryHierarchy();

        Map<String, String> parentToChildMap = prepareParentToChildMap(boundaryTypeHierarchyList);
        log.debug("Prepared parent-to-child map, size={}", parentToChildMap.size());

        List<String> hierarchyOrder = new ArrayList<>();

        String rootHierarchyNode = boundaryTypeHierarchyList
                .stream()
                .filter(hierarchyNode -> ObjectUtils.isEmpty(hierarchyNode.getParentBoundaryType()))
                .findFirst()
                .get()
                .getBoundaryType();

        log.debug("Found root hierarchy node={}", rootHierarchyNode);
        hierarchyOrder.add(rootHierarchyNode);

        IntStream.range(0, boundaryTypeHierarchyList.size() - 1).forEach(i -> {
            hierarchyOrder.add(parentToChildMap.get(hierarchyOrder.get(i)));
        });

        log.debug("Hierarchy order built, size={}", hierarchyOrder.size());
        return hierarchyOrder;
    }

    private Map<String, String> prepareParentToChildMap(List<BoundaryTypeHierarchy> boundaryTypeHierarchyList) {
        log.trace("prepareParentToChildMap method invoked, hierarchy list size={}", boundaryTypeHierarchyList != null ? boundaryTypeHierarchyList.size() : 0);
        Map<String, String> parentToChildMap = new HashMap<>();

        boundaryTypeHierarchyList.forEach(boundaryTypeHierarchy -> {
            if(!ObjectUtils.isEmpty(boundaryTypeHierarchy.getParentBoundaryType())) {
                parentToChildMap.put(boundaryTypeHierarchy.getParentBoundaryType(), boundaryTypeHierarchy.getBoundaryType());
            }
        });

        log.debug("Parent-to-child map prepared, entries count={}", parentToChildMap.size());
        return parentToChildMap;
    }

    /**
     * This method gives the total count of hierarchy definition based on the search criteria.
     * @param boundaryTypeHierarchySearchCriteria
     * @return
     */
    public Integer getBoundaryTypeHierarchyDefinitionCount(BoundaryTypeHierarchySearchCriteria boundaryTypeHierarchySearchCriteria) {
        log.trace("getBoundaryTypeHierarchyDefinitionCount method invoked");
        log.debug("Getting hierarchy definition count, tenantId={}, hierarchyType={}", 
                boundaryTypeHierarchySearchCriteria.getTenantId(),
                boundaryTypeHierarchySearchCriteria.getHierarchyType());
        
        List<Object> preparedStmtList = new ArrayList<>();
        String query = boundaryHierarchyTypeQueryBuilder.getBoundaryHierarchyTypeCountQuery(boundaryTypeHierarchySearchCriteria, preparedStmtList);
        log.debug("Executing count query");
        
        Integer count = jdbcTemplate.queryForObject(query, preparedStmtList.toArray(), Integer.class);
        log.debug("Hierarchy definition count retrieved, count={}", count);
        return count;
    }
}
