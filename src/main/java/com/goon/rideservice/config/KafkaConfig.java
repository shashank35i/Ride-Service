package com.goon.rideservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.internals.Topic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    private final String rideEvents;

    public KafkaConfig(@Value("${app.kafka.topics.ride-events}") String rideEvents){
        this.rideEvents = rideEvents;
    }

    @Bean
    public NewTopic rideEvents(){
        return TopicBuilder
                .name(rideEvents)
                .build();
    }
}
