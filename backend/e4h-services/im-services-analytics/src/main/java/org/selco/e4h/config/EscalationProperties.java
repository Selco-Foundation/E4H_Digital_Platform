package org.selco.e4h.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "escalation")
public class EscalationProperties {

    private Leadership leadership = new Leadership();
    private Vendor vendor = new Vendor();

    @Getter
    @Setter
    public static class Leadership {
        private double nfThresholdPct = 7.0;
    }

    @Getter
    @Setter
    public static class Vendor {
        private String roleCode = "COMPLAINT_RESOLVER";
    }
}
