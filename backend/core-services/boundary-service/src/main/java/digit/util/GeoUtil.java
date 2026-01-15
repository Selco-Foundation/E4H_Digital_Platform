package digit.util;

import digit.errors.ErrorCodes;
import digit.web.models.PointGeometry;
import digit.web.models.PolygonGeometry;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class GeoUtil {

    private GeoUtil() {}

    public static void validatePointGeometry(PointGeometry pointGeometry) {
        log.trace("validatePointGeometry method invoked");
        log.debug("Validating point geometry");
        validatePositions(Collections.singletonList(pointGeometry.getCoordinates()));
        log.debug("Point geometry validation completed successfully");
    }

    public static void validatePolygonGeometry(PolygonGeometry polygonGeometry) {
        log.trace("validatePolygonGeometry method invoked");
        log.debug("Validating polygon geometry");
        validateIfPolygonIsSimple(polygonGeometry.getCoordinates());
        validatePositions(polygonGeometry.getCoordinates().get(0));
        validateIfPolygonIsClosed(polygonGeometry.getCoordinates().get(0));
        log.debug("Polygon geometry validation completed successfully");
    }

    private static void validateIfPolygonIsSimple(List<List<List<Double>>> coordinates) {
        log.trace("validateIfPolygonIsSimple method invoked");
        if(coordinates.size() != 1) {
            log.warn("Invalid polygon: expected 1 ring, found {}", coordinates.size());
            throw new CustomException(ErrorCodes.INVALID_POLYGON_CODE,ErrorCodes.INVALID_POLYGON_MSG);
        }

        if(coordinates.get(0).size() < 5) {
            log.warn("Invalid polygon: insufficient coordinates, required 5, found {}", coordinates.get(0).size());
            throw new CustomException(ErrorCodes.INVALID_POLYGON_COORDINATES_DEFINITION_CODE, ErrorCodes.INVALID_POLYGON_COORDINATES_DEFINITION_MSG);
        }
        log.debug("Polygon simplicity validation passed");
    }

    private static void validatePositions(List<List<Double>> coordinatesList) {
        log.trace("validatePositions method invoked, coordinates count={}", coordinatesList != null ? coordinatesList.size() : 0);
        coordinatesList.forEach(coordinate -> {
            if(coordinate.size() != 2) {
                log.warn("Invalid position: expected 2 coordinates, found {}", coordinate.size());
                throw new CustomException(ErrorCodes.INVALID_POSITION_CODE, ErrorCodes.INVALID_POSITION_MSG);
            }
        });
        log.debug("Position validation passed for {} coordinates", coordinatesList.size());
    }

    private static void validateIfPolygonIsClosed(List<List<Double>> coordinatesList) {
        log.trace("validateIfPolygonIsClosed method invoked, coordinates count={}", coordinatesList != null ? coordinatesList.size() : 0);
        if(coordinatesList.size() >= 5) {
            List<Double> startCoordinate = coordinatesList.get(0);
            List<Double> endCoordinate = coordinatesList.get(coordinatesList.size() - 1);
            if(!Objects.equals(startCoordinate.get(0), endCoordinate.get(0)) || !Objects.equals(startCoordinate.get(1), endCoordinate.get(1))) {
                log.warn("Invalid polygon: polygon is not closed, start coordinate={}, end coordinate={}", startCoordinate, endCoordinate);
                throw new CustomException(ErrorCodes.INVALID_POLYGON_DEFINITION_CODE, ErrorCodes.INVALID_POLYGON_DEFINITION_MSG);
            }
            log.debug("Polygon closure validation passed");
        } else {
            log.debug("Skipping polygon closure validation: insufficient coordinates");
        }
    }

}
