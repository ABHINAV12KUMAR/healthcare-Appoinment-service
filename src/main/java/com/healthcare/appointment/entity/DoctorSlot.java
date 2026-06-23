package com.healthcare.appointment.entity;
import com.healthcare.model.appointment.SlotStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(
        name = "doctor_slots",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_doctor_slot",
                        columnNames = {"doctor_name", "slot_date", "slot_time"}
                )
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "doctor_name", nullable = false)
    private String doctorName;

    @Column(name = "slot_date", nullable = false)
    private LocalDate slotDate;

    @Column(name = "slot_time", nullable = false)
    private LocalTime slotTime;

    @Enumerated(EnumType.STRING)
    private SlotStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = SlotStatus.AVAILABLE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}