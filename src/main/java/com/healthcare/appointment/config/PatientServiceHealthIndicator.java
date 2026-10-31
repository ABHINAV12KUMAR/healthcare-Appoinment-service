package com.healthcare.appointment.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class PatientServiceHealthIndicator implements HealthIndicator {

    private final RestClient patientQueryRestClient;

    @Override
    public Health health() {
        try {
            long startTime = System.currentTimeMillis();
            
            patientQueryRestClient.get()
                    .uri("/actuator/health")
                    .retrieve()
                    .toBodilessEntity();
            
            long responseTime = System.currentTimeMillis() - startTime;
            
            return Health.up()
                    .withDetail("service", "patient-query-service")
                    .withDetail("responseTime", responseTime + "ms")
                    .withDetail("status", "UP")
                    .build();
                    
        } catch (Exception e) {
            log.error("Patient Query Service health check failed", e);
            return Health.down()
                    .withDetail("service", "patient-query-service")
                    .withDetail("error", e.getMessage())
                    .withDetail("status", "DOWN")
                    .build();
        }
    }
}
