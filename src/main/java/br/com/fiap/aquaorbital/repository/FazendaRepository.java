package br.com.fiap.aquaorbital.repository;

import br.com.fiap.aquaorbital.entity.Fazenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FazendaRepository extends JpaRepository<Fazenda, Long> {
    List<Fazenda> findByUsuarioResponsavelId(Long usuarioId);
    List<Fazenda> findByStatus(String status);
    List<Fazenda> findByUf(String uf);
}
