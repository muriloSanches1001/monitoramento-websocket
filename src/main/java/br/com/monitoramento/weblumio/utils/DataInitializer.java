package br.com.monitoramento.weblumio.utils;

import br.com.monitoramento.weblumio.entities.company.Company;
import br.com.monitoramento.weblumio.entities.companySetting.CompanySetting;
import br.com.monitoramento.weblumio.entities.unit.Unit;
import br.com.monitoramento.weblumio.entities.user.User;
import br.com.monitoramento.weblumio.enums.AccountType;
import br.com.monitoramento.weblumio.repositories.CompanyRepository;
import br.com.monitoramento.weblumio.repositories.CompanySettingRepository;
import br.com.monitoramento.weblumio.repositories.UnitRepository;
import br.com.monitoramento.weblumio.repositories.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Slf4j
public class DataInitializer {

    private final CompanyRepository companyRepository;
    private final UnitRepository unitRepository;
    private final UserRepository userRepository;
    private final CompanySettingRepository companySettingRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializer(
            CompanyRepository companyRepository,
            UnitRepository unitRepository,
            UserRepository userRepository,
            CompanySettingRepository companySettingRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.companyRepository = companyRepository;
        this.unitRepository = unitRepository;
        this.userRepository = userRepository;
        this.companySettingRepository = companySettingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    @PostConstruct
    public void initData() {
        log.info("Initializing data...");
        if (companyRepository.findByCompanyCode("companycode") == null) {
            try {
                CompanySetting companySetting = new CompanySetting();
                companySetting.setNotificationEmail("murilo@gmail.com");
                companySettingRepository.save(companySetting);

                Company company = new Company(
                        "Empresa",
                        "companycode",
                        "Endereço",
                        "00 0000 0000",
                        companySetting
                );
                companyRepository.save(company);

                Unit unit_1 = new Unit();
                unit_1.setName("Loja 1");
                unit_1.setAddress("Endereço 1");
                unit_1.setCompany(company);
                unitRepository.save(unit_1);

                User user = new User();
                user.setUsername("murilo@companycode");
                user.setPassword(passwordEncoder.encode("password"));
                user.setAccountType(AccountType.ADMIN);
                user.setCompany(company);
                userRepository.save(user);

                log.info("Initialized company '{}', unit_1 '{}' and user '{}'", company.getName(), unit_1.getName(), user.getUsername());
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }

}
