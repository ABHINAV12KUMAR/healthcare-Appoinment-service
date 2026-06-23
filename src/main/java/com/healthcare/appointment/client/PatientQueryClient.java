package com.healthcare.appointment.client;

import com.healthcare.appointment.dto.PatientInfoResponse;
import com.healthcare.appointment.exception.PatientServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class PatientQueryClient {

    private static final String CIRCUIT_BREAKER_PATIENT_QUERY_SERVICE = "patientQueryService";

    private final RestClient patientQueryRestClient;

    @CircuitBreaker(
            name = CIRCUIT_BREAKER_PATIENT_QUERY_SERVICE,
            fallbackMethod = "getPatientByIdFallback"
    )
    public PatientInfoResponse getPatientById(Long patientId) {

        log.debug("Calling Patient Query Service. patientId={}", patientId);

        PatientInfoResponse response = patientQueryRestClient.get()
                .uri("/api/v1/patients/{patientId}", patientId)
                .retrieve()
                .body(PatientInfoResponse.class);

        if (response == null) {
            throw new PatientServiceUnavailableException("Empty response received from Patient Query Service");
        }

        log.debug("Patient Query Service call successful. patientId={}", patientId);
        return response;
    }

    public PatientInfoResponse getPatientByIdFallback(Long patientId, Throwable exception) {

        log.error("Patient Query Service unavailable. patientId={}, error={}",
                patientId,
                exception.getMessage(),
                exception);

        throw new PatientServiceUnavailableException(
                "Patient validation service is temporarily unavailable. Please try again later."
        );
    }
}