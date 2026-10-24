package com.goon.rideservice.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.Map;

@Configuration
public class RideMatchingKafkaConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String>
    rideMatchingKafkaListenerContainerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String servers,
            KafkaTemplate<String, String> kafkaTemplate
    ) {
        var recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, ex) ->
                        new TopicPartition("ride-requested-dlt", record.partition())
        );

        recoverer.setFailIfSendResultIsError(true);

        var handler = new DefaultErrorHandler(
                recoverer,
                new FixedBackOff(2000L, 5L)
        );

        handler.setCommitRecovered(true);

        var factory =
                new ConcurrentKafkaListenerContainerFactory<String, String>();

        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(
                Map.of(
                        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, servers,
                        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                        StringDeserializer.class,
                        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                        StringDeserializer.class,
                        ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false
                )
        ));

        factory.getContainerProperties().setAckMode(
                ContainerProperties.AckMode.MANUAL_IMMEDIATE
        );

        factory.setCommonErrorHandler(handler);

        return factory;
    }
}