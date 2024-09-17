package br.com.monitoramento.weblumio.controllers;

import br.com.monitoramento.weblumio.dtos.ApiResponseDTO;
import br.com.monitoramento.weblumio.entities.companySetting.CompanySettingDTO;
import br.com.monitoramento.weblumio.services.CompanySettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping(value = "/api/company-settings/")
public class CompanySettingController {

    private final CompanySettingService companySettingService;

    @Autowired
    public CompanySettingController(final CompanySettingService companySettingService) {
        this.companySettingService = companySettingService;
    }

    @GetMapping(value = "find-by-user")
    public ResponseEntity<ApiResponseDTO> findByUser(
            @RequestHeader("Authorization") @Validated String token
    ) {

        ApiResponseDTO response = companySettingService.findCompanyByUser(token);
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }

    @PutMapping(value = "update-by-id/{id}")
    public ResponseEntity<ApiResponseDTO> updateById(
            @RequestHeader("Authorization") @Validated String token,
            @PathVariable Long id,
            @RequestBody @Validated CompanySettingDTO companySettingDTO
    ) {

        ApiResponseDTO response = companySettingService.updateCompanyById(token, id, companySettingDTO);
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }

}
