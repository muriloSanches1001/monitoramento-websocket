package br.com.monitoramento.weblumio.entities.company;

import br.com.monitoramento.weblumio.entities.companySetting.CompanySetting;
import br.com.monitoramento.weblumio.entities.unit.Unit;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "companies")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Company implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    @Column(name = "company_code", nullable = false, unique = true)
    private String companyCode;
    private String address;
    private String phone;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JoinColumn(name = "company_setting_id", nullable = false)
    private CompanySetting companySetting;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Unit> units;

    public Company(String name, String companyCode, String address, String phone, CompanySetting companySetting) {
        this.name = name;
        this.companyCode = companyCode;
        this.address = address;
        this.phone = phone;
        this.companySetting = companySetting;
    }
}
