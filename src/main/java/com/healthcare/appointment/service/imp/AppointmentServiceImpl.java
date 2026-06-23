package com.healthcare.appointment.service.imp;

import com.healthcare.appointment.client.PatientQueryClient;
import com.healthcare.appointment.dto.PatientInfoResponse;
import com.healthcare.appointment.entity.Appointment;
import com.healthcare.appointment.entity.DoctorSlot;
import com.healthcare.appointment.exception.AppointmentBookingException;
import com.healthcare.appointment.kafka.AppointmentEventProducer;
import com.healthcare.appointment.mapper.AppointmentMapper;
import com.healthcare.appointment.repository.AppointmentRepository;
import com.healthcare.appointment.repository.DoctorSlotRepository;
import com.healthcare.appointment.service.AppointmentService;
import com.healthcare.model.appointment.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentServiceImpl implements AppointmentService {

    private static final String EVENT_TYPE_BOOKED = "APPOINTMENT_BOOKED";

    private final AppointmentRepository appointmentRepository;
    private final DoctorSlotRepository doctorSlotRepository;
    private final PatientQueryClient patientQueryClient;
    private final AppointmentMapper appointmentMapper;
    private final AppointmentEventProducer appointmentEventProducer;

    @Override
    @Transactional
    public AppointmentResponse bookAppointment(AppointmentDTO appointmentDto) {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        MDC.put("patientId", String.valueOf(appointmentDto.getPatientId()));
        MDC.put("doctorName", appointmentDto.getDoctorName());

        try {
            log.info("Appointment booking started. patientId={},doctorName={}date={}, time={}",
                    appointmentDto.getPatientId(),
                    appointmentDto.getDoctorName(),
                    appointmentDto.getAppointmentDate(),
                    appointmentDto.getAppointmentTime()
            );
            validateAppointmentRequest(appointmentDto);
            PatientInfoResponse patientInfoResponse = patientQueryClient.getPatientById(appointmentDto.getPatientId());
            DoctorSlot doctorSlot = fetchAndLockDoctorSlot(appointmentDto);
            validateSlotAvailability(doctorSlot);
            validateNoDuplicateBooking(appointmentDto);
            doctorSlot.setStatus(SlotStatus.BOOKED);
            doctorSlotRepository.save(doctorSlot);
            Appointment appointment = buildAppointment(appointmentDto,patientInfoResponse);
            Appointment savedAppointment = appointmentRepository.save(appointment);
            log.info("Appointment saved successfully. appointmentId={}, patientId={}, doctorName={}, slotId={}",
                    savedAppointment.getId(),
                    savedAppointment.getPatientId(),
                    savedAppointment.getDoctorName(),
                    doctorSlot.getId());
            publishAppointmentEventAfterCommit(savedAppointment);
            return appointmentMapper.toResponse(savedAppointment);
        } finally {
            MDC.clear();
        }
    }

    private void validateAppointmentRequest(AppointmentDTO appointmentDto) {
        if (appointmentDto == null) {
            throw new AppointmentBookingException("Appointment request is required");
        }

        if (appointmentDto.getPatientId() == null) {
            throw new AppointmentBookingException("Patient id is required");
        }

        if (appointmentDto.getDoctorName() == null || appointmentDto.getDoctorName().isBlank()) {
            throw new AppointmentBookingException("Doctor name is required");
        }
        if (appointmentDto.getAppointmentDate() == null) {
            throw new AppointmentBookingException("Appointment date is required");
        }

        if (appointmentDto.getAppointmentTime() == null) {
            throw new AppointmentBookingException("Appointment time is required");
        }

        if (appointmentDto.getAppointmentDate().isBefore(LocalDate.now())) {
            log.warn("Appointment date validation failed. patientId={}, appointmentDate={}",
                    appointmentDto.getPatientId(),
                    appointmentDto.getAppointmentDate());

            throw new AppointmentBookingException("Appointment date cannot be in the past");
        }

    }

    private DoctorSlot fetchAndLockDoctorSlot(AppointmentDTO appointmentDto ) {
        DoctorSlot doctorSlot = doctorSlotRepository.findSlotForUpdate(
                appointmentDto.getDoctorName(),
                appointmentDto.getAppointmentDate(),
                appointmentDto.getAppointmentTime()
        ).orElseThrow(() -> {
            log.warn("Doctor slot not found. doctorName={}, date={}, time={}",
                    appointmentDto.getDoctorName(),
                    appointmentDto.getAppointmentDate(),
                    appointmentDto.getAppointmentTime());

            return new AppointmentBookingException("Doctor slot not found");
        });
        log.debug("Doctor slot locked successfully. slotId={}, status={}",
                doctorSlot.getId(),
                doctorSlot.getStatus());
        return doctorSlot;
    }

    private void validateSlotAvailability(DoctorSlot doctorSlot) {

        if (doctorSlot.getStatus() != SlotStatus.AVAILABLE) {
            log.warn("Doctor slot is not available. slotId={}, status={}",
                    doctorSlot.getId(),
                    doctorSlot.getStatus());

            throw new AppointmentBookingException("Doctor slot is already booked");
        }
    }

    private void validateNoDuplicateBooking(AppointmentDTO appointmentDTO) {

        boolean alreadyBooked = appointmentRepository
                .existsByPatientIdAndAppointmentDateAndAppointmentTime(
                        appointmentDTO.getPatientId(),
                        appointmentDTO.getAppointmentDate(),
                        appointmentDTO.getAppointmentTime()
                );

        if (alreadyBooked) {
            log.warn("Duplicate patient appointment attempt. patientId={}, date={}, time={}",
                    appointmentDTO.getPatientId(),
                    appointmentDTO.getAppointmentDate(),
                    appointmentDTO.getAppointmentTime());

            throw new AppointmentBookingException("Patient already has an appointment at this time");
        }
    }

    private Appointment buildAppointment(AppointmentDTO appointmentDto, PatientInfoResponse patientInfoResponse) {

        Appointment appointment = appointmentMapper.toEntity(appointmentDto);
        appointment.setPatientName(patientInfoResponse.getName());
        appointment.setPatientEmail(patientInfoResponse.getEmail());
        appointment.setStatus(AppointmentStatus.SCHEDULED);

        return appointment;
    }

    private void publishAppointmentEventAfterCommit(Appointment savedAppointment) {

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {

                log.info("Publishing appointment event after DB commit. appointmentId={}",
                        savedAppointment.getId());

                AppointmentEvent event = appointmentMapper.toEvent(savedAppointment, EVENT_TYPE_BOOKED);

                appointmentEventProducer.publishEvent(event);

                log.info("Appointment event publish triggered. appointmentId={}",
                        savedAppointment.getId());
            }
        });
    }
}
