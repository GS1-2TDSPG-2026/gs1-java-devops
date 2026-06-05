package br.com.fiap.Phycocarbon.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "TB_PERFIL", schema = "rm562085")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Perfil extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_perfil")
    @SequenceGenerator(name = "sq_perfil", sequenceName = "rm562085.SQ_PERFIL", allocationSize = 1)
    @Column(name = "id_perfil")
    private Long id;

    @Column(name = "nome_perfil", nullable = false, unique = true, length = 50)
    private String nomePerfil;

    @Column(name = "descricao", length = 200)
    private String descricao;

    @OneToMany(mappedBy = "perfil", fetch = FetchType.LAZY)
    private List<Usuario> usuarios;
}