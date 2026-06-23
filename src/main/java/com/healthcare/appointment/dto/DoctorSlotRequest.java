package com.healthcare.appointment.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class DoctorSlotRequest {
    @NotBlank(message = "Doctor name is required")
    private String doctorName;

    @NotNull(message = "Slot date is required")
    @FutureOrPresent(message = "Slot date cannot be in the past")
    private LocalDate slotDate;

    @NotNull(message = "Slot time is required")
    private LocalTime slotTime;
}
