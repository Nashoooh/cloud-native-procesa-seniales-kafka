# 🚌 Microservicio Procesador de Señales Kafka

## 📋 Descripción

Microservicio Spring Boot que procesa ubicaciones de vehículos en tiempo real desde Kafka y genera actualizaciones de horarios basándose en la proximidad a paradas predefinidas.

## 🎯 Funcionalidad Principal

Este microservicio **NO inserta en base de datos**. Su responsabilidad es:

1. ✅ **Consumir** mensajes del tópico `ubicaciones_vehiculos`
2. ✅ **Procesar** ubicaciones usando algoritmo Haversine para calcular distancias
3. ✅ **Detectar** cuando un vehículo está cerca de una parada (umbral: 500m)
4. ✅ **Generar** eventos de horarios con estados: ESTIMADO, LLEGANDO, LLEGADO
5. ✅ **Publicar** actualizaciones al tópico `horarios`
6. ✅ **Registrar logs** detallados para auditoría

> **Nota:** El Microservicio de Monitorización (otro MS) es el encargado de consumir de ambos tópicos e insertar en Oracle Cloud.

## 🏗️ Arquitectura

```
┌─────────────────────────┐
│  Productor Kafka        │
│  (Simula vehículos)     │
└───────────┬─────────────┘
            │ Produce
            ▼
┌─────────────────────────┐
│  ubicaciones_vehiculos  │
│  (Tópico Kafka)         │
└───────────┬─────────────┘
            │ Consume
            ▼
┌─────────────────────────┐
│  ESTE MICROSERVICIO     │
│  Procesador Señales     │
│  - Calcula distancias   │
│  - Genera horarios      │
└───────────┬─────────────┘
            │ Produce
            ▼
┌─────────────────────────┐
│  horarios               │
│  (Tópico Kafka)         │
└─────────────────────────┘
            │
            ▼
    MS Monitorización
    (inserta en BD)
```

## 🚀 Inicio Rápido

### Prerrequisitos

- Java 21
- Maven 3.8+
- Kafka corriendo en: `localhost:29092,localhost:39092,localhost:49092`
- Tópico `ubicaciones_vehiculos` creado
- Tópico `horarios` creado

### Compilar

```bash
mvnw clean package
```

### Ejecutar

```bash
mvnw spring-boot:run
```

El servicio estará disponible en: `http://localhost:8082`

## 📡 Endpoints REST

### Health Check
```bash
GET http://localhost:2/api/procesador/health
```

Respuesta:
```json
{
  "servicio": "Procesador de Señales Kafka",
  "estado": "ACTIVO",
  "timestamp": "2026-02-24T10:30:15",
  "descripcion": "Consumiendo de 'ubicaciones_vehiculos' y publicando a 'horarios'"
}
```

### Información del Servicio
```bash
GET http://localhost:8082/api/procesador/info
```

### Estado de Kafka
```bash
GET http://localhost:8082/api/procesador/kafka-status
```

## 📊 Paradas Predefinidas

El sistema monitorea 6 paradas:

| ID   | Nombre             | Ubicación                    |
|------|-------------------|------------------------------|
| P001 | Terminal Norte    | -12.0464, -77.0428          |
| P002 | Plaza Mayor       | -12.0565, -77.0352          |
| P003 | Parque Central    | -12.0689, -77.0389          |
| P004 | Centro Comercial  | -12.0834, -77.0321          |
| P005 | Universidad       | -12.0721, -77.0784          |
| P006 | Hospital Regional | -12.0612, -77.0456          |

## ⚙️ Configuración

### application.properties

```properties
# Kafka Consumer
spring.kafka.bootstrap-servers=localhost:29092,localhost:39092,localhost:49092
spring.kafka.consumer.group-id=procesador-seniales-group

# Parámetros de Procesamiento
procesador.distancia.umbral.km=0.5      # Umbral para detectar proximidad
procesador.tiempo.llegando.minutos=5    # Ventana de tiempo para estado "LLEGANDO"
```

## 🧮 Algoritmo de Procesamiento

### 1. Cálculo de Distancia (Haversine)

Calcula la distancia entre la ubicación del vehículo y cada parada:

```java
d = 2 * R * arcsin(√(sin²(Δlat/2) + cos(lat1) * cos(lat2) * sin²(Δlon/2)))
```

