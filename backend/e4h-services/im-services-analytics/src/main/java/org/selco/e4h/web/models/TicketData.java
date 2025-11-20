package org.selco.e4h.web.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Model class for ticket data extracted from Elasticsearch
 * Used to eliminate code duplication in WeeklyReportService
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketData {
    
    /**
     * Tenant ID of the ticket
     */
    private String tenantId;
    
    /**
     * Date when the ticket was filed (timestamp in milliseconds)
     */
    private Long filedDate;
    
    /**
     * System functional status (FUNCTIONAL, NON_FUNCTIONAL)
     */
    private String systemFunctional;
    
    /**
     * State name
     */
    private String state;
    
    /**
     * Raw data object from Elasticsearch
     */
    private Map<String, Object> data;
}
