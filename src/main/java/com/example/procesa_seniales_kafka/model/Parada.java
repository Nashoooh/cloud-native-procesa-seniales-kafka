package com.example.procesa_seniales_kafka.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Modelo de datos para las paradas predefinidas del sistema.
 * Contiene la información geográfica y descriptiva de cada parada.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Parada {
    
    private String paradaId;
    private String nombre;
    private String direccion;
    private Double latitud;
    private Double longitud;

    @Override
    public String toString() {
        return String.format("Parada[id=%s, nombre=%s, lat=%.4f, lon=%.4f]",
                paradaId, nombre, latitud, longitud);
    }
}
