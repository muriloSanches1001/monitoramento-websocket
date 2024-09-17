package br.com.monitoramento.weblumio.services;

import br.com.monitoramento.weblumio.dtos.ApiResponseDTO;
import br.com.monitoramento.weblumio.entities.companySetting.CompanySettingResponseDTO;
import br.com.monitoramento.weblumio.entities.sensor.Sensor;
import br.com.monitoramento.weblumio.entities.sensor.SensorDTO;
import br.com.monitoramento.weblumio.entities.sensor.SensorResponseDTO;
import br.com.monitoramento.weblumio.entities.unit.Unit;
import br.com.monitoramento.weblumio.entities.user.User;
import br.com.monitoramento.weblumio.enums.ErrorCode;
import br.com.monitoramento.weblumio.enums.SucessCode;
import br.com.monitoramento.weblumio.repositories.SensorRepository;
import br.com.monitoramento.weblumio.utils.ValidationUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class SensorService {

    private final SensorRepository sensorRepository;
    private final ValidationUtils validationUtils;
    private final UserService userService;
    private final UnitService unitService;

    @Autowired
    public SensorService(
            SensorRepository sensorRepository,
            ValidationUtils validationUtils,
            UserService userService,
            UnitService unitService
    ) {
        this.sensorRepository = sensorRepository;
        this.validationUtils = validationUtils;
        this.userService = userService;
        this.unitService = unitService;
    }

    public ApiResponseDTO findSensorsByUnit(String token, Long unitId) {
        try {
            Optional<Unit> optionalUnit = this.unitService.getUnitById(unitId);
            if (optionalUnit.isEmpty()) {
                log.error("Unit not found");
                return new ApiResponseDTO(ErrorCode.NOT_FOUND, "Unit not found", HttpStatus.NOT_FOUND);
            }
            Unit unit = optionalUnit.get();

            User user = this.userService.getUserByToken(token);
            if (!unit.getCompany().getId().equals(user.getCompany().getId())) {
                log.error("User does not have access to this unit's sensors");
                return new ApiResponseDTO(ErrorCode.UNAUTHORIZED, "User does not have access to this unit's sensors", HttpStatus.UNAUTHORIZED);
            }

            List<Sensor> sensors = this.sensorRepository.findByUnit(unit);
            List<SensorResponseDTO> sensorResponseDTOS = new ArrayList<>();
            for (Sensor sensor : sensors) {
                sensorResponseDTOS.add(this.sensorToSensorResponseDTO(sensor));
            }

            log.info("Sensors fetched");
            return new ApiResponseDTO(SucessCode.FINDED, "Sensors fetched", HttpStatus.OK, sensorResponseDTOS);
        } catch (Exception e) {
            log.error("Error while fetching sensor, error: {}", e.getMessage());
            return new ApiResponseDTO(ErrorCode.INTERNAL_SERVER_ERROR, "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ApiResponseDTO createSensor(String token, SensorDTO sensorDTO) {

        if (sensorDTO.sensorType() == null || Objects.equals(sensorDTO.sensorType().toString(), "")) {
            log.error("Invalid sensor type");
            return new ApiResponseDTO(ErrorCode.INVALID_SENSOR_TYPE, "Invalid Sensor Type", HttpStatus.CONFLICT);
        }

        if (sensorDTO.identifier() == null || sensorDTO.identifier().isEmpty()) {
            log.error("Invalid sensor identifier");
            return new ApiResponseDTO(ErrorCode.INVALID_IDENTIFIER, "Invalid Sensor Identifier", HttpStatus.CONFLICT);
        }

        if (sensorDTO.description() == null || sensorDTO.description().isEmpty()) {
            log.error("Invalid sensor description");
            return new ApiResponseDTO(ErrorCode.INVALID_DESCRIPTION, "Invalid Sensor Description", HttpStatus.CONFLICT);
        }

        try {
            User adminUser = userService.getUserByToken(token);
            if (!validationUtils.isUserAdmin(adminUser)) {
                log.error("Unauthorized user or invalid admin token");
                return new ApiResponseDTO(ErrorCode.UNAUTHORIZED, "Unauthorized or invalid admin token", HttpStatus.UNAUTHORIZED);
            }

            Optional<Unit> optionalUnit = this.unitService.getUnitById(sensorDTO.unitId());

            if (optionalUnit.isEmpty()) {
                log.error("Unit not found");
                return new ApiResponseDTO(ErrorCode.NOT_FOUND, "Unit not found", HttpStatus.NOT_FOUND);
            }
            Unit unit = optionalUnit.get();

            Optional<Sensor> existingSensor = sensorRepository.findByIdentifierAndUnit(sensorDTO.identifier(), unit);
            if (existingSensor.isPresent()) {
                log.error("Sensor with identifier '{}' already exists in the unit '{}'", sensorDTO.identifier(), unit.getId());
                return new ApiResponseDTO(ErrorCode.ALREADY_EXISTS, "Sensor with this identifier already exists in the unit", HttpStatus.CONFLICT);
            }

            Sensor sensor = new Sensor(
                    sensorDTO.identifier(),
                    sensorDTO.description(),
                    unit,
                    sensorDTO.sensorType()
            );

            sensorRepository.save(sensor);

            log.info("Sensor created");
            return new ApiResponseDTO(SucessCode.CREATED, "Sensor created", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while creating sensor, error: {}", e.getMessage());
            return new ApiResponseDTO(ErrorCode.INTERNAL_SERVER_ERROR, "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ApiResponseDTO updateSensor(String token, Long sensorId, SensorDTO sensorDTO) {
        if (sensorDTO.sensorType() == null || Objects.equals(sensorDTO.sensorType().toString(), "")) {
            log.error("Invalid sensor type");
            return new ApiResponseDTO(ErrorCode.INVALID_SENSOR_TYPE, "Invalid Sensor Type", HttpStatus.CONFLICT);
        }

        if (sensorDTO.identifier() == null || sensorDTO.identifier().isEmpty()) {
            log.error("Invalid sensor identifier");
            return new ApiResponseDTO(ErrorCode.INVALID_IDENTIFIER, "Invalid Sensor Identifier", HttpStatus.CONFLICT);
        }

        if (sensorDTO.description() == null || sensorDTO.description().isEmpty()) {
            log.error("Invalid sensor description");
            return new ApiResponseDTO(ErrorCode.INVALID_DESCRIPTION, "Invalid Sensor Description", HttpStatus.CONFLICT);
        }

        try {
            User adminUser = userService.getUserByToken(token);
            if (!validationUtils.isUserAdmin(adminUser)) {
                log.error("Unauthorized user or invalid admin token");
                return new ApiResponseDTO(ErrorCode.UNAUTHORIZED, "Unauthorized or invalid admin token", HttpStatus.UNAUTHORIZED);
            }

            Optional<Sensor> optionalSensor = this.getSensorById(sensorId);
            if (optionalSensor.isEmpty()) {
                log.error("Sensor not found");
                return new ApiResponseDTO(ErrorCode.NOT_FOUND, "Sensor not found", HttpStatus.NOT_FOUND);
            }
            Sensor sensor = optionalSensor.get();

            if (!sensor.getUnit().getCompany().getId().equals(adminUser.getCompany().getId())) {
                log.error("User does not have access to update this sensor");
                return new ApiResponseDTO(ErrorCode.UNAUTHORIZED, "User does not have access to update this sensor", HttpStatus.UNAUTHORIZED);
            }

            Optional<Unit> optionalUnit = this.unitService.getUnitById(sensorDTO.unitId());

            if (optionalUnit.isEmpty()) {
                log.error("Unit not found");
                return new ApiResponseDTO(ErrorCode.NOT_FOUND, "Unit not found", HttpStatus.NOT_FOUND);
            }
            Unit unit = optionalUnit.get();

            Optional<Sensor> existingSensor = sensorRepository.findByIdentifierAndUnit(sensorDTO.identifier(), unit);
            if (existingSensor.isPresent() && !existingSensor.get().getId().equals(sensor.getId())) {
                log.error("Sensor with identifier '{}' already exists in the unit '{}'", sensorDTO.identifier(), unit.getId());
                return new ApiResponseDTO(ErrorCode.ALREADY_EXISTS, "Sensor with this identifier already exists in the unit", HttpStatus.CONFLICT);
            }

            sensor.setDescription(sensorDTO.description());
            sensor.setIdentifier(sensorDTO.identifier());
            sensor.setUnit(unit);
            sensor.setType(sensorDTO.sensorType());
            sensorRepository.save(sensor);

            log.info("Sensor updated");
            return new ApiResponseDTO(SucessCode.UPDATED, "Sensor updated", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while updating sensor, error: {}", e.getMessage());
            return new ApiResponseDTO(ErrorCode.INTERNAL_SERVER_ERROR, "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ApiResponseDTO deleteSensor(String token, Long sensorId) {
        try {
            User adminUser = this.userService.getUserByToken(token);
            if (!validationUtils.isUserAdmin(adminUser)) {
                log.error("Unauthorized user or invalid admin token");
                return new ApiResponseDTO(ErrorCode.UNAUTHORIZED, "Unauthorized or invalid admin token", HttpStatus.UNAUTHORIZED);
            }

            Optional<Sensor> optionalSensor = this.getSensorById(sensorId);
            if (optionalSensor.isEmpty()) {
                log.error("Sensor not found");
                return new ApiResponseDTO(ErrorCode.NOT_FOUND, "Sensor not found", HttpStatus.NOT_FOUND);
            }
            Sensor sensor = optionalSensor.get();

            if (!sensor.getUnit().getCompany().getId().equals(adminUser.getCompany().getId())) {
                log.error("User does not have access to delete this sensor");
                return new ApiResponseDTO(ErrorCode.UNAUTHORIZED, "User does not have access to delete this sensor", HttpStatus.UNAUTHORIZED);
            }

            this.sensorRepository.delete(sensor);

            log.info("Sensor deleted");
            return new ApiResponseDTO(SucessCode.DELETED, "Sensor deleted", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while deleting sensor, error: {}", e.getMessage());
            return new ApiResponseDTO(ErrorCode.INTERNAL_SERVER_ERROR, "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public Optional<Sensor> getSensorById(Long sensorId) {
        return sensorRepository.findById(sensorId);
    }

    public void setSensorLastReadingTimeAndLastReadingValue(Sensor sensor, LocalDateTime lastReadingTime, Short lastReadingValue) {
        sensor.setLastReadingTime(lastReadingTime);
        sensor.setLastReadingValue(lastReadingValue);
        this.sensorRepository.save(sensor);
    }

    public SensorResponseDTO sensorToSensorResponseDTO(Sensor sensor) {
        return new SensorResponseDTO(
                sensor.getId(),
                sensor.getIdentifier(),
                sensor.getDescription(),
                sensor.getType(),
                sensor.getLastReadingTime(),
                sensor.getLastReadingValue()
        );
    }
}
