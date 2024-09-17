package br.com.monitoramento.weblumio.controllers;

import br.com.monitoramento.weblumio.dtos.ApiResponseDTO;
import br.com.monitoramento.weblumio.entities.sensorData.SensorDataDTO;
import br.com.monitoramento.weblumio.services.SensorDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/sensor-data")
public class SensorDataController {

    private final SensorDataService sensorDataService;

    @Autowired
    public SensorDataController(
            SensorDataService sensorDataService
    ) {
        this.sensorDataService = sensorDataService;
    }

    public void create(
            SensorDataDTO sensorDataDTO) {

        this.sensorDataService.createSensorData(sensorDataDTO);
    }

    @GetMapping(value = "find-by-sensor/{id}")
    public ResponseEntity<ApiResponseDTO> findBySensor(
            @RequestHeader("Authorization") @Validated String token,
            @PathVariable @Validated Long id) {

        ApiResponseDTO response = this.sensorDataService.findSensorDatasBySensor(token, id);
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }

}
