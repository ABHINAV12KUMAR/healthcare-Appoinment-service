package com.healthcare.appointment.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    @Value("${app.kafka.topics.appointment}")
    private String appointmentTopic;

    @Bean
    public NewTopic appointmentTopic() {
        return TopicBuilder.name(appointmentTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
