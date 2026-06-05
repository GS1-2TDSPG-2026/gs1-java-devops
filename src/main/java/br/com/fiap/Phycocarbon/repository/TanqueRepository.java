package br.com.fiap.Phycocarbon.repository;

import br.com.fiap.Phycocarbon.entity.Tanque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TanqueRepository extends JpaRepository<Tanque, Long> {
    List<Tanque> findByFazendaId(Long fazendaId);
    List<Tanque> findByStatus(String status);
    Optional<Tanque> findByCodigoTanque(String codigoTanque);
    boolean existsByCodigoTanque(String codigoTanque);
}
