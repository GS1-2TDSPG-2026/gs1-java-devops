package br.com.fiap.Phycocarbon.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "TB_CREDITO_CARBONO", schema = "rm562085")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CreditoCarbono extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_credito_carbono")
    @SequenceGenerator(name = "sq_credito_carbono", sequenceName = "rm562085.SQ_CREDITO_CARBONO", allocationSize = 1)
    @Column(name = "id_credito")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_fazenda", nullable = false)
    private Fazenda fazenda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_lote", nullable = false)
    private LoteBiomassa lote;

    @Column(name = "co2_toneladas", nullable = false, precision = 12, scale = 4)
    private BigDecimal co2Toneladas;

    @Column(name = "hash_auditoria", nullable = false, unique = true, length = 256)
    private String hashAuditoria;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "GERADO";

    @Column(name = "dt_validacao")
    private LocalDateTime dtValidacao;

    @OneToMany(mappedBy = "credito", fetch = FetchType.LAZY)
    private List<TransacaoMarketplace> transacoes;
}