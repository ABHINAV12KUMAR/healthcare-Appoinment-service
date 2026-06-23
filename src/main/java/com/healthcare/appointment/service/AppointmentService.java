package com.healthcare.appointment.service;

import com.healthcare.model.appointment.AppointmentDTO;
import com.healthcare.model.appointment.AppointmentResponse;

public interface AppointmentService {
    AppointmentResponse bookAppointment(AppointmentDTO appointmentDto);
}
