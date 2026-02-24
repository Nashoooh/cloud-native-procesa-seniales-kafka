package com.example.procesa_seniales_kafka.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Modelo de datos para las ubicaciones de vehículos recibidas desde Kafka.
 * Representa el estado en tiempo real de un vehículo incluyendo su posición GPS,
 * velocidad, estado operativo y información de pasajeros.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UbicacionVehiculo {
    
    private String vehiculoId;
    private String placaVehiculo;
    private Double latitud;
    private Double longitud;
    private Double velocidad;
    private String direccion;
    private String ciudad;
    private String estado; // EN_RUTA, DETENIDO, EN_PARADA
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    private String conductor;
    private Integer pasajeros;
    private String ruta;

    @Override
    public String toString() {
        return String.format("UbicacionVehiculo[vehiculoId=%s, placa=%s, lat=%.4f, lon=%.4f, velocidad=%.2f km/h, estado=%s, pasajeros=%d, ruta=%s, timestamp=%s]",
                vehiculoId, placaVehiculo, latitud, longitud, velocidad, estado, pasajeros, ruta, timestamp);
    }
}
