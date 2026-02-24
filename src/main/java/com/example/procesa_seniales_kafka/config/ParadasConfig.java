package com.example.procesa_seniales_kafka.config;

import com.example.procesa_seniales_kafka.model.Parada;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración de las paradas predefinidas del sistema.
 * Estas paradas se utilizan para calcular la proximidad de los vehículos
 * y generar actualizaciones de horarios.
 */
@Configuration
public class ParadasConfig {

    /**
     * Define las 6 paradas principales del sistema de transporte.
     * Coordenadas basadas en ubicaciones reales en Lima, Perú.
     */
    @Bean
    public List<Parada> paradasPredefinidas() {
        return Arrays.asList(
                Parada.builder()
                        .paradaId("P001")
                        .nombre("Terminal Norte")
                        .direccion("Av. Principal 123")
                        .latitud(-12.0464)
                        .longitud(-77.0428)
                        .build(),
                
                Parada.builder()
                        .paradaId("P002")
                        .nombre("Plaza Mayor")
                        .direccion("Jr. Unión 456")
                        .latitud(-12.0565)
                        .longitud(-77.0352)
                        .build(),
                
                Parada.builder()
                        .paradaId("P003")
                        .nombre("Parque Central")
                        .direccion("Av. Arequipa 789")
                        .latitud(-12.0689)
                        .longitud(-77.0389)
                        .build(),
                
                Parada.builder()
                        .paradaId("P004")
                        .nombre("Centro Comercial")
                        .direccion("Av. Javier Prado 321")
                        .latitud(-12.0834)
                        .longitud(-77.0321)
                        .build(),
                
                Parada.builder()
                        .paradaId("P005")
                        .nombre("Universidad")
                        .direccion("Av. Universitaria 654")
                        .latitud(-12.0721)
                        .longitud(-77.0784)
                        .build(),
                
                Parada.builder()
                        .paradaId("P006")
                        .nombre("Hospital Regional")
                        .direccion("Av. Salud 987")
                        .latitud(-12.0612)
                        .longitud(-77.0456)
                        .build()
        );
    }
}
