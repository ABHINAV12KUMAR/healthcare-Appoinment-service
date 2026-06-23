package com.healthcare.appointment.repository;

import com.healthcare.appointment.entity.Appointment;
import com.healthcare.model.appointment.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {


    List<Appointment> findByPatientId(Long patientId);
    List<Appointment> findByDoctorName(String doctorName);
    List<Appointment> findByStatus(AppointmentStatus status);
    boolean existsByPatientIdAndAppointmentDateAndAppointmentTime(
            Long patientId,
            LocalDate appointmentDate,
            LocalTime appointmentTime
    );

}
