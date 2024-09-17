package br.com.monitoramento.weblumio.repositories;

import br.com.monitoramento.weblumio.entities.sensor.Sensor;
import br.com.monitoramento.weblumio.entities.sensorData.SensorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SensorDataRepository extends JpaRepository<SensorData, Long> {

    List<SensorData> findBySensor(Sensor sensor);

}
