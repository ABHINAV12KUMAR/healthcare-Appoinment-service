package com.healthcare.appointment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.appointment.dto.DoctorSlotRequest;
import com.healthcare.appointment.service.DoctorSlotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DoctorSlotController.class)
class DoctorSlotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DoctorSlotService doctorSlotService;

    private DoctorSlotRequest request;

    @BeforeEach
    void setUp() {
        request = new DoctorSlotRequest();
        request.setDoctorName("Dr. John Doe");
        request.setSlotDate(LocalDate.now().plusDays(1));
        request.setSlotTime(LocalTime.of(10, 0));
    }

    @Test
    void createSlot_Success() throws Exception {
        doNothing().when(doctorSlotService).createDoctorSlot(any(DoctorSlotRequest.class));

        mockMvc.perform(post("/api/v1/doctor-slots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Doctor slot created successfully"));

        verify(doctorSlotService, times(1)).createDoctorSlot(any(DoctorSlotRequest.class));
    }

    @Test
    void createSlot_ValidationError_WhenDoctorNameIsBlank() throws Exception {
        request.setDoctorName("");

        mockMvc.perform(post("/api/v1/doctor-slots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createSlot_ValidationError_WhenDoctorNameIsNull() throws Exception {
        request.setDoctorName(null);

        mockMvc.perform(post("/api/v1/doctor-slots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createSlot_ValidationError_WhenSlotDateIsNull() throws Exception {
        request.setSlotDate(null);

        mockMvc.perform(post("/api/v1/doctor-slots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createSlot_ValidationError_WhenSlotDateIsInPast() throws Exception {
        request.setSlotDate(LocalDate.now().minusDays(1));

        mockMvc.perform(post("/api/v1/doctor-slots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createSlot_ValidationError_WhenSlotTimeIsNull() throws Exception {
        request.setSlotTime(null);

        mockMvc.perform(post("/api/v1/doctor-slots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