### 2. Detección de Proximidad

- **< 50m**: Estado = `LLEGADO`
- **< 200m y velocidad < 10 km/h**: Estado = `LLEGANDO`
- **< 500m**: Estado = `ESTIMADO`
- **> 500m**: No genera evento

### 3. Cálculo de Retraso

```java
retraso = horarioReal - horarioEstimado (en minutos)
```

## 📝 Formato de Mensajes

### Input: ubicaciones_vehiculos

```json
{
  "vehiculoId": "VEH-001",
  "placaVehiculo": "ABC-001",
  "latitud": -12.0464,
  "longitud": -77.0428,
  "velocidad": 45.50,
  "direccion": "Av. Principal 456",
  "ciudad": "Lima",
  "estado": "EN_RUTA",
  "timestamp": "2026-02-24T10:30:15",
  "conductor": "Juan Pérez",
  "pasajeros": 25,
  "ruta": "Ruta A - Norte"
}
```

### Output: horarios

```json
{
  "vehiculoId": "VEH-001",
  "placaVehiculo": "ABC-001",
  "paradaId": "P001",
  "nombreParada": "Terminal Norte",
  "direccionParada": "Av. Principal 123",
  "horarioEstimado": "10:25:00",
  "horarioReal": "10:28:00",
  "retrasoMinutos": 3,
  "timestamp": "2026-02-24T10:28:00",
  "ruta": "Ruta A - Norte",
  "secuenciaParada": 1,
  "estado": "LLEGADO"
}
```

## 📋 Logs

Los logs se muestran en **consola únicamente** para seguimiento en tiempo real.

### Tipos de Logs

```
📨 MENSAJE RECIBIDO → Cada ubicación consumida
🚗 UBICACIÓN → Detalles del vehículo
🎯 Vehículo cerca → Cuando detecta proximidad
✅ HORARIO PUBLICADO → Cuando publica a Kafka
📝 [LOG BD PENDIENTE] → Información para futura inserción en BD
```

## 🗄️ Base de Datos

**IMPORTANTE**: Este microservicio **NO requiere conexión a base de datos**.

La configuración de Oracle Cloud está comentada en `application.properties` ya que:
- Este MS solo procesa y publica eventos
- El MS de Monitorización se encarga de la persistencia
- Los logs contienen toda la info necesaria para auditoría temporal

Cuando el MS de Monitorización esté listo con acceso a Oracle, podrá consumir los eventos del tópico `horarios` y persistirlos.

## 🧪 Testing

### Verificar Consumo (en Consola)

Observa los logs en la consola donde ejecutas el microservicio:
- Mensajes recibidos con emojis 📨 🚗
- Detección de proximidad 🎯
- Horarios publicados ✅

### Verificar Publicación a Kafka

```bash
# Consumir del tópico horarios
docker exec -it kafka-1 kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic horarios \
  --from-beginning
```

## 🔧 Desarrollo

### Estructura del Proyecto

```
src/main/java/com/example/procesa_seniales_kafka/
├── config/
│   ├── KafkaConfig.java          # Configuración de Kafka
│   └── ParadasConfig.java        # Paradas predefinidas
├── consumer/
│   └── UbicacionVehiculoConsumer.java  # Consumer Kafka
├── controller/
│   └── ProcesadorController.java      # REST endpoints
├── model/
│   ├── UbicacionVehiculo.java    # DTO ubicación
│   ├── HorarioVehiculo.java      # DTO horario
│   └── Parada.java               # DTO parada
├── service/
│   └── ProcesamientoSenialesService.java  # Lógica de negocio
└── ProcesaSenialesKafkaApplication.java   # Main
```

## 📦 Dependencias Principales

```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc</artifactId>
</dependency>
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>
```

## 🐛 Troubleshooting

### Error: No se pueden consumir mensajes

Verificar:
1. Kafka está corriendo: `docker ps`
2. Tópico existe: `kafka-topics --list`
3. Puerto correcto en `application.properties`

### Error: No se publican horarios

Verificar:
1. Logs de distancias calculadas
2. Umbral de distancia en configuración
3. Ubicaciones reales de paradas vs vehículos

## 📄 Licencia

Proyecto académico - 2026

## Ignacio Andana - Bastian Cortes

Desarrollo para sistema de monitorización de vehículos
