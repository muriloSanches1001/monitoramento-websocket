package br.com.monitoramento.weblumio.entities.sensor;

import br.com.monitoramento.weblumio.entities.sensorData.SensorData;
import br.com.monitoramento.weblumio.entities.unit.Unit;
import br.com.monitoramento.weblumio.enums.SensorType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "sensors")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Sensor implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String identifier;
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SensorType type;

    private LocalDateTime lastReadingTime;
    private Short lastReadingValue;

    public Sensor(String identifier, String description, Unit unit, SensorType type) {
        this.identifier = identifier;
        this.description = description;
        this.unit = unit;
        this.type = type;
    }

    @OneToMany(mappedBy = "sensor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SensorData> sensorData;
}