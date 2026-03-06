package digit.util;

import com.fasterxml.jackson.databind.JsonNode;
import digit.repository.BoundaryHierarchyRepository;
import digit.repository.querybuilder.BoundaryHierarchyTypeQueryBuilder;
import digit.web.models.BoundaryTypeHierarchy;
import digit.web.models.BoundaryTypeHierarchyDefinition;
import digit.web.models.BoundaryTypeHierarchySearchCriteria;
import org.egov.tracer.model.CustomException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.IntStream;

@Component
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
        List<BoundaryTypeHierarchyDefinition> boundaryTypeHierarchyDefinitionList = boundaryHierarchyRepository.search(BoundaryTypeHierarchySearchCriteria.builder()
                .tenantId(tenantId)
                .hierarchyType(hierarchyType)
                .build());

        if(CollectionUtils.isEmpty(boundaryTypeHierarchyDefinitionList)) {
            throw new CustomException("HIERARCHY_DEFINITION_DOES_NOT_EXIST_ERR", "Hierarchy definition does not exist");
        }

        List<BoundaryTypeHierarchy> boundaryTypeHierarchyList = boundaryTypeHierarchyDefinitionList.get(0).getBoundaryHierarchy();

        Map<String, String> parentToChildMap = prepareParentToChildMap(boundaryTypeHierarchyList);

        List<String> hierarchyOrder = new ArrayList<>();

        String rootHierarchyNode = boundaryTypeHierarchyList
                .stream()
                .filter(hierarchyNode -> ObjectUtils.isEmpty(hierarchyNode.getParentBoundaryType()))
                .findFirst()
                .get()
                .getBoundaryType();

        hierarchyOrder.add(rootHierarchyNode);

        IntStream.range(0, boundaryTypeHierarchyList.size() - 1).forEach(i -> {
            hierarchyOrder.add(parentToChildMap.get(hierarchyOrder.get(i)));
        });

        return hierarchyOrder;
    }

    private Map<String, String> prepareParentToChildMap(List<BoundaryTypeHierarchy> boundaryTypeHierarchyList) {
        Map<String, String> parentToChildMap = new HashMap<>();

        boundaryTypeHierarchyList.forEach(boundaryTypeHierarchy -> {
            if(!ObjectUtils.isEmpty(boundaryTypeHierarchy.getParentBoundaryType())) {
                parentToChildMap.put(boundaryTypeHierarchy.getParentBoundaryType(), boundaryTypeHierarchy.getBoundaryType());
            }
        });

        return parentToChildMap;
    }

    /**
     * This method gives the total count of hierarchy definition based on the search criteria.
     * @param boundaryTypeHierarchySearchCriteria
     * @return
     */
    public Integer getBoundaryTypeHierarchyDefinitionCount(BoundaryTypeHierarchySearchCriteria boundaryTypeHierarchySearchCriteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = boundaryHierarchyTypeQueryBuilder.getBoundaryHierarchyTypeCountQuery(boundaryTypeHierarchySearchCriteria, preparedStmtList);
        return jdbcTemplate.queryForObject(query, preparedStmtList.toArray(), Integer.class);
    }

    // Output India_AndamanandNicobarIslands: → AN, India_Telangana → TE, India_Assam_Biswanath → AB
    public static String boundaryCodeToCode(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }

        // Nettoyage
        String cleaned = input.trim();

        // Supprimer "India_" si présent
        if (cleaned.startsWith("India_")) {
            cleaned = cleaned.substring("India_".length());
        }

        // Supprimer ":" et tout ce qui suit
        int colonIndex = cleaned.indexOf(":");
        if (colonIndex >= 0) {
            cleaned = cleaned.substring(0, colonIndex);
        }

        // Enlever underscores
        cleaned = cleaned.replace("_", "");

        // Split CamelCase
        String[] words = cleaned.split("(?=[A-Z])");

        // Construire le code
        StringBuilder code = new StringBuilder();

        if (words.length >= 2) {
            code.append(Character.toUpperCase(words[0].charAt(0)));
            code.append(Character.toUpperCase(words[1].charAt(0)));
        } else if (words.length == 1 && words[0].length() >= 2) {
            code.append(Character.toUpperCase(words[0].charAt(0)));
            code.append(Character.toUpperCase(words[0].charAt(1)));
        } else if (words.length == 1) {
            code.append(Character.toUpperCase(words[0].charAt(0)));
        }

        return code.toString();
    }

    // Only use for state boundary code
    public String boundaryCodeToName(String boundaryCode) {
        if (boundaryCode == null || boundaryCode.isBlank()) {
            return "";
        }

        // Nettoyage
        String cleaned = boundaryCode.trim();

        // Supprimer "India_" si présent
        if (cleaned.startsWith("India_")) {
            cleaned = cleaned.substring("India_".length());
        }

        // Remplacer _ par espace
        cleaned = cleaned.replace("_", " ");

        // Ajouter des espaces avant les majuscules (CamelCase)
        cleaned = cleaned.replaceAll("(?<=[a-z])(?=[A-Z])", " ");

        // Normaliser les espaces multiples
        cleaned = cleaned.replaceAll("\\s+", " ").trim();

        // Mettre en forme (Majuscule au début de chaque mot)
        String[] words = cleaned.split(" ");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }

        return result.toString().trim();
    }

    public boolean isValidStateBoundaryFormat(String boundary) {
        if (boundary == null || boundary.trim().isEmpty()) {
            return false;
        }

        return boundary.matches("^India_([A-Z][a-z]+)+$");
    }

    public boolean hasOnlyCountryAndState(JsonNode boundaryNode) {

        if (boundaryNode == null) {
            return false;
        }

        JsonNode geographyDetails = boundaryNode
                .path("geographyDetails");

        if (geographyDetails.isMissingNode() || !geographyDetails.isObject()) {
            return false;
        }

        Set<String> allowedFields = Set.of("country", "state");

        Iterator<String> fieldNames = geographyDetails.fieldNames();
        while (fieldNames.hasNext()) {
            String field = fieldNames.next();
            if (!allowedFields.contains(field)) {
                return false; // ex: district, block, etc.
            }
        }

        // Vérifie aussi que country et state existent bien
        return geographyDetails.hasNonNull("country")
                && geographyDetails.hasNonNull("state");
    }

}
