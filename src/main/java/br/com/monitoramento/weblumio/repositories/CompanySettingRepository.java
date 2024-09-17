package br.com.monitoramento.weblumio.repositories;

import br.com.monitoramento.weblumio.entities.companySetting.CompanySetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanySettingRepository extends JpaRepository<CompanySetting, Long> {
}
