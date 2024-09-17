package br.com.monitoramento.weblumio.entities.unit;

import br.com.monitoramento.weblumio.entities.company.Company;
import br.com.monitoramento.weblumio.entities.sensor.Sensor;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "units")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Unit implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String address;

    public Unit(String name, String address, Company company) {
        this.name = name;
        this.address = address;
        this.company = company;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @OneToMany(mappedBy = "unit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Sensor> sensors;
}
