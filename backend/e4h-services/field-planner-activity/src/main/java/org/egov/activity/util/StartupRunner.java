package org.egov.activity.util;

import org.egov.activity.config.ActivityConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class StartupRunner implements CommandLineRunner {

    private final ActivityConfiguration activityConfiguration;
    private final Map<String, String> configMap = new HashMap<>();

    public StartupRunner(ActivityConfiguration activityConfiguration) {
        this.activityConfiguration = activityConfiguration;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("➡ Application démarrée !");
        configMap.put("AC_OFF_GRID_RMS_SINGLE_PHASE", activityConfiguration.getBomACOffGridSinglePhase());
        configMap.put("AC_OFF_GRID_RMS_THREE_PHASE", activityConfiguration.getBomACOffGridSThreePhase());
        configMap.put("HYBRID_RMS_SINGLE_PHASE", activityConfiguration.getBomHybridSinglePhase());
        configMap.put("HYBRID_RMS_SINGLE_PHASE", activityConfiguration.getBomHybridThreePhase());
        configMap.put("DC_SYSTEM", activityConfiguration.getBomDCSystem());
    }

    public Map<String, String> getConfigMap() {
        return configMap;
    }
}

