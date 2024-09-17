package br.com.monitoramento.weblumio.entities.companySetting;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Entity
@Table(name = "company_settings")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CompanySetting implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "notification_email")
    private String notificationEmail;
}
