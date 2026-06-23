package com.healthcare.appointment.kafka;

import com.healthcare.model.appointment.AppointmentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentEventProducer {

    private final KafkaTemplate<String, AppointmentEvent> kafkaTemplate;
    @Value("${app.kafka.topics.appointment:appointment-topic}")
    private String appointmentTopic;

    public void publishEvent(AppointmentEvent event) {
        String key = String.valueOf(event.getAppointmentId());
        log.info("Publishing appointment event. topic={}, key={},evenType={} ", appointmentTopic, key, event.getEventType());
        kafkaTemplate.send(appointmentTopic, key, event)
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        log.error("Failed to publish appointment event. appointmentId={}, error={}",
                                event.getAppointmentId(), exception.getMessage());
                    } else {
                        log.info("Appointment event published successfully. appointmentId={}, topic={}, partition={}, offset={}",
                                event.getAppointmentId(),
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }

}
