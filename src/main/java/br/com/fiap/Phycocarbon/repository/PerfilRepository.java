package br.com.fiap.Phycocarbon.repository;

import br.com.fiap.Phycocarbon.entity.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PerfilRepository extends JpaRepository<Perfil, Long> {
    Optional<Perfil> findByNomePerfil(String nomePerfil);
    boolean existsByNomePerfil(String nomePerfil);
}
