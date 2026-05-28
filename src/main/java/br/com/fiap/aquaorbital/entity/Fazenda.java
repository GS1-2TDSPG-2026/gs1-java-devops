package br.com.fiap.aquaorbital.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "TB_FAZENDA")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Fazenda {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_fazenda")
    @SequenceGenerator(name = "seq_fazenda", sequenceName = "SEQ_FAZENDA", allocationSize = 1)
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
    private String status = "ATIVO";

    @Column(name = "dt_cadastro", updatable = false)
    @Builder.Default
    private LocalDateTime dtCadastro = LocalDateTime.now();

    @OneToMany(mappedBy = "fazenda", fetch = FetchType.LAZY)
    private List<Tanque> tanques;

    @OneToMany(mappedBy = "fazenda", fetch = FetchType.LAZY)
    private List<LoteBiomassa> lotes;

    @OneToMany(mappedBy = "fazenda", fetch = FetchType.LAZY)
    private List<CreditoCarbono> creditos;
}