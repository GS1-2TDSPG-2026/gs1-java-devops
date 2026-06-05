package br.com.fiap.Phycocarbon.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "TB_TRANSACAO_MARKETPLACE", schema = "rm562085")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TransacaoMarketplace extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_transacao_marketplace")
    @SequenceGenerator(name = "sq_transacao_marketplace", sequenceName = "rm562085.SQ_TRANSACAO_MARKETPLACE", allocationSize = 1)
    @Column(name = "id_transacao")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_comprador", nullable = false)
    private Usuario usuarioComprador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_lote")
    private LoteBiomassa lote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_credito")
    private CreditoCarbono credito;

    @Column(name = "tipo_transacao", nullable = false, length = 30)
    private String tipoTransacao;

    @Column(name = "quantidade", nullable = false, precision = 12, scale = 3)
    private BigDecimal quantidade;

    @Column(name = "valor_total", nullable = false, precision = 14, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDENTE";
}