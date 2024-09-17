package br.com.monitoramento.weblumio.repositories;

import br.com.monitoramento.weblumio.entities.company.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Company findByCompanyCode(String companyCode);
}
