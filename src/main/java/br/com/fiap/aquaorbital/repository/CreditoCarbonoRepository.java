package br.com.fiap.aquaorbital.repository;

import br.com.fiap.aquaorbital.entity.CreditoCarbono;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CreditoCarbonoRepository extends JpaRepository<CreditoCarbono, Long> {
    List<CreditoCarbono> findByFazendaId(Long fazendaId);
    List<CreditoCarbono> findByStatus(String status);
    Optional<CreditoCarbono> findByHashAuditoria(String hashAuditoria);
    List<CreditoCarbono> findByLoteId(Long loteId);
}