package br.com.monitoramento.weblumio.services;

import br.com.monitoramento.weblumio.dtos.ApiResponseDTO;
import br.com.monitoramento.weblumio.entities.sensor.Sensor;
import br.com.monitoramento.weblumio.entities.sensor.SensorResponseDTO;
import br.com.monitoramento.weblumio.entities.sensorData.SensorData;
import br.com.monitoramento.weblumio.entities.sensorData.SensorDataDTO;
import br.com.monitoramento.weblumio.entities.sensorData.SensorDataResponseDTO;
import br.com.monitoramento.weblumio.entities.unit.Unit;
import br.com.monitoramento.weblumio.entities.user.User;
import br.com.monitoramento.weblumio.enums.ErrorCode;
import br.com.monitoramento.weblumio.enums.SucessCode;
import br.com.monitoramento.weblumio.handlers.SensorWebSocketHandler;
import br.com.monitoramento.weblumio.repositories.SensorDataRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class SensorDataService {

    private final SensorDataRepository sensorDataRepository;
    private final SensorService sensorService;
    private final UserService userService;
    private final SensorWebSocketHandler sensorWebSocketHandler;

    @Autowired
    public SensorDataService(
            SensorDataRepository sensorDataRepository,
            SensorService sensorService,
            UserService userService,
            SensorWebSocketHandler sensorWebSocketHandler
    ) {
        this.sensorDataRepository = sensorDataRepository;
        this.sensorService = sensorService;
        this.userService = userService;
        this.sensorWebSocketHandler = sensorWebSocketHandler;
    }

    public ApiResponseDTO findSensorDatasBySensor(String token, Long sensorId) {
        try {
            Optional<Sensor> optionalSensor = this.sensorService.getSensorById(sensorId);
            if (optionalSensor.isEmpty()) {
                log.error("Sensor not found");
                return new ApiResponseDTO(ErrorCode.NOT_FOUND, "Sensor not found", HttpStatus.NOT_FOUND);
            }
            Sensor sensor = optionalSensor.get();

            User user = this.userService.getUserByToken(token);
            if (!sensor.getUnit().getCompany().getId().equals(user.getCompany().getId())) {
                log.error("User does not have access to this sensor data´s");
                return new ApiResponseDTO(ErrorCode.UNAUTHORIZED, "User does not have access to this sensor data´s", HttpStatus.UNAUTHORIZED);
            }

            List<SensorData> sensorDatas = this.sensorDataRepository.findBySensor(sensor);
            List<SensorDataResponseDTO> sensorDataResponseDTOS = new ArrayList<>();
            for (SensorData sensorData : sensorDatas) {
                sensorDataResponseDTOS.add(this.sensorDataToSensorDataResponseDTO(sensorData));
            }

            log.info("Sensor data´s fetched");
            return new ApiResponseDTO(SucessCode.FINDED, "Sensor data´s fetched", HttpStatus.OK, sensorDataResponseDTOS);
        } catch (Exception e) {
            log.error("Error while fetching sensor data, error: {}", e.getMessage());
            return new ApiResponseDTO(ErrorCode.INTERNAL_SERVER_ERROR, "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void createSensorData(SensorDataDTO sensorDataDTO) {
        try {
            Optional<Sensor> optionalSensor = this.sensorService.getSensorById(sensorDataDTO.sensorId());
            if (optionalSensor.isEmpty()) {
                log.error("Sensor not found to create sensor data");
                return;
            }
            Sensor sensor = optionalSensor.get();

            SensorData sensorData = new SensorData(
                    sensor,
                    LocalDateTime.now(),
                    sensorDataDTO.value()
            );

            this.sensorService.setSensorLastReadingTimeAndLastReadingValue(sensor, sensorData.getDateTime(), sensorData.getValue());

            String message = new ObjectMapper().writeValueAsString(sensorData.getValue().toString());
            this.sensorWebSocketHandler.sendSensorData(sensor.getId(), message);

            this.sensorDataRepository.save(sensorData);
            log.info("Sensor data with sensor '{}' created", sensorDataDTO.sensorId());
        } catch (Exception e) {
            log.error("Error while creating sensor data with sensor '{}', error: '{}'", sensorDataDTO.sensorId(), e.getMessage());
        }
    }

    public SensorDataResponseDTO sensorDataToSensorDataResponseDTO(SensorData sensorData) {
        return new SensorDataResponseDTO(
                sensorData.getId(),
                sensorData.getDateTime(),
                sensorData.getValue()
        );
    }

}
