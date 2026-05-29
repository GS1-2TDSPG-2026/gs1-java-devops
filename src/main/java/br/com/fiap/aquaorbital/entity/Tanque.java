package br.com.fiap.aquaorbital.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "TB_TANQUE", schema = "rm562085")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Tanque {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_tanque")
    @SequenceGenerator(name = "seq_tanque", sequenceName = "rm562085.SEQ_TANQUE", allocationSize = 1)
    @Column(name = "id_tanque")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_fazenda", nullable = false)
    private Fazenda fazenda;

    @Column(name = "codigo_tanque", nullable = false, unique = true, length = 30)
    private String codigoTanque;

    @Column(name = "tipo_alga", nullable = false, length = 50)
    private String tipoAlga;

    @Column(name = "capacidade_litros", precision = 10, scale = 2)
    private BigDecimal capacidadeLitros;

    @Column(name = "ph_min", precision = 4, scale = 2)
    private BigDecimal phMin;

    @Column(name = "ph_max", precision = 4, scale = 2)
    private BigDecimal phMax;

    @Column(name = "temperatura_min", precision = 4, scale = 2)
    private BigDecimal temperaturaMin;

    @Column(name = "temperatura_max", precision = 4, scale = 2)
    private BigDecimal temperaturaMax;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "ATIVO";

    @Column(name = "dt_instalacao")
    private LocalDate dtInstalacao;

    @OneToMany(mappedBy = "tanque", fetch = FetchType.LAZY)
    private List<LoteBiomassa> lotes;
}