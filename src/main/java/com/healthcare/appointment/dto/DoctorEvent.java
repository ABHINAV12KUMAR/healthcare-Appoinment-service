package com.healthcare.appointment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorEvent {
    private Long doctorId;
    private String doctorName;
    private String specialization;
    private String email;
    private String phone;
    private Boolean available;
    private String eventType; // DOCTOR_CREATED, DOCTOR_UPDATED, DOCTOR_DELETED
    private LocalDateTime timestamp;
}
