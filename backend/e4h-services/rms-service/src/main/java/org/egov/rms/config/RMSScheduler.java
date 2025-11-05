package org.egov.rms.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.rms.service.RMSOrchestratorService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RMSScheduler {

    private final RMSOrchestratorService orchestratorService;
    private final RMSConfiguration config;

    /**
     * Scheduled job to collect data and apply rules every 15 minutes
     */
    @Scheduled(cron = "${rms.scheduler.rule.engine.cron:0 */15 * * * *}", zone = "Asia/Kolkata")
    @ConditionalOnProperty(value = "rms.scheduler.rule.engine.enabled", havingValue = "true", matchIfMissing = true)
    public void executeRuleEngine() {
        log.info("Starting scheduled rule engine execution");
        try {
            orchestratorService.executeWorkflow();
        } catch (Exception e) {
            log.error("Error in scheduled rule engine execution", e);
        }
        log.info("Completed scheduled rule engine execution");
    }

    /**
     * Scheduled job for daily solar data analysis (runs at 1 AM)
     */
    @Scheduled(cron = "${rms.scheduler.solar.daily.cron:0 0 1 * * *}", zone = "Asia/Kolkata")
    @ConditionalOnProperty(value = "rms.scheduler.solar.daily.enabled", havingValue = "true", matchIfMissing = true)
    public void executeSolarDailyAnalysis() {
        log.info("Starting scheduled daily solar analysis");
        try {
            // This will trigger panel-level analysis which requires 7 days of data
            orchestratorService.executeWorkflow();
        } catch (Exception e) {
            log.error("Error in scheduled daily solar analysis", e);
        }
        log.info("Completed scheduled daily solar analysis");
    }
}

