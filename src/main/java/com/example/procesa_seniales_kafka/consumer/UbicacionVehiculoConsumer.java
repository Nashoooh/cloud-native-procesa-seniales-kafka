package com.example.procesa_seniales_kafka.consumer;

import com.example.procesa_seniales_kafka.model.UbicacionVehiculo;
import com.example.procesa_seniales_kafka.service.ProcesamientoSenialesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Consumer de Kafka que escucha el tópico de ubicaciones de vehículos.
 * Procesa cada mensaje y delega al servicio de procesamiento de señales.
 */
@Slf4j
@Component
public class UbicacionVehiculoConsumer {

    @Autowired
    private ProcesamientoSenialesService procesamientoService;

    @KafkaListener(
            topics = "ubicaciones_vehiculos",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumirUbicacion(
            @Payload UbicacionVehiculo ubicacion,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment
    ) {
        try {
            log.info("📨 MENSAJE RECIBIDO → Tópico: ubicaciones_vehiculos | Key: {} | Partition: {} | Offset: {}",
                    key, partition, offset);
            
            log.info("🚗 UBICACIÓN → Vehículo: {} | Placa: {} | Lat: {:.4f} | Lon: {:.4f} | Velocidad: {:.2f} km/h | Estado: {} | Pasajeros: {}",
                    ubicacion.getVehiculoId(),
                    ubicacion.getPlacaVehiculo(),
                    ubicacion.getLatitud(),
                    ubicacion.getLongitud(),
                    ubicacion.getVelocidad(),
                    ubicacion.getEstado(),
                    ubicacion.getPasajeros());

            // Log para cuando no hay BD disponible
            log.info("📝 [LOG BD PENDIENTE] UBICACION - vehiculoId: {}, placa: {}, lat: {}, lon: {}, velocidad: {}, estado: {}, ciudad: {}, pasajeros: {}, timestamp: {}",
                    ubicacion.getVehiculoId(),
                    ubicacion.getPlacaVehiculo(),
                    ubicacion.getLatitud(),
                    ubicacion.getLongitud(),
                    ubicacion.getVelocidad(),
                    ubicacion.getEstado(),
                    ubicacion.getCiudad(),
                    ubicacion.getPasajeros(),
                    ubicacion.getTimestamp());

            // Procesar la ubicación para generar horarios si es necesario
            procesamientoService.procesarUbicacion(ubicacion);

            // Confirmar que el mensaje fue procesado exitosamente
            acknowledgment.acknowledge();
            
            log.debug("✅ Mensaje procesado y confirmado correctamente");

        } catch (Exception e) {
            log.error("❌ Error al procesar ubicación: {}", e.getMessage(), e);
            // No hacemos acknowledge para que el mensaje se reintente
        }
    }
}
