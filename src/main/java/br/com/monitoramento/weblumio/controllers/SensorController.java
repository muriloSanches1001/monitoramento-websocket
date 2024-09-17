package br.com.monitoramento.weblumio.controllers;

import br.com.monitoramento.weblumio.dtos.ApiResponseDTO;
import br.com.monitoramento.weblumio.entities.sensor.SensorDTO;
import br.com.monitoramento.weblumio.entities.user.UserDTO;
import br.com.monitoramento.weblumio.repositories.SensorRepository;
import br.com.monitoramento.weblumio.services.SensorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping(value = "/api/sensor/")
public class SensorController {

    private final SensorService sensorService;

    @Autowired
    public SensorController(
            SensorService sensorService
    ) {
        this.sensorService = sensorService;
    }

    @GetMapping(value = "find-by-unit/{id}")
    public ResponseEntity<ApiResponseDTO> findByUnit(
            @RequestHeader("Authorization") @Validated String token,
            @PathVariable @Validated Long id) {

        ApiResponseDTO response = this.sensorService.findSensorsByUnit(token, id);
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }

    @PostMapping(value = "create")
    public ResponseEntity<ApiResponseDTO> create(
            @RequestHeader("Authorization") @Validated String token,
            @RequestBody @Validated SensorDTO sensorDTO) {

        ApiResponseDTO response = this.sensorService.createSensor(token, sensorDTO);
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }

    @PutMapping(value = "update/{id}")
    public ResponseEntity<ApiResponseDTO> update(
            @RequestHeader("Authorization") @Validated String token,
            @RequestBody @Validated SensorDTO sensorDTO,
            @PathVariable @Validated Long id) {

        ApiResponseDTO response = this.sensorService.updateSensor(token, id, sensorDTO);
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }

    @DeleteMapping(value = "delete/{id}")
    public ResponseEntity<ApiResponseDTO> delete(
            @RequestHeader("Authorization") @Validated String token,
            @PathVariable @Validated Long id) {

        ApiResponseDTO response = this.sensorService.deleteSensor(token, id);
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }
}
