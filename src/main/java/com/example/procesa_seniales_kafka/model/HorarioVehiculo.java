package com.example.procesa_seniales_kafka.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Modelo de datos para los horarios de vehículos.
 * Representa la información de llegada de un vehículo a una parada específica,
 * incluyendo horarios estimados, reales y cálculo de retrasos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HorarioVehiculo {
    
    private String vehiculoId;
    private String placaVehiculo;
    private String paradaId;
    private String nombreParada;
    private String direccionParada;
    
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime horarioEstimado;
    
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime horarioReal;
    
    private Integer retrasoMinutos;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    private String ruta;
    private Integer secuenciaParada;
    private String estado; // ESTIMADO, LLEGANDO, LLEGADO, PASADO

    @Override
    public String toString() {
        return String.format("HorarioVehiculo[vehiculoId=%s, placa=%s, parada=%s (%s), horarioEstimado=%s, horarioReal=%s, retraso=%d min, estado=%s, timestamp=%s]",
                vehiculoId, placaVehiculo, paradaId, nombreParada, horarioEstimado, horarioReal, retrasoMinutos, estado, timestamp);
    }
}
