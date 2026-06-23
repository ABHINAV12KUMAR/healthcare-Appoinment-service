package com.healthcare.appointment.service.imp;

import com.healthcare.appointment.dto.DoctorSlotRequest;
import com.healthcare.appointment.entity.DoctorSlot;
import com.healthcare.appointment.exception.AppointmentBookingException;
import com.healthcare.appointment.repository.DoctorSlotRepository;
import com.healthcare.model.appointment.SlotStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorSlotServiceImplTest {

    @Mock
    private DoctorSlotRepository doctorSlotRepository;

    @InjectMocks
    private DoctorSlotServiceImpl doctorSlotService;

    private DoctorSlotRequest request;

    @BeforeEach
    void setUp() {
        request = new DoctorSlotRequest();
        request.setDoctorName("Dr. John Doe");
        request.setSlotDate(LocalDate.now().plusDays(1));
        request.setSlotTime(LocalTime.of(10, 0));
    }

    @Test
    void createDoctorSlot_Success() {
        when(doctorSlotRepository.save(any(DoctorSlot.class))).thenReturn(new DoctorSlot());

        doctorSlotService.createDoctorSlot(request);

        verify(doctorSlotRepository, times(1)).save(any(DoctorSlot.class));
    }

    @Test
    void createDoctorSlot_ThrowsException_WhenRepositoryFails() {
        when(doctorSlotRepository.save(any(DoctorSlot.class)))
                .thenThrow(new RuntimeException("Database error"));

        AppointmentBookingException exception = assertThrows(
                AppointmentBookingException.class,
                () -> doctorSlotService.createDoctorSlot(request)
        );

        assertEquals("Doctor slot already exists or could not be created", exception.getMessage());
        verify(doctorSlotRepository, times(1)).save(any(DoctorSlot.class));
    }

    @Test
    void createDoctorSlot_BuildsCorrectEntity() {
        when(doctorSlotRepository.save(any(DoctorSlot.class))).thenAnswer(invocation -> {
            DoctorSlot slot = invocation.getArgument(0);
            assertEquals("Dr. John Doe", slot.getDoctorName());
            assertEquals(LocalDate.now().plusDays(1), slot.getSlotDate());
            assertEquals(LocalTime.of(10, 0), slot.getSlotTime());
            assertEquals(SlotStatus.AVAILABLE, slot.getStatus());
            return slot;
        });

        doctorSlotService.createDoctorSlot(request);

        verify(doctorSlotRepository).save(any(DoctorSlot.class));
    }
}
