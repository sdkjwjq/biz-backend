package org.example.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Clock;

@Configuration
public class WorkRecordConfig {
    @Bean
    @ConditionalOnMissingBean(name="workRecordClock")
    public Clock workRecordClock() {
        return Clock.systemUTC();
    }
}
