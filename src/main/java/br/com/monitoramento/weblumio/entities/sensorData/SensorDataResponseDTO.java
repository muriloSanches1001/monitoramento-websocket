package br.com.monitoramento.weblumio.entities.sensorData;

import java.time.LocalDateTime;

public record SensorDataResponseDTO(Long id, LocalDateTime localDateTime, Short value) {
}
