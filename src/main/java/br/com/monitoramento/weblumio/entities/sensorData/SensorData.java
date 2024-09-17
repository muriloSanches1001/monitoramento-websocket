package br.com.monitoramento.weblumio.entities.sensorData;

import br.com.monitoramento.weblumio.entities.sensor.Sensor;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_datas")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SensorData implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_id", nullable = false)
    private Sensor sensor;

    @Column(nullable = false)
    private LocalDateTime dateTime;
    @Column(nullable = false)
    private Short value;

    public SensorData(Sensor sensor, LocalDateTime dateTime, Short value) {
        this.sensor = sensor;
        this.dateTime = dateTime;
        this.value = value;
    }


}
