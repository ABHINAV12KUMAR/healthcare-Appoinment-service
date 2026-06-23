package com.healthcare.appointment.service.imp;

import com.healthcare.appointment.dto.DoctorSlotRequest;
import com.healthcare.appointment.entity.DoctorSlot;
import com.healthcare.appointment.exception.AppointmentBookingException;
import com.healthcare.appointment.repository.DoctorSlotRepository;
import com.healthcare.appointment.service.DoctorSlotService;
import com.healthcare.model.appointment.SlotStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DoctorSlotServiceImpl implements DoctorSlotService {

    private final DoctorSlotRepository doctorSlotRepository;

    @Override
    public void createDoctorSlot(DoctorSlotRequest request) {
        DoctorSlot doctorSlot = DoctorSlot.builder()
                .doctorName(request.getDoctorName())
                .slotDate(request.getSlotDate())
                .slotTime(request.getSlotTime())
                .status(SlotStatus.AVAILABLE)
                .build();

        try {
            doctorSlotRepository.save(doctorSlot);
        } catch (Exception ex) {
            throw new AppointmentBookingException("Doctor slot already exists or could not be created");
        }
    }

}

