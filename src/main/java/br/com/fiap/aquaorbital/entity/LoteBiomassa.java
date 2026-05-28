package br.com.fiap.aquaorbital.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "TB_LOTE_BIOMASSA")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LoteBiomassa {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_lote")
    @SequenceGenerator(name = "seq_lote", sequenceName = "SEQ_LOTE", allocationSize = 1)
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
    private LocalDateTime dtColheita = LocalDateTime.now();

    @OneToMany(mappedBy = "lote", fetch = FetchType.LAZY)
    private List<CreditoCarbono> creditos;

    @OneToMany(mappedBy = "lote", fetch = FetchType.LAZY)
    private List<TransacaoMarketplace> transacoes;
}