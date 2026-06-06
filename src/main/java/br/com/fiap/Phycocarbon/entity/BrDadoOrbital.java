package br.com.fiap.Phycocarbon.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "TB_DADO_ORBITAL_BR", schema = "rm562085")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrDadoOrbital extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_dado_orbital_br")
    @SequenceGenerator(
            name = "sq_dado_orbital_br",
            sequenceName = "rm562085.SQ_DADO_ORBITAL_BR",
            allocationSize = 1
    )
    @Column(name = "id_dado_orbital_br")
    private Long id;

    @Column(name = "cod_estacao", nullable = false, length = 10)
    private String codEstacao;

    @Column(name = "nome_estacao", length = 100)
    private String nomeEstacao;

    @Column(name = "dt_medicao", nullable = false, length = 10)
    private String dataMedicao;

    @Column(name = "hr_medicao", length = 4)
    private String horaMedicao;

    @Column(name = "temp_maxima", precision = 6, scale = 2)
    private BigDecimal tempMaxima;

    @Column(name = "temp_minima", precision = 6, scale = 2)
    private BigDecimal tempMinima;

    @Column(name = "temp_media", precision = 6, scale = 2)
    private BigDecimal tempMedia;

    @Column(name = "umidade_relativa", precision = 6, scale = 2)
    private BigDecimal umidadeRelativa;

    @Column(name = "precipitacao", precision = 8, scale = 2)
    private BigDecimal precipitacao;

    @Column(name = "velocidade_vento", precision = 6, scale = 2)
    private BigDecimal velocidadeVento;

    @Column(name = "direcao_vento", precision = 6, scale = 2)
    private BigDecimal direcaoVento;

    @Column(name = "pressao_atm", precision = 8, scale = 2)
    private BigDecimal pressaoAtm;

    @Column(name = "radiacao_global", precision = 10, scale = 2)
    private BigDecimal radiacaoGlobal;

    @Column(name = "json_original", columnDefinition = "CLOB")
    private String jsonOriginal;
}