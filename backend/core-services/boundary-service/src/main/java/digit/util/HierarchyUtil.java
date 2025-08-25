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
        log.debug("Fetching hierarchy order for tenantId: {}, hierarchyType: {}", tenantId, hierarchyType);
        List<BoundaryTypeHierarchyDefinition> boundaryTypeHierarchyDefinitionList = boundaryHierarchyRepository.search(BoundaryTypeHierarchySearchCriteria.builder()
                .tenantId(tenantId)
                .hierarchyType(hierarchyType)
                .build());

        if(CollectionUtils.isEmpty(boundaryTypeHierarchyDefinitionList)) {
            throw new CustomException("HIERARCHY_DEFINITION_DOES_NOT_EXIST_ERR", "Hierarchy definition does not exist");
        }

        List<BoundaryTypeHierarchy> boundaryTypeHierarchyList = boundaryTypeHierarchyDefinitionList.get(0).getBoundaryHierarchy();
        log.debug("Fetched hierarchy definition list, size: {}", boundaryTypeHierarchyList.size());

        Map<String, String> parentToChildMap = prepareParentToChildMap(boundaryTypeHierarchyList);
        log.debug("Prepared parent-to-child map: {}", parentToChildMap);

        List<String> hierarchyOrder = new ArrayList<>();

        String rootHierarchyNode = boundaryTypeHierarchyList
                .stream()
                .filter(hierarchyNode -> ObjectUtils.isEmpty(hierarchyNode.getParentBoundaryType()))
                .findFirst()
                .get()
                .getBoundaryType();

        log.debug("Identified root hierarchy node: {}", rootHierarchyNode);
        hierarchyOrder.add(rootHierarchyNode);

        IntStream.range(0, boundaryTypeHierarchyList.size() - 1).forEach(i -> {
            hierarchyOrder.add(parentToChildMap.get(hierarchyOrder.get(i)));
        });

        log.debug("Final hierarchy order: {}", hierarchyOrder);
        return hierarchyOrder;
    }

    private Map<String, String> prepareParentToChildMap(List<BoundaryTypeHierarchy> boundaryTypeHierarchyList) {
        log.debug("Preparing parent-to-child map from boundaryTypeHierarchyList size: {}", boundaryTypeHierarchyList.size());
        Map<String, String> parentToChildMap = new HashMap<>();

        boundaryTypeHierarchyList.forEach(boundaryTypeHierarchy -> {
            if(!ObjectUtils.isEmpty(boundaryTypeHierarchy.getParentBoundaryType())) {
                log.trace("Mapping parent: {} -> child: {}", boundaryTypeHierarchy.getParentBoundaryType(), boundaryTypeHierarchy.getBoundaryType());
                parentToChildMap.put(boundaryTypeHierarchy.getParentBoundaryType(), boundaryTypeHierarchy.getBoundaryType());
            }
        });

        log.debug("Completed parent-to-child map preparation.");
        return parentToChildMap;
    }

    /**
     * This method gives the total count of hierarchy definition based on the search criteria.
     * @param boundaryTypeHierarchySearchCriteria
     * @return
     */
    public Integer getBoundaryTypeHierarchyDefinitionCount(BoundaryTypeHierarchySearchCriteria boundaryTypeHierarchySearchCriteria) {
        log.debug("Fetching hierarchy definition count for criteria: {}", boundaryTypeHierarchySearchCriteria);
        List<Object> preparedStmtList = new ArrayList<>();
        String query = boundaryHierarchyTypeQueryBuilder.getBoundaryHierarchyTypeCountQuery(boundaryTypeHierarchySearchCriteria, preparedStmtList);
        log.debug("Generated SQL Query: {}", query);
        log.trace("Prepared Statement List: {}", preparedStmtList);
        log.debug("Fetched hierarchy definition count: {}", jdbcTemplate.queryForObject(query, preparedStmtList.toArray(), Integer.class));
        return jdbcTemplate.queryForObject(query, preparedStmtList.toArray(), Integer.class);
    }
}
