package br.com.fiap.aquaorbital.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "TB_FAZENDA", schema = "rm562085")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Fazenda extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_fazenda")
    @SequenceGenerator(name = "sq_fazenda", sequenceName = "rm562085.SQ_FAZENDA", allocationSize = 1)
    @Column(name = "id_fazenda")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_responsavel", nullable = false)
    private Usuario usuarioResponsavel;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "cidade", nullable = false, length = 100)
    private String cidade;

    @Column(name = "uf", nullable = false, length = 2)
    private String uf;

    @Column(name = "latitude", precision = 10, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 6)
    private BigDecimal longitude;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "ATIVA";

    @OneToMany(mappedBy = "fazenda", fetch = FetchType.LAZY)
    private List<Tanque> tanques;

    @OneToMany(mappedBy = "fazenda", fetch = FetchType.LAZY)
    private List<LoteBiomassa> lotes;

    @OneToMany(mappedBy = "fazenda", fetch = FetchType.LAZY)
    private List<CreditoCarbono> creditos;
}