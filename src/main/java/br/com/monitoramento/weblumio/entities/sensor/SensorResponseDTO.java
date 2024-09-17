package br.com.monitoramento.weblumio.entities.sensor;

import br.com.monitoramento.weblumio.entities.unit.Unit;
import br.com.monitoramento.weblumio.enums.SensorType;

import java.time.LocalDateTime;

public record SensorResponseDTO(Long id, String identifier, String description, SensorType type, LocalDateTime lastReadingTime, Short lastReadingValue) {
}
