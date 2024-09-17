package br.com.monitoramento.weblumio.services;

import br.com.monitoramento.weblumio.dtos.ApiResponseDTO;
import br.com.monitoramento.weblumio.entities.sensor.Sensor;
import br.com.monitoramento.weblumio.entities.sensor.SensorDTO;
import br.com.monitoramento.weblumio.entities.sensor.SensorResponseDTO;
import br.com.monitoramento.weblumio.entities.unit.Unit;
import br.com.monitoramento.weblumio.entities.unit.UnitDTO;
import br.com.monitoramento.weblumio.entities.unit.UnitResponseDTO;
import br.com.monitoramento.weblumio.entities.user.User;
import br.com.monitoramento.weblumio.enums.ErrorCode;
import br.com.monitoramento.weblumio.enums.SucessCode;
import br.com.monitoramento.weblumio.repositories.UnitRepository;
import br.com.monitoramento.weblumio.utils.ValidationUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.core.util.UuidUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class UnitService {

    private final UnitRepository unitRepository;
    private final UserService userService;
    private final ValidationUtils validationUtils;

    @Autowired
    public UnitService(
            UnitRepository unitRepository,
            UserService userService,
            ValidationUtils validationUtils
    ) {
        this.unitRepository = unitRepository;
        this.userService = userService;
        this.validationUtils = validationUtils;
    }

    public ApiResponseDTO findUnitsByUser(String token) {
        try {
            User user = this.userService.getUserByToken(token);

            List<Unit> units = user.getCompany().getUnits();
            List<UnitResponseDTO> unitResponseDTOS = new ArrayList<>();
            for (Unit unit : units) {
                unitResponseDTOS.add(this.unitToUnitResponseDTO(unit));
            }

            log.info("Units fetched");
            return new ApiResponseDTO(SucessCode.FINDED, "Units fetched", HttpStatus.OK, unitResponseDTOS);
        } catch (Exception e) {
            log.error("Error while fetching unit, error: {}", e.getMessage());
            return new ApiResponseDTO(ErrorCode.INTERNAL_SERVER_ERROR, "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ApiResponseDTO createUnit(String token, UnitDTO unitDTO) {

        if (unitDTO.name() == null || unitDTO.name().isEmpty()) {
            log.error("Invalid unit name");
            return new ApiResponseDTO(ErrorCode.INVALID_IDENTIFIER, "Invalid Unit Name", HttpStatus.CONFLICT);
        }

        if (unitDTO.address() == null || unitDTO.address().isEmpty()) {
            log.error("Invalid unit address");
            return new ApiResponseDTO(ErrorCode.INVALID_DESCRIPTION, "Invalid Unit Address", HttpStatus.CONFLICT);
        }

        try {
            User adminUser = userService.getUserByToken(token);
            if (!validationUtils.isUserAdmin(adminUser)) {
                log.error("Unauthorized user or invalid admin token");
                return new ApiResponseDTO(ErrorCode.UNAUTHORIZED, "Unauthorized or invalid admin token", HttpStatus.UNAUTHORIZED);
            }

            Optional<Unit> existingUnit = this.unitRepository.findByName(unitDTO.name());
            if (existingUnit.isPresent()) {
                log.error("Unit with name '{}' already exists in the company '{}'", unitDTO.name(), adminUser.getCompany().getId());
                return new ApiResponseDTO(ErrorCode.ALREADY_EXISTS, "Unit with this name already exists in the company", HttpStatus.CONFLICT);
            }

            Unit unit = new Unit(
                    unitDTO.name(),
                    unitDTO.address(),
                    adminUser.getCompany()
            );

            this.unitRepository.save(unit);
            log.info("Unit created");
            return new ApiResponseDTO(SucessCode.CREATED, "Unit created", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while creating unit, error: {}", e.getMessage());
            return new ApiResponseDTO(ErrorCode.INTERNAL_SERVER_ERROR, "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ApiResponseDTO updateUnit(String token, Long sensorId, UnitDTO unitDTO) {

        if (unitDTO.name() == null || unitDTO.name().isEmpty()) {
            log.error("Invalid unit name");
            return new ApiResponseDTO(ErrorCode.INVALID_IDENTIFIER, "Invalid Unit Name", HttpStatus.CONFLICT);
        }

        if (unitDTO.address() == null || unitDTO.address().isEmpty()) {
            log.error("Invalid unit address");
            return new ApiResponseDTO(ErrorCode.INVALID_DESCRIPTION, "Invalid Unit Address", HttpStatus.CONFLICT);
        }

        try {
            User adminUser = userService.getUserByToken(token);
            if (!validationUtils.isUserAdmin(adminUser)) {
                log.error("Unauthorized user or invalid admin token");
                return new ApiResponseDTO(ErrorCode.UNAUTHORIZED, "Unauthorized or invalid admin token", HttpStatus.UNAUTHORIZED);
            }

            Optional<Unit> optionalUnit = this.getUnitById(sensorId);
            if (optionalUnit.isEmpty()) {
                log.error("Unit not found");
                return new ApiResponseDTO(ErrorCode.NOT_FOUND, "Unit not found", HttpStatus.NOT_FOUND);
            }
            Unit unit = optionalUnit.get();

            if (!unit.getCompany().getId().equals(adminUser.getCompany().getId())) {
                log.error("User does not have access to update this unit");
                return new ApiResponseDTO(ErrorCode.UNAUTHORIZED, "User does not have access to update this unit", HttpStatus.UNAUTHORIZED);
            }

            Optional<Unit> existingUnit = this.unitRepository.findByName(unitDTO.name());
            if (existingUnit.isPresent() && !existingUnit.get().getId().equals(unit.getId())) {
                log.error("Unit with name '{}' already exists in the company '{}'", unitDTO.name(), unit.getId());
                return new ApiResponseDTO(ErrorCode.ALREADY_EXISTS, "Unit with this name already exists in the company", HttpStatus.CONFLICT);
            }

            unit.setName(unitDTO.name());
            unit.setAddress(unitDTO.address());

            this.unitRepository.save(unit);

            log.info("Unit updated");
            return new ApiResponseDTO(SucessCode.UPDATED, "Unit updated", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while updating unit, error: {}", e.getMessage());
            return new ApiResponseDTO(ErrorCode.INTERNAL_SERVER_ERROR, "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ApiResponseDTO deleteUnit(String token, Long unitId) {
        try {
            User adminUser = this.userService.getUserByToken(token);
            if (!validationUtils.isUserAdmin(adminUser)) {
                log.error("Unauthorized user or invalid admin token");
                return new ApiResponseDTO(ErrorCode.UNAUTHORIZED, "Unauthorized or invalid admin token", HttpStatus.UNAUTHORIZED);
            }

            Optional<Unit> optionalUnit = this.getUnitById(unitId);
            if (optionalUnit.isEmpty()) {
                log.error("Unit not found");
                return new ApiResponseDTO(ErrorCode.NOT_FOUND, "Unit not found", HttpStatus.NOT_FOUND);
            }
            Unit unit = optionalUnit.get();

            if (!unit.getCompany().getId().equals(adminUser.getCompany().getId())) {
                log.error("User does not have access to delete this unit");
                return new ApiResponseDTO(ErrorCode.UNAUTHORIZED, "User does not have access to delete this unit", HttpStatus.UNAUTHORIZED);
            }

            this.unitRepository.delete(unit);

            log.info("Unit deleted");
            return new ApiResponseDTO(SucessCode.DELETED, "Unit deleted", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while deleting unit, error: {}", e.getMessage());
            return new ApiResponseDTO(ErrorCode.INTERNAL_SERVER_ERROR, "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public Optional<Unit> getUnitById(Long id) {
        return unitRepository.findById(id);
    }

    public UnitResponseDTO unitToUnitResponseDTO(Unit unit) {
        return new UnitResponseDTO(
                unit.getId(),
                unit.getName(),
                unit.getAddress()
        );
    }

}
