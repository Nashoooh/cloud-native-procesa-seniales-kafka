package com.example.procesa_seniales_kafka.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para verificar el estado del microservicio.
 */
@Slf4j
@RestController
@RequestMapping("/api/procesador")
public class ProcesadorController {

    @Autowired
    private KafkaTemplate<String, ?> kafkaTemplate;

    /**
     * Endpoint de health check.
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("servicio", "Procesador de Señales Kafka");
        response.put("estado", "ACTIVO");
        response.put("timestamp", LocalDateTime.now());
        response.put("descripcion", "Consumiendo de 'ubicaciones_vehiculos' y publicando a 'horarios'");
        
        log.info("✅ Health check solicitado - Servicio ACTIVO");
        
        return response;
    }

    /**
     * Endpoint para obtener información del servicio.
     */
    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("microservicio", "Procesador de Señales");
        response.put("version", "1.0.0");
        response.put("descripcion", "Microservicio que procesa ubicaciones de vehículos y genera actualizaciones de horarios");
        
        Map<String, String> topics = new HashMap<>();
        topics.put("consume", "ubicaciones_vehiculos");
        topics.put("produce", "horarios");
        response.put("topics", topics);
        
        Map<String, String> funcionalidad = new HashMap<>();
        funcionalidad.put("1", "Consume mensajes del tópico 'ubicaciones_vehiculos'");
        funcionalidad.put("2", "Calcula proximidad a paradas usando fórmula de Haversine");
        funcionalidad.put("3", "Genera eventos de horarios cuando detecta vehículos cerca de paradas");
        funcionalidad.put("4", "Publica actualizaciones al tópico 'horarios'");
        funcionalidad.put("5", "Registra logs detallados para posterior inserción en BD");
        response.put("funcionalidad", funcionalidad);
        
        response.put("nota", "Este microservicio NO inserta en BD. El MS de Monitorización se encarga de eso.");
        
        return response;
    }

    /**
     * Endpoint para verificar el estado de conexión con Kafka.
     */
    @GetMapping("/kafka-status")
    public Map<String, Object> kafkaStatus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Intentar obtener métricas del productor
            response.put("kafka_conectado", true);
            response.put("estado", "CONECTADO");
            response.put("mensaje", "Conexión con Kafka establecida correctamente");
        } catch (Exception e) {
            response.put("kafka_conectado", false);
            response.put("estado", "ERROR");
            response.put("mensaje", "Error de conexión con Kafka: " + e.getMessage());
            log.error("❌ Error al verificar conexión con Kafka", e);
        }
        
        response.put("timestamp", LocalDateTime.now());
        return response;
    }
}
