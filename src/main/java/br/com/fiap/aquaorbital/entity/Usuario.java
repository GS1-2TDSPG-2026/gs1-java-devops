package br.com.fiap.aquaorbital.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "TB_USUARIO")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_usuario")
    @SequenceGenerator(name = "seq_usuario", sequenceName = "SEQ_USUARIO", allocationSize = 1)
    @Column(name = "id_usuario")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_perfil", nullable = false)
    private Perfil perfil;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @Column(name = "telefone", length = 20)
    private String telefone;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "ATIVO";

    @Column(name = "dt_criacao", updatable = false)
    @Builder.Default
    private LocalDateTime dtCriacao = LocalDateTime.now();

    @OneToMany(mappedBy = "usuarioResponsavel", fetch = FetchType.LAZY)
    private List<Fazenda> fazendas;

    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + perfil.getNomePerfil()));
    }
    @Override public String getPassword() { return senhaHash; }
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return "ATIVO".equals(status); }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return "ATIVO".equals(status); }
}