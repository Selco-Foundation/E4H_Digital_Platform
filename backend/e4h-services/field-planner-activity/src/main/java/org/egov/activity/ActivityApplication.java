package org.egov.activity;

import org.cache2k.extra.spring.SpringCache2kCacheManager;
import org.egov.tracer.config.TracerConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableCaching
@Import({TracerConfiguration.class})
public class ActivityApplication {
    public static void main(String[] args) {
        SpringApplication.run(ActivityApplication.class, args);
    }

    @Value("${cache.expiry.minutes}")
    private int cacheExpiry;

//    @Bean
//    @Profile("!test")
//    public CacheManager cacheManager(){
//        return new SpringCache2kCacheManager().addCaches(b->b.name("boundaryConfiguration").expireAfterWrite(cacheExpiry, TimeUnit.MINUTES)
//                .entryCapacity(10));
//    }
}
