package com.healthcare.appointment.entity;
import com.healthcare.model.appointment.AppointmentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(
        name = "appointments",
        uniqueConstraints ={
                @UniqueConstraint(name =  "uk_doctor_date_time",
                                  columnNames = {"doctor_name", "appointment_date", "appointment_time"}
                )
        } )
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long patientId;
    private String patientName;
    private String patientEmail;
    @Column(name = "doctor_name", nullable = false)
    private String doctorName;

    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @Column(name = "appointment_time", nullable = false)
    private LocalTime appointmentTime;

    private String reason;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = AppointmentStatus.PENDING;
        }
    }
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

