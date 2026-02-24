package com.example.procesa_seniales_kafka.service;

import com.example.procesa_seniales_kafka.model.HorarioVehiculo;
import com.example.procesa_seniales_kafka.model.Parada;
import com.example.procesa_seniales_kafka.model.UbicacionVehiculo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio principal de procesamiento de señales de vehículos.
 * Calcula la proximidad a paradas y genera actualizaciones de horarios.
 */
@Slf4j
@Service
public class ProcesamientoSenialesService {

    @Autowired
    private KafkaTemplate<String, HorarioVehiculo> kafkaTemplate;

    @Autowired
    private List<Parada> paradasPredefinidas;

    @Value("${procesador.distancia.umbral.km:0.5}")
    private double distanciaUmbralKm;

    @Value("${procesador.tiempo.llegando.minutos:5}")
    private int tiempoLlegandoMinutos;

    // Mapa para rastrear el estado de cada vehículo por parada
    // Key: vehiculoId-paradaId, Value: último horario registrado
    private Map<String, HorarioVehiculo> estadoVehiculos = new HashMap<>();

    /**
     * Procesa una ubicación de vehículo para determinar si genera un evento de horario.
     */
    public void procesarUbicacion(UbicacionVehiculo ubicacion) {
        log.debug("📍 Procesando ubicación: {}", ubicacion);

        // Buscar parada más cercana
        Optional<ParadaCercana> paradaCercanaOpt = encontrarParadaMasCercana(ubicacion);

        if (paradaCercanaOpt.isPresent()) {
            ParadaCercana paradaCercana = paradaCercanaOpt.get();
            Parada parada = paradaCercana.getParada();
            double distanciaKm = paradaCercana.getDistanciaKm();

            log.info("🎯 Vehículo {} cerca de {} - Distancia: {:.3f} km", 
                    ubicacion.getVehiculoId(), parada.getNombre(), distanciaKm);

            // Generar horario basado en la distancia
            HorarioVehiculo horario = generarHorario(ubicacion, parada, distanciaKm);

            // Verificar si debemos publicar este horario (evitar duplicados)
            if (debePublicarHorario(horario)) {
                publicarHorario(horario);
            }
        } else {
            log.debug("ℹ️  Vehículo {} no está cerca de ninguna parada", ubicacion.getVehiculoId());
        }
    }

    /**
     * Encuentra la parada más cercana al vehículo usando la fórmula de Haversine.
     */
    private Optional<ParadaCercana> encontrarParadaMasCercana(UbicacionVehiculo ubicacion) {
        ParadaCercana paradaMasCercana = null;
        double distanciaMinima = Double.MAX_VALUE;

        for (Parada parada : paradasPredefinidas) {
            double distancia = calcularDistanciaHaversine(
                    ubicacion.getLatitud(),
                    ubicacion.getLongitud(),
                    parada.getLatitud(),
                    parada.getLongitud()
            );

            if (distancia < distanciaMinima && distancia <= distanciaUmbralKm) {
                distanciaMinima = distancia;
                paradaMasCercana = new ParadaCercana(parada, distancia);
            }
        }

        return Optional.ofNullable(paradaMasCercana);
    }

    /**
     * Calcula la distancia entre dos puntos GPS usando la fórmula de Haversine.
     * @return Distancia en kilómetros
     */
    private double calcularDistanciaHaversine(double lat1, double lon1, double lat2, double lon2) {
        final int RADIO_TIERRA_KM = 6371;

        double latDistancia = Math.toRadians(lat2 - lat1);
        double lonDistancia = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistancia / 2) * Math.sin(latDistancia / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistancia / 2) * Math.sin(lonDistancia / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return RADIO_TIERRA_KM * c;
    }

    /**
     * Genera un objeto HorarioVehiculo basado en la ubicación y parada.
     */
    private HorarioVehiculo generarHorario(UbicacionVehiculo ubicacion, Parada parada, double distanciaKm) {
        LocalDateTime ahora = LocalDateTime.now();
        LocalTime horaActual = ahora.toLocalTime();

        // Determinar el estado basado en la distancia y velocidad
        String estado = determinarEstado(distanciaKm, ubicacion.getVelocidad());

        // Calcular horario estimado (simulado - en producción vendría de una tabla de horarios)
        LocalTime horarioEstimado = calcularHorarioEstimado(ubicacion, parada);

        // El horario real es la hora actual
        LocalTime horarioReal = horaActual;

        // Calcular retraso en minutos
        int retrasoMinutos = calcularRetraso(horarioEstimado, horarioReal);

        // Obtener secuencia de parada (simulado)
        int secuenciaParada = obtenerSecuenciaParada(parada.getParadaId());

        return HorarioVehiculo.builder()
                .vehiculoId(ubicacion.getVehiculoId())
                .placaVehiculo(ubicacion.getPlacaVehiculo())
                .paradaId(parada.getParadaId())
                .nombreParada(parada.getNombre())
                .direccionParada(parada.getDireccion())
                .horarioEstimado(horarioEstimado)
                .horarioReal(horarioReal)
                .retrasoMinutos(retrasoMinutos)
                .timestamp(ahora)
                .ruta(ubicacion.getRuta())
                .secuenciaParada(secuenciaParada)
                .estado(estado)
                .build();
    }

