package br.com.monitoramento.weblumio.controllers;

import br.com.monitoramento.weblumio.dtos.ApiResponseDTO;
import br.com.monitoramento.weblumio.entities.unit.UnitDTO;
import br.com.monitoramento.weblumio.services.UnitService;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/unit/")
public class UnitController {

    private final UnitService unitService;

    @Autowired
    public UnitController(
            UnitService unitService
    ) {
        this.unitService = unitService;
    }

    @GetMapping(value = "find-by-user")
    public ResponseEntity<ApiResponseDTO> findByUnit(
            @RequestHeader("Authorization") @Validated String token) {

        ApiResponseDTO response = this.unitService.findUnitsByUser(token);
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }

    @PostMapping(value = "create")
    public ResponseEntity<ApiResponseDTO> create(
            @RequestHeader("Authorization") @Validated String token,
            @RequestBody @Validated UnitDTO unitDTO) {

        ApiResponseDTO response = this.unitService.createUnit(token, unitDTO);
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }

    @PutMapping(value = "update/{id}")
    public ResponseEntity<ApiResponseDTO> update(
            @RequestHeader("Authorization") @Validated String token,
            @RequestBody @Validated UnitDTO unitDTO,
            @PathVariable @Validated Long id) {

        ApiResponseDTO response = this.unitService.updateUnit(token, id, unitDTO);
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }

    @DeleteMapping(value = "delete/{id}")
    public ResponseEntity<ApiResponseDTO> delete(
            @RequestHeader("Authorization") @Validated String token,
            @PathVariable @Validated Long id) {

        ApiResponseDTO response = this.unitService.deleteUnit(token, id);
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }

}
