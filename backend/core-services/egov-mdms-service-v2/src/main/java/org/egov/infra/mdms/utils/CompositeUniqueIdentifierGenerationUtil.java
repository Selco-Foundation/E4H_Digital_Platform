package org.egov.infra.mdms.utils;

import com.fasterxml.jackson.databind.JsonNode;
import org.egov.infra.mdms.model.MdmsRequest;
import org.egov.tracer.model.CustomException;
import org.json.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.IntStream;
import static org.egov.infra.mdms.utils.MDMSConstants.*;

@Slf4j
public class CompositeUniqueIdentifierGenerationUtil {

    private CompositeUniqueIdentifierGenerationUtil(){}

    /**
     * This method creates composite unique identifier based on the attributes provided
     * in x-unique-key param.
     * @param schemaObject
     * @param mdmsRequest
     * @return
     */
    public static String getUniqueIdentifier(JSONObject schemaObject, MdmsRequest mdmsRequest) {
        log.trace("CompositeUniqueIdentifierGenerationUtil.getUniqueIdentifier: method invoked");
        String schemaCode = mdmsRequest.getMdms() != null ? mdmsRequest.getMdms().getSchemaCode() : "null";
        log.debug("Generating unique identifier for schemaCode: {}", schemaCode);
        
        org.json.JSONArray uniqueFieldPaths = (org.json.JSONArray) schemaObject.get(X_UNIQUE_KEY);
        log.debug("Unique field paths count: {}", uniqueFieldPaths != null ? uniqueFieldPaths.length() : 0);

        JsonNode data = mdmsRequest.getMdms().getData();
        StringBuilder compositeUniqueIdentifier = new StringBuilder();

        // Build composite unique identifier
        IntStream.range(0, uniqueFieldPaths.length()).forEach(i -> {
            String fieldPath = uniqueFieldPaths.getString(i);
            String uniqueIdentifierChunk = data.at(getJsonPointerExpressionFromDotSeparatedPath(fieldPath)).asText();

            // Throw error in case value against unique identifier is empty
            if(uniqueIdentifierChunk == null || uniqueIdentifierChunk.isEmpty()) {
                log.error("Empty value found for unique field path: {}", fieldPath);
                throw new CustomException("UNIQUE_IDENTIFIER_EMPTY_ERR", "Values defined against unique fields cannot be empty.");
            }

            compositeUniqueIdentifier.append(uniqueIdentifierChunk);

            if (i != (uniqueFieldPaths.length() - 1))
                compositeUniqueIdentifier.append(DOT_SEPARATOR);
        });

        log.debug("Generated unique identifier successfully");
        return compositeUniqueIdentifier.toString();
    }

    /**
     * This method creates a JSON pointer expression from dot separated path.
     * @param dotSeparatedPath
     * @return
     */
    public static String getJsonPointerExpressionFromDotSeparatedPath(String dotSeparatedPath) {
        log.trace("CompositeUniqueIdentifierGenerationUtil.getJsonPointerExpressionFromDotSeparatedPath: method invoked");
        return FORWARD_SLASH + dotSeparatedPath.replaceAll(DOT_REGEX, FORWARD_SLASH);
    }

    /**
     * This method creates JSON path expression from dot separated path.
     * @param dotSeparatedPath
     * @return
     */
    public static String getJsonPathExpressionFromDotSeparatedPath(String dotSeparatedPath) {
        log.trace("CompositeUniqueIdentifierGenerationUtil.getJsonPathExpressionFromDotSeparatedPath: method invoked");
        return DOLLAR_DOT + dotSeparatedPath;
    }

}
