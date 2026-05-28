package br.com.fiap.aquaorbital.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "TB_PERFIL")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_perfil")
    @SequenceGenerator(name = "seq_perfil", sequenceName = "SEQ_PERFIL", allocationSize = 1)
    @Column(name = "id_perfil")
    private Long id;

    @Column(name = "nome_perfil", nullable = false, unique = true, length = 50)
    private String nomePerfil;

    @Column(name = "descricao", length = 200)
    private String descricao;

    @OneToMany(mappedBy = "perfil", fetch = FetchType.LAZY)
    private List<Usuario> usuarios;
}