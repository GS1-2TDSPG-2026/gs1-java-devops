package br.com.fiap.aquaorbital.repository;

import br.com.fiap.aquaorbital.entity.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PerfilRepository extends JpaRepository<Perfil, Long> {
    Optional<Perfil> findByNomePerfil(String nomePerfil);
    boolean existsByNomePerfil(String nomePerfil);
}