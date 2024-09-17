package br.com.monitoramento.weblumio.entities.sensor;

import br.com.monitoramento.weblumio.enums.SensorType;

public record SensorDTO(String identifier, String description, Long unitId, SensorType sensorType) {
}
