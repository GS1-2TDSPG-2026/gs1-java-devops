package br.com.fiap.Phycocarbon.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "TB_LOTE_BIOMASSA", schema = "rm562085")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class LoteBiomassa extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_lote_biomassa")
    @SequenceGenerator(name = "sq_lote_biomassa", sequenceName = "rm562085.SQ_LOTE_BIOMASSA", allocationSize = 1)
    @Column(name = "id_lote")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_fazenda", nullable = false)
    private Fazenda fazenda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tanque", nullable = false)
    private Tanque tanque;

    @Column(name = "taxonomia_alga", nullable = false, length = 100)
    private String taxonomiaAlga;

    @Column(name = "peso_kg", nullable = false, precision = 10, scale = 3)
    private BigDecimal pesoKg;

    @Column(name = "preco_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precoUnitario;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "DISPONIVEL";


    @Column(name = "dt_colheita", nullable = false, updatable = false)
    @Builder.Default
    private LocalDate dtColheita = LocalDate.now();

    @OneToMany(mappedBy = "lote", fetch = FetchType.LAZY)
    private List<CreditoCarbono> creditos;

    @OneToMany(mappedBy = "lote", fetch = FetchType.LAZY)
    private List<TransacaoMarketplace> transacoes;
}
