package br.com.fiap.aquaorbital.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "TB_DADO_ORBITAL", schema = "rm562085")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DadoOrbital {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_dado_orbital")
    @SequenceGenerator(name = "sq_dado_orbital", sequenceName = "rm562085.SQ_DADO_ORBITAL", allocationSize = 1)
    @Column(name = "id_dado_orbital")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_fazenda", nullable = false)
    private Fazenda fazenda;

    @Column(name = "fonte", nullable = false, length = 50)
    private String fonte;

    @Column(name = "dt_coleta", nullable = false)
    private LocalDate dtColeta;

    @Column(name = "irradiancia_par", precision = 10, scale = 4)
    private BigDecimal irradianciaParTot;

    @Column(name = "nebulosidade", precision = 5, scale = 2)
    private BigDecimal nebulosidade;

    @Column(name = "temperatura_ambiente", precision = 5, scale = 2)
    private BigDecimal temperaturaAmbiente;

    @Column(name = "latitude", precision = 10, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 6)
    private BigDecimal longitude;

    @Column(name = "dt_registro", updatable = false)
    @Builder.Default
    private LocalDateTime dtRegistro = LocalDateTime.now();
}
