package br.com.monitoramento.weblumio.repositories;

import br.com.monitoramento.weblumio.entities.sensor.Sensor;
import br.com.monitoramento.weblumio.entities.unit.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SensorRepository extends JpaRepository<Sensor, Long> {

    Optional<Sensor> findByIdentifierAndUnit(String identifier, Unit unit);

    List<Sensor> findByUnit(Unit unit);
}
