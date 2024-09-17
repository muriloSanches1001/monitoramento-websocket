package br.com.monitoramento.weblumio.services;

import br.com.monitoramento.weblumio.dtos.ApiResponseDTO;
import br.com.monitoramento.weblumio.entities.companySetting.CompanySetting;
import br.com.monitoramento.weblumio.entities.companySetting.CompanySettingDTO;
import br.com.monitoramento.weblumio.entities.companySetting.CompanySettingResponseDTO;
import br.com.monitoramento.weblumio.entities.user.User;
import br.com.monitoramento.weblumio.enums.ErrorCode;
import br.com.monitoramento.weblumio.enums.SucessCode;
import br.com.monitoramento.weblumio.repositories.CompanySettingRepository;
import br.com.monitoramento.weblumio.utils.ValidationUtils;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class CompanySettingService {

    private final CompanySettingRepository companySettingRepository;
    private final ValidationUtils validationUtils;
    private final UserService userService;

    @Autowired
    public CompanySettingService(
            CompanySettingRepository companySettingRepository,
            ValidationUtils validationUtils,
            UserService userService
    ) {
        this.companySettingRepository = companySettingRepository;
        this.validationUtils = validationUtils;
        this.userService = userService;
    }

    public ApiResponseDTO findCompanyByUser(String token) {
        try {
            User adminUser = userService.getUserByToken(token);
            if (!validationUtils.isUserAdmin(adminUser)) {
                log.error("Unauthorized user or invalid admin token");
                return new ApiResponseDTO(ErrorCode.UNAUTHORIZED, "Unauthorized or invalid admin token", HttpStatus.UNAUTHORIZED);
            }

            CompanySettingResponseDTO companySettingResponseDTO = this.companySettingToCompanySettingResponseDTO(adminUser.getCompany().getCompanySetting());

            log.info("Company fetched");
            return new ApiResponseDTO(SucessCode.FINDED, "Company fetched", HttpStatus.OK, companySettingResponseDTO);
        } catch (Exception e) {
            log.error("Error while getting company, error: {}", e.getMessage());
            return new ApiResponseDTO(ErrorCode.INTERNAL_SERVER_ERROR, "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ApiResponseDTO updateCompanyById(String token, Long id, CompanySettingDTO companySettingDTO) {
        try {
            User adminUser = userService.getUserByToken(token);
            if (!validationUtils.isUserAdmin(adminUser)) {
                log.error("Unauthorized user or invalid admin token");
                return new ApiResponseDTO(ErrorCode.UNAUTHORIZED, "Unauthorized or invalid admin token", HttpStatus.UNAUTHORIZED);
            }

            Optional<CompanySetting> companySettingOptional = companySettingRepository.findById(id);
            if (companySettingOptional.isEmpty()) {
                log.error("Company setting not found");
                return new ApiResponseDTO(ErrorCode.NOT_FOUND, "Company setting not found", HttpStatus.NOT_FOUND);
            }
            CompanySetting companySetting = companySettingOptional.get();

            if (!validationUtils.isEmailValid(companySettingDTO.notificationEmail())) {
                log.error("Invalid email address");
                return new ApiResponseDTO(ErrorCode.INVALID_EMAIL, "Invalid email address", HttpStatus.CONFLICT);
            }

            companySetting.setNotificationEmail(companySettingDTO.notificationEmail());
            companySettingRepository.save(companySetting);

            log.info("Company setting updated");
            return new ApiResponseDTO(SucessCode.UPDATED, "Company setting updated", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while updating company, error: {}", e.getMessage());
            return new ApiResponseDTO(ErrorCode.INTERNAL_SERVER_ERROR, "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public CompanySettingResponseDTO companySettingToCompanySettingResponseDTO(CompanySetting companySetting) {
        return new CompanySettingResponseDTO(
                companySetting.getNotificationEmail()
        );
    }

}
