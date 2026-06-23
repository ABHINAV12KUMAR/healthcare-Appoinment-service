package com.healthcare.appointment.controller;

import com.healthcare.appointment.dto.DoctorSlotRequest;
import com.healthcare.appointment.service.DoctorSlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/doctor-slots")
@RequiredArgsConstructor
public class DoctorSlotController {
    private final DoctorSlotService doctorSlotService;

    @PostMapping
    public ResponseEntity<String> createSlot(
            @Valid @RequestBody DoctorSlotRequest request
    ) {
        doctorSlotService.createDoctorSlot(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Doctor slot created successfully");
    }
}
