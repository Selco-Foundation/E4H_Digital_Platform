package org.egov.im.util;

import com.jayway.jsonpath.JsonPath;
import org.egov.im.web.models.Incident;
import org.egov.im.web.models.Priority;
import org.egov.tracer.model.CustomException;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.egov.im.util.IMConstants.MDMS_HF_SERVICEDEF_SEARCH;

/**
 * Facility user observations (SEM 4.0 Ticket Reporting): the Incident.ServiceDefs entries of channel
 * HF. Each one belongs to a component (componentCode) and gives the ticket its incidentType
 * (menuPath), its incidentSubType (serviceCode) and a priority.
 */
public final class HfServiceDefs {

    public static final String SERVICE_CODE = "serviceCode";
    public static final String MENU_PATH = "menuPath";
    public static final String PRIORITY = "priority";

    private HfServiceDefs() {
    }

    /**
     * HF ServiceDefs entries of the component, in MDMS order.
     */
    public static List<Map<String, Object>> forComponent(Object mdmsData, String component) {
        if (mdmsData == null || component == null || component.isBlank()) {
            return Collections.emptyList();
        }
        try {
            List<Map<String, Object>> entries = JsonPath.read(mdmsData, MDMS_HF_SERVICEDEF_SEARCH.replace("{COMPONENT}", component));
            return entries == null ? Collections.emptyList() : entries;
        } catch (Exception e) {
            throw new CustomException("JSONPATH_ERROR",
                    "Failed to parse MDMS ServiceDefs for component: " + component + ". Error: " + e.getMessage());
        }
    }

    /**
     * HF ServiceDefs entries of the incident's observations, in the order the reporter chose them;
     * codes not offered for the component are left out.
     */
    public static List<Map<String, Object>> forObservations(Object mdmsData, Incident incident) {
        if (CollectionUtils.isEmpty(incident.getObservations())) {
            return Collections.emptyList();
        }
        Map<String, Map<String, Object>> byCode = forComponent(mdmsData, incident.getComponent()).stream()
                .filter(entry -> entry.get(SERVICE_CODE) != null)
                .collect(Collectors.toMap(entry -> String.valueOf(entry.get(SERVICE_CODE)), entry -> entry, (a, b) -> a));
        return incident.getObservations().stream()
                .map(byCode::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * The observation that sets the ticket type: the one of highest priority, the first chosen on a tie.
     */
    public static Optional<Map<String, Object>> primaryObservation(Object mdmsData, Incident incident) {
        return forObservations(mdmsData, incident).stream()
                .min(Comparator.comparingInt(entry -> rank(priorityOf(entry))));
    }

    /**
     * Priority of an entry, null when missing or not HIGH, MEDIUM or LOW (any case).
     */
    public static Priority priorityOf(Map<String, Object> entry) {
        Object value = entry.get(PRIORITY);
        if (value == null) {
            return null;
        }
        try {
            return Priority.valueOf(String.valueOf(value).trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // HIGH first; an entry without a valid priority comes last
    private static int rank(Priority priority) {
        return priority == null ? Integer.MAX_VALUE : priority.ordinal();
    }
}
