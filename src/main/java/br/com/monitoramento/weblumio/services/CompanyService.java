package br.com.monitoramento.weblumio.services;

import br.com.monitoramento.weblumio.entities.company.Company;
import br.com.monitoramento.weblumio.repositories.CompanyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CompanyService {

    private final CompanyRepository companyRepository;

    @Autowired
    public CompanyService(
            CompanyRepository companyRepository
    ) {
        this.companyRepository = companyRepository;
    }

    public Company getCompanyByCompanyCode(String companyCode) {
        return companyRepository.findByCompanyCode(companyCode);
    }

}
