package com.example.procesa_seniales_kafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Aplicación principal del Microservicio Procesador de Señales.
 * 
 * Este microservicio NO requiere base de datos, solo procesa mensajes de Kafka:
 * - Consume del tópico: ubicaciones_vehiculos
 * - Procesa ubicaciones y calcula horarios
 * - Publica al tópico: horarios
 */
@SpringBootApplication
public class ProcesaSenialesKafkaApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProcesaSenialesKafkaApplication.class, args);
		System.out.println("\n🚀 Microservicio Procesador de Señales INICIADO");
		System.out.println("📡 Consumiendo de: ubicaciones_vehiculos");
		System.out.println("📤 Publicando a: horarios");
		System.out.println("🌐 API disponible en: http://localhost:8082/api/procesador/health\n");
	}

}
