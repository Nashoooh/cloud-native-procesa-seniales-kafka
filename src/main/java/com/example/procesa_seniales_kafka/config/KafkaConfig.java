package com.example.procesa_seniales_kafka.config;

import com.example.procesa_seniales_kafka.model.HorarioVehiculo;
import com.example.procesa_seniales_kafka.model.UbicacionVehiculo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.JacksonUtils;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuración de Kafka para el microservicio procesador de señales.
 * Define los consumers para recibir ubicaciones y producers para publicar horarios.
 */
@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Bean
    public ObjectMapper objectMapper() {
        return JacksonUtils.enhancedObjectMapper()
                .registerModule(new JavaTimeModule());
    }

    // ========================================================================
    // CONSUMER CONFIGURATION
    // ========================================================================

    @Bean
    public ConsumerFactory<String, UbicacionVehiculo> consumerFactory(ObjectMapper objectMapper) {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // Manual commit
        
        JsonDeserializer<UbicacionVehiculo> jsonDeserializer = new JsonDeserializer<>(UbicacionVehiculo.class, objectMapper);
        jsonDeserializer.addTrustedPackages("*");
        
        ErrorHandlingDeserializer<UbicacionVehiculo> errorHandlingDeserializer = 
                new ErrorHandlingDeserializer<>(jsonDeserializer);
        
        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                errorHandlingDeserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UbicacionVehiculo> kafkaListenerContainerFactory(
            ConsumerFactory<String, UbicacionVehiculo> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, UbicacionVehiculo> factory = 
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }

    // ========================================================================
    // PRODUCER CONFIGURATION
    // ========================================================================

    @Bean
    public ProducerFactory<String, HorarioVehiculo> producerFactory(ObjectMapper objectMapper) {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        
        JsonSerializer<HorarioVehiculo> jsonSerializer = new JsonSerializer<>(objectMapper);
        
        return new DefaultKafkaProducerFactory<>(
                config,
                new StringSerializer(),
                jsonSerializer
        );
    }

    @Bean
    public KafkaTemplate<String, HorarioVehiculo> kafkaTemplate(
            ProducerFactory<String, HorarioVehiculo> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
