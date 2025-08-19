package org.egov.wf.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.wf.config.WorkflowConfig;
import org.egov.wf.producer.Producer;
import org.egov.wf.repository.PauseStateRepository;
import org.egov.wf.util.WorkflowUtil;
import org.egov.wf.web.models.PauseState;
import org.egov.wf.web.models.PauseStateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PauseStateService {

    private PauseStateRepository pauseStateRepository;
    private WorkflowUtil workflowUtil;
    private Producer producer;
    private WorkflowConfig config;

    @Autowired
    public PauseStateService(PauseStateRepository pauseStateRepository, WorkflowUtil workflowUtil,
                            Producer producer, WorkflowConfig config) {
        this.pauseStateRepository = pauseStateRepository;
        this.workflowUtil = workflowUtil;
        this.producer = producer;
        this.config = config;
    }

    /**
     * Saves or updates pause states using Kafka producer
     * @param pauseStateRequest The request containing pause states
     * @return The list of pause states with audit details set
     */
    public List<PauseState> saveOrUpdate(PauseStateRequest pauseStateRequest) {
        List<PauseState> updatedPauseStates = new ArrayList<>();
        
        for (PauseState pauseState : pauseStateRequest.getPauseStates()) {
            // Set audit details (created only once, modified every time)
            if (pauseState.getAuditDetails() == null) {
                pauseState.setAuditDetails(workflowUtil.getAuditDetails(pauseStateRequest.getRequestInfo().getUserInfo().getUuid(), true));
            } else {
                pauseState.setAuditDetails(workflowUtil.getAuditDetails(pauseStateRequest.getRequestInfo().getUserInfo().getUuid(), false));
            }
            
            updatedPauseStates.add(pauseState);
        }

        // Push to Kafka
        producer.push(config.getSavePauseStateTopic(), pauseStateRequest);
        
        // Return the pause states with audit details (actual persistence happens via persister)
        return updatedPauseStates;
    }

    /**
     * Finds pause state by businessId and businessService
     * @param businessId The business ID
     * @param businessService The business service
     * @return The pause state if found, null otherwise
     */
    public PauseState findByBusinessIdAndBusinessService(String businessId, String businessService) {
        return pauseStateRepository.findByBusinessIdAndBusinessService(businessId, businessService);
    }

    /**
     * Finds all pause states for given business IDs and business service
     * @param businessIds List of business IDs
     * @param businessService The business service
     * @return List of pause states
     */
    public List<PauseState> findByBusinessIdsAndBusinessService(List<String> businessIds, String businessService) {
        return pauseStateRepository.findByBusinessIdsAndBusinessService(businessIds, businessService);
    }

    /**
     * Checks if a workflow is paused
     * @param businessId The business ID
     * @param businessService The business service
     * @return true if paused, false otherwise
     */
    public boolean isPaused(String businessId, String businessService) {
        PauseState pauseState = findByBusinessIdAndBusinessService(businessId, businessService);
        return pauseState != null && pauseState.getIsPaused() != null && pauseState.getIsPaused();
    }
}
