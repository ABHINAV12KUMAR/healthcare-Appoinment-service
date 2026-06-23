package com.healthcare.appointment.dto;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class PatientInfoResponse {
    @NotNull
    private Long id;
    @NotNull
    private String name;
    @NotNull
    private String email;
    @NotNull
    private String disease;
}
