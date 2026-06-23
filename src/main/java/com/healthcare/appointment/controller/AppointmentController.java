package com.healthcare.appointment.controller;

import com.healthcare.appointment.service.AppointmentService;
import com.healthcare.model.appointment.AppointmentDTO;
import com.healthcare.model.appointment.AppointmentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointment", description = "Appointment booking")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @Operation(summary = "Book a new appointment")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Appointment booked successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "409", description = "Slot already booked"),
            @ApiResponse(responseCode = "503", description = "Patient service unavailable")
    })
    @PostMapping
    public ResponseEntity<AppointmentResponse> bookAppointment(
            @Valid @RequestBody AppointmentDTO appointmentDTO) {

        log.info("Appointment booking request received. patientId={}, doctorName={}",
                appointmentDTO.getPatientId(),
                appointmentDTO.getDoctorName());

        AppointmentResponse response = appointmentService.bookAppointment(appointmentDTO);

        log.info("Appointment booking response sent. appointmentId={}",
                response.getAppointmentId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}