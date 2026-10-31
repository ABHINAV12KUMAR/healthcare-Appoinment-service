package com.healthcare.appointment.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class ObservabilityConfig {

    @Bean
    @Profile("prod")
    public MeterBinder kafkaMetricsBinder() {
        return registry -> {
            registry.gauge("kafka.consumer.lag", 0);
        };
    }
}