    /**
     * Determina el estado del vehículo respecto a la parada.
     */
    private String determinarEstado(double distanciaKm, double velocidad) {
        if (distanciaKm < 0.05) { // Menos de 50 metros
            return "LLEGADO";
        } else if (distanciaKm < 0.2 && velocidad < 10) { // Menos de 200m y velocidad baja
            return "LLEGANDO";
        } else if (distanciaKm <= distanciaUmbralKm) {
            return "ESTIMADO";
        } else {
            return "PASADO";
        }
    }

    /**
     * Calcula el horario estimado de llegada (simulado).
     * En producción, esto vendría de una tabla de horarios predefinidos.
     */
    private LocalTime calcularHorarioEstimado(UbicacionVehiculo ubicacion, Parada parada) {
        // Simulación: basado en la hora actual más/menos algunos minutos según la secuencia
        LocalTime ahora = LocalTime.now();
        int secuencia = obtenerSecuenciaParada(parada.getParadaId());
        
        // Ajustar según la secuencia (simulación simple)
        return ahora.plusMinutes(secuencia * 5L);
    }

    /**
     * Calcula el retraso en minutos entre horario estimado y real.
     */
    private int calcularRetraso(LocalTime estimado, LocalTime real) {
        return (int) ChronoUnit.MINUTES.between(estimado, real);
    }

    /**
     * Obtiene la secuencia de la parada en la ruta (simulado).
     */
    private int obtenerSecuenciaParada(String paradaId) {
        // Extrae el número del ID (P001 -> 1, P002 -> 2, etc.)
        return Integer.parseInt(paradaId.substring(1));
    }

    /**
     * Verifica si se debe publicar el horario para evitar duplicados.
     */
    private boolean debePublicarHorario(HorarioVehiculo horario) {
        String clave = horario.getVehiculoId() + "-" + horario.getParadaId();
        HorarioVehiculo ultimoHorario = estadoVehiculos.get(clave);

        // Si no hay registro previo, publicar
        if (ultimoHorario == null) {
            estadoVehiculos.put(clave, horario);
            return true;
        }

        // Si el estado cambió, publicar
        if (!ultimoHorario.getEstado().equals(horario.getEstado())) {
            estadoVehiculos.put(clave, horario);
            return true;
        }

        // Si pasó más de 1 minuto desde la última actualización, publicar
        long minutosDiferencia = ChronoUnit.MINUTES.between(
                ultimoHorario.getTimestamp(),
                horario.getTimestamp()
        );

        if (minutosDiferencia >= 1) {
            estadoVehiculos.put(clave, horario);
            return true;
        }

        return false;
    }

    /**
     * Publica el horario al tópico de Kafka.
     */
    private void publicarHorario(HorarioVehiculo horario) {
        try {
            kafkaTemplate.send("horarios", horario.getVehiculoId(), horario);
            
            log.info("✅ HORARIO PUBLICADO → Tópico: horarios | Vehículo: {} | Parada: {} | Estado: {} | Retraso: {} min",
                    horario.getVehiculoId(),
                    horario.getNombreParada(),
                    horario.getEstado(),
                    horario.getRetrasoMinutos());
            
            // Log adicional en archivo para cuando no haya BD
            log.info("📝 [LOG BD PENDIENTE] HORARIO - vehiculoId: {}, paradaId: {}, estado: {}, horarioEstimado: {}, horarioReal: {}, retraso: {} min",
                    horario.getVehiculoId(),
                    horario.getParadaId(),
                    horario.getEstado(),
                    horario.getHorarioEstimado(),
                    horario.getHorarioReal(),
                    horario.getRetrasoMinutos());
            
        } catch (Exception e) {
            log.error("❌ Error al publicar horario: {}", e.getMessage(), e);
        }
    }

    /**
     * Clase interna para almacenar información de parada cercana.
     */
    private static class ParadaCercana {
        private final Parada parada;
        private final double distanciaKm;

        public ParadaCercana(Parada parada, double distanciaKm) {
            this.parada = parada;
            this.distanciaKm = distanciaKm;
        }

        public Parada getParada() {
            return parada;
        }

        public double getDistanciaKm() {
            return distanciaKm;
        }
    }
}
