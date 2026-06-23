package com.healthcare.appointment.mapper;

import com.healthcare.appointment.entity.Appointment;
import com.healthcare.model.appointment.AppointmentDTO;
import com.healthcare.model.appointment.AppointmentEvent;
import com.healthcare.model.appointment.AppointmentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "patientName", ignore = true)
    @Mapping(target = "patientEmail", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Appointment toEntity(AppointmentDTO dto);


    @Mapping(target = "appointmentId", source = "id")
    AppointmentResponse toResponse(Appointment appointment);

    @Mapping(target = "appointmentId", source = "appointment.id")
    @Mapping(target = "eventType", source = "eventType")
    AppointmentEvent toEvent(Appointment appointment, String eventType);
}
