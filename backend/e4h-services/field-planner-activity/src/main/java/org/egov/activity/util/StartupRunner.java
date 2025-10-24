package org.egov.activity.util;

import lombok.extern.slf4j.Slf4j;
import org.egov.activity.config.ActivityConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class StartupRunner implements CommandLineRunner {

    private final ActivityConfiguration activityConfiguration;
    private final Map<String, String> configMap = new HashMap<>();

    public StartupRunner(ActivityConfiguration activityConfiguration) {
        this.activityConfiguration = activityConfiguration;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Application started - BOM configuration loaded");
        configMap.put("AC_OFF_GRID", activityConfiguration.getBomACOffGridSinglePhase());
        configMap.put("AC_OFF_GRID_THREE_PHASE", activityConfiguration.getBomACOffGridSThreePhase());
        configMap.put("HYBRID_SINGLE_PHASE", activityConfiguration.getBomHybridSinglePhase());
        configMap.put("HYBRID_THREE_PHASE", activityConfiguration.getBomHybridThreePhase());
        configMap.put("DC", activityConfiguration.getBomDCSystem());
    }

    public Map<String, String> getConfigMap() {
        return configMap;
    }
}

