package com.healthcare.appointment.kafka;

import com.healthcare.appointment.dto.DoctorEvent;
import com.healthcare.appointment.entity.Doctor;
import com.healthcare.appointment.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DoctorEventConsumer {

    private final DoctorRepository doctorRepository;

    @KafkaListener(
            topics = "${app.kafka.topics.doctor}",
            groupId = "appointment-service-group"
    )
    public void consumeDoctorEvent(DoctorEvent event) {
        log.info("Received doctor event. eventType={}, doctorName={}, doctorId={}",
                event.getEventType(),
                event.getDoctorName(),
                event.getDoctorId());

        switch (event.getEventType()) {
            case "DOCTOR_CREATED":
            case "DOCTOR_UPDATED":
                handleDoctorCreatedOrUpdated(event);
                break;
            case "DOCTOR_DELETED":
                handleDoctorDeleted(event);
                break;
            default:
                log.warn("Unknown doctor event type: {}", event.getEventType());
        }
    }

    private void handleDoctorCreatedOrUpdated(DoctorEvent event) {
        Doctor doctor = doctorRepository.findByDoctorName(event.getDoctorName())
                .orElse(Doctor.builder()
                        .doctorName(event.getDoctorName())
                        .build());

        doctor.setSpecialization(event.getSpecialization());
        doctor.setEmail(event.getEmail());
        doctor.setPhone(event.getPhone());
        doctor.setAvailable(event.getAvailable());

        Doctor savedDoctor = doctorRepository.save(doctor);
        log.info("Doctor saved/updated successfully. doctorId={}, doctorName={}",
                savedDoctor.getId(),
                savedDoctor.getDoctorName());
    }

    private void handleDoctorDeleted(DoctorEvent event) {
        doctorRepository.findByDoctorName(event.getDoctorName())
                .ifPresent(doctor -> {
                    doctorRepository.delete(doctor);
                    log.info("Doctor deleted successfully. doctorName={}", event.getDoctorName());
                });
    }
}
